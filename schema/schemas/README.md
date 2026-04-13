# Schemas

This directory contains the YAML schema files that define the WebSocket message protocol used by Robocode Tank Royale for network communication between the server, bots, observers, and controllers.

Each `.schema.yaml` file is a [JSON Schema Draft 2020-12](https://json-schema.org/) definition. They are the authoritative contract for every message exchanged on the wire.

> **For sequence diagrams and flow documentation** — how these messages participate in the battle lifecycle, connection handshake, turn loop, and controller commands — see:
> **[`docs-internal/architecture/models/flows/`](../../docs-internal/architecture/models/flows/README.md)**

---

## Handshakes — Connection Establishment

Initial messages exchanged when clients connect to the server.

| Schema | Direction | Purpose |
|--------|-----------|---------|
| [server-handshake.schema.yaml](server-handshake.schema.yaml) | Server → Client | Server identity + sessionId assignment |
| [bot-handshake.schema.yaml](bot-handshake.schema.yaml) | Bot → Server | Bot registration with metadata |
| [observer-handshake.schema.yaml](observer-handshake.schema.yaml) | Observer → Server | Observer registration |
| [controller-handshake.schema.yaml](controller-handshake.schema.yaml) | Controller → Server | Controller registration |
| [bot-ready.schema.yaml](bot-ready.schema.yaml) | Bot → Server | Bot ready to start battle |

---

## Commands — Controller Operations

Commands sent by controllers to manage game state.

| Schema | Direction | Purpose |
|--------|-----------|---------|
| [start-game.schema.yaml](start-game.schema.yaml) | Controller → Server | Initiate battle with selected bots |
| [stop-game.schema.yaml](stop-game.schema.yaml) | Controller → Server | Terminate current battle |
| [pause-game.schema.yaml](pause-game.schema.yaml) | Controller → Server | Pause battle execution |
| [resume-game.schema.yaml](resume-game.schema.yaml) | Controller → Server | Resume paused battle |
| [next-turn.schema.yaml](next-turn.schema.yaml) | Controller → Server | Execute single turn (debug stepping) |
| [change-tps.schema.yaml](change-tps.schema.yaml) | Controller → Server | Change turns per second |
| [bot-policy-update.schema.yaml](bot-policy-update.schema.yaml) | Controller → Server | Update per-bot policy (debug graphics, breakpoint mode) |
| [enable-debug-mode.schema.yaml](enable-debug-mode.schema.yaml) | Controller → Server | Enable turn-by-turn debug stepping |
| [disable-debug-mode.schema.yaml](disable-debug-mode.schema.yaml) | Controller → Server | Disable debug stepping |

---

## Events — Server Notifications

Events broadcast by the server to inform clients of state changes.

### Game Lifecycle

| Schema | Recipients | Purpose |
|--------|------------|---------|
| [game-started-event-for-bot.schema.yaml](game-started-event-for-bot.schema.yaml) | Bots | Battle started, includes initial state |
| [game-started-event-for-observer.schema.yaml](game-started-event-for-observer.schema.yaml) | Observers, Controllers | Battle started, full state |
| [game-ended-event-for-bot.schema.yaml](game-ended-event-for-bot.schema.yaml) | Bots | Battle ended, personal results |
| [game-ended-event-for-observer.schema.yaml](game-ended-event-for-observer.schema.yaml) | Observers, Controllers | Battle ended, complete results |
| [game-aborted-event.schema.yaml](game-aborted-event.schema.yaml) | All | Battle cancelled/aborted |
| [game-paused-event-for-observer.schema.yaml](game-paused-event-for-observer.schema.yaml) | Observers, Controllers | Battle paused |
| [game-resumed-event-for-observer.schema.yaml](game-resumed-event-for-observer.schema.yaml) | Observers, Controllers | Battle resumed |

### Round Events

| Schema | Recipients | Purpose |
|--------|------------|---------|
| [round-started-event.schema.yaml](round-started-event.schema.yaml) | All | New round began |
| [round-ended-event-for-bot.schema.yaml](round-ended-event-for-bot.schema.yaml) | Bots | Round ended, personal stats |
| [round-ended-event-for-observer.schema.yaml](round-ended-event-for-observer.schema.yaml) | Observers, Controllers | Round ended, full stats |

### Turn Events

| Schema | Recipients | Purpose |
|--------|------------|---------|
| [tick-event-for-bot.schema.yaml](tick-event-for-bot.schema.yaml) | Bots | Turn state, events, bot own state |
| [tick-event-for-observer.schema.yaml](tick-event-for-observer.schema.yaml) | Observers, Controllers | Turn state, all bots' states |
| [skipped-turn-event.schema.yaml](skipped-turn-event.schema.yaml) | Bot | Bot failed to send intent in time |

### Bot Gameplay Events

| Schema | Recipients | Purpose |
|--------|------------|---------|
| [scanned-bot-event.schema.yaml](scanned-bot-event.schema.yaml) | Bot | Radar detected another bot |
| [hit-by-bullet-event.schema.yaml](hit-by-bullet-event.schema.yaml) | Bot | Bot took damage from bullet |
| [bullet-fired-event.schema.yaml](bullet-fired-event.schema.yaml) | Bot | Bot fired a bullet |
| [bullet-hit-bot-event.schema.yaml](bullet-hit-bot-event.schema.yaml) | Bot | Bot's bullet hit target |
| [bullet-hit-bullet-event.schema.yaml](bullet-hit-bullet-event.schema.yaml) | Bot | Bot's bullet hit another bullet |
| [bullet-hit-wall-event.schema.yaml](bullet-hit-wall-event.schema.yaml) | Bot | Bot's bullet hit wall |
| [bot-hit-bot-event.schema.yaml](bot-hit-bot-event.schema.yaml) | Bot | Bot collided with another bot |
| [bot-hit-wall-event.schema.yaml](bot-hit-wall-event.schema.yaml) | Bot | Bot collided with wall |
| [bot-death-event.schema.yaml](bot-death-event.schema.yaml) | Bot | Bot destroyed |
| [won-round-event.schema.yaml](won-round-event.schema.yaml) | Bot | Bot won the round |

### Team Events

| Schema | Recipients | Purpose |
|--------|------------|---------|
| [team-message-event.schema.yaml](team-message-event.schema.yaml) | Team Bot | Message from teammate |

### Meta Events

| Schema | Recipients | Purpose |
|--------|------------|---------|
| [bot-list-update.schema.yaml](bot-list-update.schema.yaml) | Observers, Controllers | Available bots changed |
| [tps-changed-event.schema.yaml](tps-changed-event.schema.yaml) | Observers, Controllers | Turns per second changed |

---

## Intents — Bot Actions

Messages sent by bots to declare their desired actions for the next turn.

| Schema | Direction | Purpose |
|--------|-----------|---------|
| [bot-intent.schema.yaml](bot-intent.schema.yaml) | Bot → Server | Bot's desired movement, rotation, firing |
| [team-message.schema.yaml](team-message.schema.yaml) | Bot → Server | Message to a teammate |

---

## State Objects — Data Transfer Objects

Reusable data structures embedded in events and commands.

| Schema | Purpose |
|--------|---------|
| [bot-state.schema.yaml](bot-state.schema.yaml) | Bot position, energy, direction (no ID) |
| [bot-state-with-id.schema.yaml](bot-state-with-id.schema.yaml) | Bot state including bot ID |
| [bot-info.schema.yaml](bot-info.schema.yaml) | Bot metadata (name, version, authors) |
| [bot-address.schema.yaml](bot-address.schema.yaml) | Bot network address |
| [bullet-state.schema.yaml](bullet-state.schema.yaml) | Bullet position, direction, power |
| [participant.schema.yaml](participant.schema.yaml) | Bot participating in battle |
| [game-setup.schema.yaml](game-setup.schema.yaml) | Battle configuration (arena, rules) |
| [initial-position.schema.yaml](initial-position.schema.yaml) | Starting position for a bot |
| [results-for-bot.schema.yaml](results-for-bot.schema.yaml) | Personal battle results |
| [results-for-observer.schema.yaml](results-for-observer.schema.yaml) | Complete battle results |
| [color.schema.yaml](color.schema.yaml) | RGB color value |
| [event.schema.yaml](event.schema.yaml) | Base event structure |
| [message.schema.yaml](message.schema.yaml) | Base message structure |

---

## Related Documentation

- **[Protocol Flow Diagrams](../../docs-internal/architecture/models/flows/README.md)** — Sequence diagrams for all protocol flows
- **[Message Schema Reference](../../docs-internal/architecture/models/message-schema/README.md)** — Detailed message contracts with examples
- **[ADR-0006: Schema-Driven Contracts](../../docs-internal/architecture/adr/0006-schema-driven-protocol-contracts.md)** — Design rationale
- **[ADR-0009: WebSocket Protocol](../../docs-internal/architecture/adr/0009-websocket-communication-protocol.md)** — Protocol design
