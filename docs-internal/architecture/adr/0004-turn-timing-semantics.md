# ADR-0004: Turn Timing Semantics

**Status:** Accepted  
**Date:** 2026-02-13

---

## Context

Tank Royale's turn-based game loop has two timing parameters:

1. **Turn Timeout** — Hard deadline for bot responses before SkippedTurnEvent
2. **TPS (Turns Per Second)** — Visual rendering speed for spectators

**Problem:** How do bot processing completion and visual rendering timing interact to ensure deterministic battles while
respecting user TPS preferences?

**Critical requirements:**

- **Deterministic battles** (same turnTimeout → same outcome, regardless of TPS)
- **Fair competition** (all bots get same deadline)
- **Performance** (fast bots enable high TPS when requested)

---

## Decision

Use a **two-phase timing model** that separates bot processing from visual rendering:

### Phase 1: Bot Processing (Turn Logic Completion)

**Turn timeout is a HARD DEADLINE**, not a minimum wait time:

1. Server waits for bot intents up to `turnTimeout` microseconds
2. When **all bots respond** OR **turnTimeout expires**, turn logic immediately completes
3. Bots that didn't respond within `turnTimeout` receive **SkippedTurnEvent**
4. Next turn begins for bots immediately (no artificial delay)

**Bot processing completes at:** `min(max(bot_response_times), turnTimeout)`

### Phase 2: Visual Rendering (Observer Frame Timing)

After turn logic completes, server applies **visual delay** to respect TPS:

```
requested_turn_duration = 1000 ms / requestedTPS  (if TPS > 0)
visual_delay = max(0, requested_turn_duration - bot_processing_duration)
```

**Special TPS values:**

- `TPS > 0`: Inject delay to match requested frame rate
- `TPS = -1`: No delay (as fast as possible, bot-limited)
- `TPS = 0`: Pause game (infinite delay)

---

## Rationale

**Why two-phase timing:**

- **Bot fairness** (Phase 1): All bots get the same hard deadline
- **Visual experience** (Phase 2): Observers get smooth playback at the desired speed
- **Independence**: Bot processing and visual rendering don't interfere

**Why turn timeout is a deadline, not minimum:**

- ✅ Fast bots + slow TPS: correct visual delay
- ✅ At TPS=-1, speed is bot-limited (no artificial waiting)
- ✅ Deterministic battles (same turnTimeout = identical results at any TPS)
- ❌ **Rejected**: "Wait full turnTimeout even if all bots respond early" would slow down batch simulations unnecessarily

### Timing Diagram with Two Phases

```
Scenario 1: Fast bots, slow TPS
turnTimeout=10000µs (10ms), all bots respond in 8000µs (8ms), TPS=30 (33.33ms period)

Time →
0ms ──────── 8ms ───────────────────── 33.33ms
│            │                         │
Start        │                         Visual frame advances
             │                         (after 25.33ms delay)
             └─ Turn logic completes
                Bots receive next turn events
                (bot processing duration = 8ms)


Scenario 2: Fast bots, unlimited TPS  
turnTimeout=10000µs (10ms), all bots respond in 8000µs (8ms), TPS=-1

Time →
0ms ──────── 8ms
│            │
Start        Turn logic completes AND visual advances
             (no delay, bot-limited)
             Actual TPS = 125 (1000/8)


Scenario 3: One bot times out
turnTimeout=10000µs (10ms), Bot A=5000µs (5ms), Bot B=7000µs (7ms), Bot C=(no response), TPS=30

Time →
0ms ──────── 10ms ──────────────────── 33.33ms
│            │                         │
Start        │                         Visual frame advances
             │                         (after 23.33ms delay)
             └─ Turn logic completes at deadline
                Bot C receives SkippedTurnEvent
                (bot processing duration = 10ms)


Scenario 4: Very slow TPS
turnTimeout=10000µs (10ms), all bots respond in 8000µs (8ms), TPS=10 (100ms period)

Time →
0ms ──────── 8ms ──────────────────────────────── 100ms
│            │                                     │
Start        │                                     Visual frame advances
             │                                     (after 92ms delay)
             └─ Turn logic completes
                Bots already on next turn
                (bot processing duration = 8ms)
```

---

## Implementation

