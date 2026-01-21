# Capability: WebSocket Protocol

The Tank Royale WebSocket protocol defines the communication between bots, observers, controllers, and the authoritative
server. It is a schema-driven, asynchronous protocol.

## Requirements

### Requirement: Bot Connection Lifecycle

The protocol SHALL support a defined bot connection lifecycle, including handshaking and session identification.

#### Scenario: Bot joins the server

- **WHEN** a Bot opens a WebSocket connection
- **THEN** the Server SHALL send a `server-handshake` containing a `session-id`
- **AND** the Bot SHALL respond with a `bot-handshake` containing that `session-id`

### Requirement: Game Start Synchronization

The protocol SHALL ensure all participating bots are ready before starting a turn-based battle.

#### Scenario: All bots become ready

- **WHEN** the Server sends `game-started-event-for-bot` to all selected bots
- **AND** all bots respond with `bot-ready` within the `ready-timeout`
- **THEN** the Server SHALL transition to `GAME_RUNNING` state

### Requirement: Turn-Based Main Loop

The protocol SHALL advance the game in discrete turns (ticks), where each turn requires a reactive exchange of state and
intent.

#### Scenario: Running next turn

- **WHEN** the Server state is `GAME_RUNNING`
- **THEN** the Server SHALL send a `tick-event-for-bot` to each bot
- **AND** each Bot SHOULD respond with a `bot-intent` before the turn timeout
- **AND** the Server SHALL advance the physics simulation based on received intents

### Requirement: Language Agnostic Schemas

All protocol messages SHALL follow the JSON schemas defined in `schema/schemas/`, ensuring cross-language compatibility
for all Bot APIs.

#### Scenario: Schema validation

- **WHEN** a message is sent or received by any client or server
- **THEN** it MUST validate against its corresponding `.schema.yaml` definition
