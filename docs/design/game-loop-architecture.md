---
id: ARCH-020
type: architecture
status: draft
links: [ADR-0011]
title: Real-Time Game Loop — Design Specification
provenance: inferred
---

# Real-Time Game Loop — Design Specification

This document details the real-time, discrete tick game loop architecture for Tank Royale. It complements ADR-0011 and centralizes diagrams, pseudo-code, and operational configuration.

## Overview

- Discrete tick loop at 30 TPS (~33ms per turn)
- Server authoritative physics and game state
- Synchronous intent collection per tick with strict timeouts
- Deterministic physics for fair, reproducible battles

## Synchronization Pattern

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

## State Machine

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

## Per-Tick Execution (Pseudo-code)

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

## Operational Configuration

```bash
java -jar server.jar --tps=30 --turn-timeout=30 --max-inactivity=30
```

## References

- ADR-0011: `/docs/decisions/0011-realtime-game-loop-architecture.md`
- Server Implementation: `/server/README.md`
- Related ADRs: ADR-0008 (Server-authoritative deterministic physics), ADR-0012 (Turn timing semantics)