```kotlin
fun executeTurn() {
    // Phase 1: Bot Processing
    val botResponseTimes = waitForAllBotIntents(maxWaitTime = turnTimeout)
    val botProcessingDuration = min(botResponseTimes.max(), turnTimeout)

    // Bots that exceeded deadline get SkippedTurnEvent
    val skippedBots = botsWhere { responseTime > turnTimeout }
    skippedBots.forEach { sendSkippedTurnEvent(it) }

    // Turn logic completes IMMEDIATELY
    processTurnLogic()
    // ← Next turn already started for bots

    // Phase 2: Visual Rendering Delay
    val requestedTurnDuration = when {
        requestedTPS == 0 -> Double.POSITIVE_INFINITY  // Pause
        requestedTPS < 0 -> 0.0  // Unlimited (no delay)
        else -> 1000.0 / requestedTPS
    }

    val visualDelay = max(0.0, requestedTurnDuration - botProcessingDuration)

    if (requestedTPS == 0) {
        pauseGame()
    } else if (visualDelay > 0) {
        Thread.sleep(visualDelay.toLong())
    }
    // ← Visual frame advances for observers
}
```

---

## Consequences

- ✅ **Deterministic battles**: Same `turnTimeout` + same bots = identical results at any TPS
- ✅ **Maximum speed within bot constraints**: TPS=-1 runs as fast as bots respond
- ✅ **Fair competition**: All bots get identical `turnTimeout` deadline
- ✅ **Flexible visualization**: TPS controls only observer experience
- ❌ **Bots must process synchronously**: Cannot rely on async processing after sending intent
- ❌ **Turn timeout cannot be zero**: Minimum 1µs required

## Guidance for Bot Developers

**Process events before sending intent** - turn completes immediately when all bots respond:

```java
// GOOD: Process events synchronously before responding  
void run() {
    while (isRunning()) {
        go();  // Dispatches events FIRST, then sends intent
        // After go() returns, all events processed
        var target = findBestTarget();
        aimAt(target);
    }
}
```

**Don't assume minimum processing time** - `turnTimeout` is a deadline, not guaranteed window:

```java
// BAD: Assuming turn takes at least turnTimeout
Thread.sleep(turnTimeout -elapsed);  // Turn may have already advanced!
```

---

## Examples Summary

| Scenario               | turnTimeout | Bot Responses                 | TPS | Bot Phase | Visual Phase | Total   |
|------------------------|-------------|-------------------------------|-----|-----------|--------------|---------|
| Fast bots, slow visual | 10000µs     | 8000µs (8ms)                  | 30  | 8ms       | +25.33ms     | 33.33ms |
| Fast bots, unlimited   | 10000µs     | 8000µs (8ms)                  | -1  | 8ms       | +0ms         | 8ms     |
| One timeout            | 10000µs     | A=5000µs, B=7000µs, C=timeout | 30  | 10ms      | +23.33ms     | 33.33ms |
| Very slow visual       | 10000µs     | 8000µs (8ms)                  | 10  | 8ms       | +92ms        | 100ms   |

**Key Insight:** Bot processing duration and visual delay are independent. Battle determinism guaranteed because only
Phase 1 affects outcomes.

### Timing Diagram with Two Phases

```
Scenario 1: Fast bots, slow TPS
turnTimeout=10000µs (10ms), all bots respond in 8000µs (8ms), TPS=30 (33.33ms period)

Time →
0ms ──────── 8ms ───────────────────── 33.33ms
│            │                         │
Start        │                         Visual frame advances
             │                         (after 25.33ms delay)
             └─ Turn logic completes
                Bots receive next turn events
                (bot processing duration = 8ms)


Scenario 2: Fast bots, unlimited TPS  
turnTimeout=10000µs (10ms), all bots respond in 8000µs (8ms), TPS=-1

Time →
0ms ──────── 8ms
│            │
Start        Turn logic completes AND visual advances
             (no delay, bot-limited)
             Actual TPS = 125 (1000/8)


Scenario 3: One bot times out
turnTimeout=10000µs (10ms), Bot A=5000µs (5ms), Bot B=7000µs (7ms), Bot C=(no response), TPS=30

Time →
0ms ──────── 10ms ──────────────────── 33.33ms
│            │                         │
Start        │                         Visual frame advances
             │                         (after 23.33ms delay)
             └─ Turn logic completes at deadline
                Bot C receives SkippedTurnEvent
                (bot processing duration = 10ms)


Scenario 4: Very slow TPS
turnTimeout=10000µs (10ms), all bots respond in 8000µs (8ms), TPS=10 (100ms period)

Time →
0ms ──────── 8ms ──────────────────────────────── 100ms
│            │                                     │
Start        │                                     Visual frame advances
             │                                     (after 92ms delay)
             └─ Turn logic completes
                Bots already on next turn
                (bot processing duration = 8ms)
```

