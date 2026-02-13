# ADR-0004: Turn Timing Semantics

**Status:** Accepted

**Date:** day-1 (documenting existing architecture)

**Decision Makers:** Architecture Team

---

## Context

Robocode Tank Royale's turn-based game loop has two timing parameters that control turn execution:

1. **Turn Timeout** — How long bots have to respond before being marked as "skipped"
2. **TPS (Turns Per Second)** — How fast the game visually progresses for spectators

A question arose: **What happens when all bots respond before the turn timeout?** And **how do TPS and turn timeout interact?**

This decision documents the intended semantics and ensures consistent behavior across server implementations.

### The Problem

Consider this scenario:
- Turn timeout: 30ms (bots have 30ms to respond)
- TPS: 30 (target ~33ms per turn for visualization)
- All bots respond in 5ms

**Should the server:**
1. Wait the full 30ms (turn timeout) regardless? (Interpretation A)
2. Proceed immediately after 5ms? (Interpretation B)
3. Proceed after TPS period (~33ms) since bots responded? (Interpretation C)

Each interpretation has different implications for:
- Fast batch simulations (running thousands of battles)
- Fair competition (ensuring all bots have guaranteed processing time)
- Spectator experience (smooth visualization at desired speed)

---

## Decision

We adopt **Interpretation C** with the following semantics:

### Turn Timeout = Maximum Deadline

The turn timeout defines the **maximum** time a bot has to submit its intent. After this deadline:
- Bots that haven't responded are marked as "skipped"
- The turn proceeds regardless of missing intents
- This is a **hard deadline**, not a guaranteed processing window

### Early Completion When All Respond

If **all participating bots** submit their intents before the turn timeout:
- The server **MAY** proceed to the next turn early
- This enables fast batch simulations when all bots are quick

### TPS Constrains Minimum Turn Duration

The TPS setting defines the **minimum** time between turns:
- Minimum turn period = `1,000,000,000 / TPS` nanoseconds
- Even if all bots respond instantly, the server waits for this period
- This ensures spectators can follow the action at a reasonable pace

### Combined Behavior

```
Turn Duration = max(TPS_period, time_until_all_respond)
             ... but never exceeds turn_timeout (deadline kicks in)
```

In code terms:
- `minDelayNanos` = TPS period (visualization constraint)
- `maxDelayNanos` = Turn timeout (hard deadline)
- `notifyReady()` = All bots responded (can proceed after min delay)

---

## Rationale

### Why Not Guarantee Turn Timeout as Minimum?

**Rejected Interpretation A:** "Turn timeout is guaranteed minimum time for all bots"

This would mean even if all bots respond in 1ms, we wait 30ms. Problems:
- **Slow batch simulations**: Running 10,000 battles would take much longer
- **Unnecessary waiting**: If everyone is done, why wait?
- **TPS becomes meaningless**: Can't run faster than turn timeout allows

### Why Allow Early Completion?

**Benefits:**
- Fast batch simulations for training/testing
- Responsive gameplay when all bots are quick
- TPS=-1 (unlimited) actually runs as fast as possible

**The key insight:** If a bot needs more processing time, it should simply take longer to respond. The turn timeout protects against infinitely slow bots, not against fast bots finishing early.

### Why Have TPS Separate from Turn Timeout?

**TPS serves a different purpose than turn timeout:**

| Aspect | Turn Timeout | TPS |
|--------|--------------|-----|
| Purpose | Fairness (prevent stalling) | Visualization speed |
| Who cares | Bots | Spectators |
| What happens if exceeded | Bot skips turn | N/A (minimum, not maximum) |

**Example scenarios:**

1. **Development/Debugging (TPS=10, timeout=30ms)**
   - Slow playback for observation
   - Bots still have 30ms to respond per turn
   - Turns take at least 100ms (1/10 second) for viewing

2. **Batch Simulation (TPS=-1, timeout=30ms)**
   - Run as fast as possible
   - No TPS constraint (minimum = 0)
   - If all bots respond in 5ms, proceed in 5ms
   - Turn timeout still protects against slow bots

3. **Competition (TPS=30, timeout=30ms)**
   - Real-time viewing at 30 fps
   - Bots have ~30ms per turn
   - Balanced for both viewing and bot processing

---

## Implementation

### Timer Configuration in GameServer

