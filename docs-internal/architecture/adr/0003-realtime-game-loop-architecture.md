# ADR-0003: Real-Time Game Loop Architecture

**Status:** Accepted  
**Date:** 2026-02-11

---

## Context

Tank Royale is a real-time programming game where multiple bots battle simultaneously.

**Problem:** How to synchronize actions of multiple bots while ensuring deterministic, fair gameplay?

**Requirements:**
- Consistent frame rate (30 TPS target)
- Deterministic physics (reproducible results)
- Fair synchronization (all bots see same state)
- Handle bot timeouts gracefully
- Support pause/resume for debugging

---

## Decision

Use a **turn-based discrete tick loop** at **30 TPS** with:
- Server as authoritative game state manager
- Synchronous bot intent collection per tick  
- Strict timeout enforcement per bot
- Deterministic physics simulation

---

## Rationale

**Why tick-based loop:**
- ✅ **Deterministic**: Same inputs → same outputs (reproducible)
- ✅ **Fair synchronization**: All bots receive identical game state simultaneously
- ✅ **Timeout handling**: Bots can't stall the game (fixed deadline)
- ✅ **Predictable performance**: 30 TPS = ~33ms per turn budget
- ✅ **Network-friendly**: 30 TPS matches typical latency constraints

### Synchronization Pattern

```mermaid
sequenceDiagram
    participant Server
    participant Bot1
    participant Bot2
    participant Bot3
    
    Note over Server: Turn N begins
    Server->>Bot1: tick-event (same game state)
    Server->>Bot2: tick-event (same game state)
    Server->>Bot3: tick-event (same game state)
    
    par Bots process in parallel
        Bot1->>Bot1: Decide action
        Bot2->>Bot2: Decide action  
        Bot3->>Bot3: Decide action
    end
    
    Bot1->>Server: bot-intent
    Bot2->>Server: bot-intent
    Bot3->>Server: bot-intent
    
    Note over Server: Wait for all intents (or timeout)
    Note over Server: Advance physics → Turn N+1
```

**Alternatives rejected:**
- **Continuous real-time**: Non-deterministic, sync issues
- **Event-driven async**: Race conditions, unfair network advantages  
- **Lockstep sync**: One slow client stalls everyone
- **Hybrid tick+event**: Added complexity without clear benefit

---

## Implementation

### Game Loop State Machine

```mermaid
stateDiagram-v2
    [*] --> WAIT_FOR_PARTICIPANTS: Server starts
    WAIT_FOR_PARTICIPANTS --> WAIT_FOR_READY: All bots connected
    WAIT_FOR_READY --> GAME_RUNNING: All bots ready
    GAME_RUNNING --> GAME_PAUSED: Pause requested
    GAME_PAUSED --> GAME_RUNNING: Resume requested
    GAME_RUNNING --> GAME_STOPPED: Battle ends
    GAME_STOPPED --> [*]
    
    note right of GAME_RUNNING
        Main tick loop: 30 iterations/sec
    end note
```

### Per-Tick Execution

```kotlin
fun executeTurn() {
    // 1. Send tick events to all bots (same game state)
    bots.forEach { it.sendTickEvent(gameState) }
    
    // 2. Collect intents with timeout (~30ms)
    val intents = bots.associateWith { bot ->
        try {
            bot.receiveIntent(timeout = botTimeoutMs)
        } catch (e: TimeoutException) {
            bot.sendSkippedTurnEvent()
            null // Late response
        }
    }
    
    // 3. Apply all valid intents to physics
    applyIntents(intents.filterNotNull())
    updatePhysics()
    checkCollisions()
    
    // 4. Maintain 30 TPS
    sleepToMaintainTPS()
}
```

**Configuration:**
```bash
java -jar server.jar --tps=30 --turn-timeout=30 --max-inactivity=30
```

---

## Consequences

- ✅ Deterministic physics (fair competition)  
- ✅ Timeout enforcement prevents game stalling
- ✅ Predictable performance (33ms per turn)
- ✅ Pause/resume support for debugging
- ✅ Replay system possible (record intents per turn)
- ❌ Fixed frame rate (can't exceed 30 TPS)
- ❌ Movement quantized to 33ms granularity
- ❌ Bot logic must complete within timeout

---

## References

- [Game Loop Patterns](https://gameprogrammingpatterns.com/game-loop.html)
- [Server Implementation](/server/)

