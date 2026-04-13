# Business Flows

This directory contains documentation of key business processes and their interactions with domain entities.

## What are Business Flows?

Business flows document **how entities interact through processes**:
- **Sequences** — Step-by-step process execution
- **Actors** — Entities and external systems involved
- **Messages** — Data exchanged between actors
- **Timing** — When events occur (especially important for 30 TPS game loop)
- **Error Handling** — What happens when things go wrong

Business flows answer: **"How does the system execute key processes?"**

---

## Core Flows

### Battle Lifecycle Flow
**[→ battle-lifecycle.md](./battle-lifecycle.md)**

The complete journey of a battle from creation to completion:
- **Phases:** WAIT_FOR_PARTICIPANTS → WAIT_FOR_READY → GAME_RUNNING → GAME_ENDED
- **Key Events:** Bot joins, all bots ready, game starts, victory condition
- **Duration:** From handshake to game end
- **Actors:** Bot, Server, Controller (optional)

**Use When:** Understanding battle progression, state transitions, timing

---

### Bot Connection Flow
**[→ bot-connection.md](./bot-connection.md)**

How a bot connects to the server and initializes for battle:
- **Sequence:** WebSocket connect → Handshakes → Validation → Ready
- **Messages:** server-handshake, bot-handshake, game-started-event, bot-ready
- **Timing:** Connection through game start
- **Actors:** Bot, Server

**Use When:** Implementing bot connection logic, debugging handshake issues

---

### Turn Execution Flow
**[→ turn-execution.md](./turn-execution.md)**

The per-turn sequence at 30 TPS (turns per second):
- **Frequency:** 30 times per second = ~33ms per turn
- **Sequence:** 15-step turn execution at authoritative server
- **Messages:** tick-event-for-bot, tick-event-for-observer, bot-intent
- **Timing:** Strict turn timeout enforcement
- **Actors:** Server, Bot, Observers

**Use When:** Understanding game loop, physics updates, event generation, timing constraints

---

### Event Handling Flow
**[→ event-handling.md](./event-handling.md)**

How events (WonRoundEvent, ScannedBotEvent, etc.) are generated, transmitted, queued, and dispatched:
- **Lifecycle:** Server generation → WebSocket transmission → client queue → dispatch to handlers
- **Platforms:** Java, Python, C# (cross-platform parity)
- **Output Handling:** RecordingPrintStream/RecordingTextWriter buffering and transfer
- **Critical Fix:** Proper stdout capture from round-end event handlers like onWonRound()
- **Comparison:** Tank Royale vs Classic Robocode event architecture

**Use When:** Debugging event handler issues, understanding cross-platform output capture, investigating event dispatch timing

---

## Flow Diagram Format

All flows use **Mermaid sequence diagrams**:

```mermaid
sequenceDiagram
    participant Actor1
    participant Actor2
    
    Actor1->>Actor2: Message sent
    Note over Actor2: Processing step
    Actor2-->>Actor1: Response
```

## Message Exchange Format

All flows reference **actual WebSocket messages** from `/schema/schemas/`:

```json
{
  "type": "message-type-name",
  "field1": "value1",
  "field2": "value2"
}
```

## Timing Diagrams

Critical for 30 TPS system. Flows include:
- Turn sequence with millisecond precision
- Timeout boundaries
- Event ordering constraints

---

## Keeping Diagrams in Sync

These flow diagrams are the **single canonical source** for all protocol sequence documentation. The YAML schema definitions live in `schema/schemas/` — whenever those change, the relevant flow document must be updated.

| If you change… | Update this flow |
|----------------|-----------------|
| `bot-handshake`, `server-handshake`, `bot-list-update` | `bot-connection.md` — Bot Joining / Leaving |
| `observer-handshake` | `bot-connection.md` — Observer Joining |
| `controller-handshake` | `bot-connection.md` — Controller Joining |
| `start-game`, `game-started-event-*`, `bot-ready` | `battle-lifecycle.md` — Phase 2 |
| `tick-event-for-bot`, `bot-intent`, `skipped-turn-event` | `turn-execution.md` — Steps 4–15 |
| `game-ended-event-*` | `battle-lifecycle.md` — Phase 4 |
| `stop-game`, `game-aborted-event` | `battle-lifecycle.md` — Aborting a Game |
| `pause-game`, `resume-game`, `game-paused-event-*`, `game-resumed-event-*` | `battle-lifecycle.md` — Pause/Resume |
| `next-turn`, `enable-debug-mode`, `disable-debug-mode` | `battle-lifecycle.md` — Debug Mode |
| `bot-policy-update` | `battle-lifecycle.md` — Breakpoint Mode / Debug Graphics |
| `change-tps`, `tps-changed-event` | `battle-lifecycle.md` — Changing TPS |
| Any bot gameplay event schema | `event-handling.md` |

> AI agents: also see the Schema ↔ Flow Mapping table in `.ai/documentation.md`.

---

## Related Documentation

- **[Message Schema](../message-schema/)** — WebSocket message contracts
- **[Schema YAML Definitions](/schema/schemas/)** — Actual message format definitions
- **[ADRs](../../adr/)** — Design decisions about flow architecture
- **[C4 Views](../../c4-views/)** — Component interactions

---

## Index

| Flow | Status | Purpose |
|------|--------|---------|
| [Battle Lifecycle](./battle-lifecycle.md) | ✅ | Battle state progression |
| [Bot Connection](./bot-connection.md) | ✅ | Bot initialization |
| [Turn Execution](./turn-execution.md) | ✅ | Per-turn game loop (30 TPS) |
| [Event Handling](./event-handling.md) | ✅ | Event generation, queuing, dispatch, and output capture |

---

**Last Updated:** 2026-04-13