```kotlin
private fun resetTurnTimeout() {
    val minPeriodNanos = calculateTurnTimeoutMinPeriod().inWholeNanoseconds  // TPS constraint
    val maxPeriodNanos = calculateTurnTimeoutMaxPeriod().inWholeNanoseconds  // Turn timeout

    turnTimeoutTimer?.schedule(
        minDelayNanos = minPeriodNanos,
        maxDelayNanos = maxOf(minPeriodNanos, maxPeriodNanos)
    )
}

private fun calculateTurnTimeoutMinPeriod(): Duration {
    return if (tps <= 0) Duration.ZERO else 1_000_000_000.nanoseconds / tps
}

private fun calculateTurnTimeoutMaxPeriod(): Duration {
    return gameSetup.turnTimeout
}
```

### Intent Collection

```kotlin
internal fun handleBotIntent(conn: WebSocket, intent: BotIntent) {
    // ... update intent ...
    
    // If all bot intents have been received, we can start next turn
    botsThatSentIntent += conn
    if (botIntents.size == botsThatSentIntent.size) {
        turnTimeoutTimer?.notifyReady()  // Signal early completion possible
    }
}
```

### Timing Diagram

```
Time →
0ms          10ms         20ms         30ms         40ms
|------------|------------|------------|------------|
             ↑                         ↑
             All bots respond          Turn timeout (deadline)
             
Case 1: TPS=30 (~33ms period)
├─────────── TPS period ──────────────┤
Result: Proceed at ~33ms (TPS constraint, since all responded)

Case 2: TPS=100 (10ms period)  
├── TPS ──┤
Result: Proceed at 10ms (all responded, TPS elapsed)

Case 3: TPS=-1 (no constraint)
Result: Proceed at 10ms (immediately when all respond)

Case 4: One bot doesn't respond, TPS=30
Result: Proceed at 30ms (turn timeout deadline, bot skips)
```

---

## Consequences

### Positive

- ✅ **Fast batch simulations**: TPS=-1 runs as fast as bots respond
- ✅ **Flexible visualization**: TPS controls spectator experience independently
- ✅ **Clear semantics**: Turn timeout = deadline, TPS = minimum pace
- ✅ **Fair competition**: All bots get same deadline regardless of response time

### Negative

- ❌ **Bot architecture matters**: Bots that respond "too fast" may miss events
  - Mitigation: Bot APIs dispatch events before sending intent in `go()`
  - Mitigation: Document that bots should process events before responding

- ❌ **Results may vary with TPS at unlimited speed**: 
  - At TPS=-1, turns may advance before bot's async processing completes
  - Mitigation: This is by design for batch simulation; use TPS=30 for consistent results

### Neutral

- Turn timeout and TPS can be configured independently
- Default TPS=30 and timeout=30000µs provide balanced defaults
- Bots should not assume any minimum processing time beyond their response time

---

## Guidance for Bot Developers

### Don't Rely on Turn Timeout as Processing Window

**Wrong approach:**
```java
// BAD: Assuming we have turn_timeout to process
void run() {
    while (isRunning()) {
        // Start processing in background
        asyncProcessor.update(getEvents());
        
        go();  // Send intent immediately
        
        // Assume server waits for turn_timeout
        // WRONG: Server may proceed as soon as all bots respond!
    }
}
```

**Correct approach:**
```java
// GOOD: Process events before responding
void run() {
    while (isRunning()) {
        go();  // This dispatches events FIRST, then sends intent
        
        // After go(), all events for this turn have been processed
        var target = findBestTarget();  // Use processed data
        aimAt(target);
    }
}
```

### Event Processing Order

The Bot API guarantees that `go()` dispatches all pending events **before** sending the intent:

```java
// Inside BaseBot.go()
public void go() {
    // 1. Dispatch all queued events (calls your handlers)
    baseBotInternals.dispatchEvents(currentTick.getTurnNumber());
    
    // 2. Only THEN send intent to server
    baseBotInternals.execute();
}
```

This ensures your event handlers run before your intent is sent, regardless of TPS.

---

## Test Coverage

See `server/src/test/kotlin/core/TurnTimingTest.kt` for comprehensive tests covering:

- Turn timeout as deadline (without early completion)
- Early completion when all bots respond
- TPS constraint respected even with fast bots
- Unlimited TPS behavior
- Edge cases (equal TPS and timeout, TPS > timeout)

---

## Related Decisions

- **ADR-0003:** Real-Time Game Loop Architecture
- **ADR-0001:** WebSocket Communication Protocol

---

## References

- [ResettableTimer Implementation](/server/src/main/kotlin/dev/robocode/tankroyale/server/core/ResettableTimer.kt)
- [GameServer Turn Handling](/server/src/main/kotlin/dev/robocode/tankroyale/server/core/GameServer.kt)
- [TPS Documentation](/docs-build/docs/articles/tps.md)