---

## Consequences

### Positive

- ✅ **Deterministic battles**: Battle outcome depends ONLY on `turnTimeout`, never on TPS
    - Same `turnTimeout` + same bots = identical results at any TPS
    - Critical for competitive play and reproducible simulations

- ✅ **Maximum speed within bot constraints**: TPS=-1 runs as fast as bots can respond
    - No artificial waiting when all bots are done
    - Batch simulations run at maximum speed (bot-limited)

- ✅ **Clean separation of concerns**:
    - Phase 1 (bot processing) ensures fairness
    - Phase 2 (visual rendering) ensures smooth playback
    - Each phase independent from the other

- ✅ **Flexible visualization**: TPS controls only observer experience
    - Users can speed up/slow down/pause without affecting battle
    - Same battle can be watched at different speeds

- ✅ **Fair competition**: All bots get identical `turnTimeout` deadline
    - Fast bots don't get penalized (turn completes when they're done)
    - Slow bots get their full deadline before SkippedTurnEvent

### Negative

- ❌ **Bots must process events synchronously**: Bots cannot rely on async processing after sending intent
    - Mitigation: Bot APIs dispatch events before sending intent in `go()`
    - Mitigation: Document synchronous processing requirement

- ❌ **Turn timeout cannot be zero**: Minimum 1µs required for bot communication
    - Mitigation: This is already a practical constraint
    - Mitigation: Document minimum turnTimeout value

### Neutral

- Turn timeout and TPS are completely independent parameters
- Default TPS=30 provides smooth visualization (30 fps)
- Default turnTimeout depends on bot complexity and network latency
- Replays ignore `turnTimeout` entirely (pure visualization, see Replay Behavior below)

---

## Replay Behavior

Replays simplify to **pure Phase 2** timing since all turn data is pre-recorded:

```kotlin
fun executeReplayTurn() {
    // Load pre-recorded turn data
    val turnData = loadNextTurnFromRecording()
    renderTurn(turnData)

    // Only Phase 2: Visual delay (no bot processing)
    val visualDelayMs = when {
        tps == 0 -> {
            pauseReplay()
            return
        }
        tps < 0 -> 0  // Unlimited playback speed
        else -> 1000 / tps
    }

    if (visualDelayMs > 0) {
        Thread.sleep(visualDelayMs)
    }
}
```

**Key differences from live battles:**

| Aspect                 | Live Battle                         | Replay                          |
|------------------------|-------------------------------------|---------------------------------|
| **Turn timeout**       | ✅ Phase 1 (bot deadline)            | ❌ Not applicable (no bots)      |
| **Bot response times** | ✅ Determines processing duration    | ❌ Not applicable (pre-recorded) |
| **TPS**                | ✅ Phase 2 (visual delay only)       | ✅ Pure visual timing            |
| **Pause (TPS=0)**      | ✅ Pauses visual, bots still process | ✅ Pauses playback               |
| **Unlimited (TPS=-1)** | ✅ As fast as bots respond           | ✅ As fast as rendering allows   |

---

## Guidance for Bot Developers

### Process Events Before Sending Intent

The turn completes immediately when all bots respond. You **cannot** rely on the server waiting for `turnTimeout`:

**Wrong approach:**

```java
// BAD: Async processing after sending intent
void run() {
    while (isRunning()) {
        go();  // Send intent immediately

        // Process in background
        asyncProcessor.update(getEvents());

        // WRONG: Next turn may start before this completes!
        // Server doesn't wait for turnTimeout if all bots responded
    }
}
```

**Correct approach:**

```java
// GOOD: Process events synchronously before responding  
void run() {
    while (isRunning()) {
        go();  // This dispatches events FIRST, then sends intent

        // After go() returns, all events processed
        // Next turn can start immediately without issues
        var target = findBestTarget();
        aimAt(target);
    }
}
```

### Event Processing Order Guaranteed by Bot API

The Bot API ensures `go()` is synchronous and completes event processing before submitting intent:

```java
// Inside BaseBot.go()
public void go() {
    // 1. Dispatch all queued events (calls your event handlers)
    baseBotInternals.dispatchEvents(currentTick.getTurnNumber());

    // 2. Only THEN send intent to server
    baseBotInternals.execute();

    // 3. By the time go() returns, your event handlers have run
}
```

**What this means:**

- Your `onScannedBot()`, `onHitByBullet()`, etc. handlers run **before** go() returns
- You can safely compute strategy based on events **after** go() returns
- The server may start the next turn immediately after go() sends the intent

### Don't Assume Minimum Processing Time

**Wrong assumption:**

```java
// BAD: Assuming turn takes at least turnTimeout
void run() {
    while (isRunning()) {
        long startTime = System.currentTimeMillis();
        go();

        // Sleep to "use" the remaining turn timeout
        // WRONG: Turn may have already advanced!
        long elapsed = System.currentTimeMillis() - startTime;
        Thread.sleep(turnTimeout - elapsed);
    }
}
```

**Correct understanding:**

- `turnTimeout` is a **deadline**, not a guaranteed processing window
- Turn completes when all bots respond (may be much faster than turnTimeout)
- Only bots that EXCEED turnTimeout get penalized (SkippedTurnEvent)
- Respond as fast as you can; don't artificially wait

---

## Test Coverage

See `server/src/test/kotlin/core/TurnTimingTest.kt` for comprehensive tests covering:

### Phase 1: Bot Processing Tests

- Turn completes when all bots respond (early completion)
- Turn completes at deadline when bot times out (SkippedTurnEvent)
- Multiple bots with varying response times
- All bots respond instantly (minimum latency)

### Phase 2: Visual Delay Tests

- Visual delay correctly added for slow TPS (e.g., TPS=10)
- No visual delay for unlimited TPS (TPS=-1)
- Pause behavior (TPS=0)
- Visual delay respects bot processing duration

### Determinism Tests

- Same turnTimeout produces identical battle outcomes at different TPS values
- TPS changes don't affect bot behavior or battle results
- Fast bots + slow TPS: correct visual delay
- Slow bots + fast TPS: no artificial speedup

### Edge Cases

- TPS=0 (pause): bots still process, visual paused
- TPS=-1 (unlimited): as fast as possible, but bot-limited by max(bot_response_times)
- turnTimeout equals average response time
- turnTimeout less than average response time (many SkippedTurnEvents)
- Very high TPS (e.g., 1000) with slow bots

---

## Examples Summary

| Scenario               | turnTimeout | Bot Responses                 | TPS | Bot Phase | Visual Phase | Total   |
|------------------------|-------------|-------------------------------|-----|-----------|--------------|---------|
| Fast bots, slow visual | 10000µs     | 8000µs (8ms)                  | 30  | 8ms       | +25.33ms     | 33.33ms |
| Fast bots, unlimited   | 10000µs     | 8000µs (8ms)                  | -1  | 8ms       | +0ms         | 8ms     |
| One timeout            | 10000µs     | A=5000µs, B=7000µs, C=timeout | 30  | 10ms      | +23.33ms     | 33.33ms |
| Very slow visual       | 10000µs     | 8000µs (8ms)                  | 10  | 8ms       | +92ms        | 100ms   |
| All instant            | 10000µs     | 100µs (0.1ms)                 | -1  | 0.1ms     | +0ms         | 0.1ms   |
| Paused                 | 10000µs     | 8000µs (8ms)                  | 0   | 8ms       | ∞ (paused)   | ∞       |

**Key Insight:** Bot processing duration and visual delay are completely independent. Battle determinism is guaranteed
because only Phase 1 (bot processing) affects outcomes.

---

## Related Decisions

- **ADR-0003:** Real-Time Game Loop Architecture
- **ADR-0001:** WebSocket Communication Protocol

---

## References

- [ResettableTimer Implementation](/server/src/main/kotlin/dev/robocode/tankroyale/server/core/ResettableTimer.kt)
- [GameServer Turn Handling](/server/src/main/kotlin/dev/robocode/tankroyale/server/core/GameServer.kt)
- [TPS Documentation](/docs-build/docs/articles/tps.md)
