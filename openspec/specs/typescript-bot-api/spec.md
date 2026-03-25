# typescript-bot-api Specification

## Purpose
TBD - created by archiving change add-typescript-protocol-layer-2. Update Purpose after archive.
## Requirements
### Requirement: Protocol DTOs

The TypeScript Bot API SHALL provide protocol DTO interfaces matching the server JSON schema for all messages exchanged
between bot and server. DTOs SHALL use TypeScript interfaces (not classes) with camelCase field names matching the wire
format exactly.

#### Scenario: MessageType enum covers all server message types

- **WHEN** a developer accesses `MessageType.TICK_EVENT_FOR_BOT`
- **THEN** the value is `"TickEventForBot"`
- **AND** all message type string values match Java's `Message.Type` enum exactly

#### Scenario: BotHandshake DTO has all required fields

- **WHEN** a developer constructs a `BotHandshake` object
- **THEN** it contains fields: `type`, `sessionId`, `name`, `version`, `authors`, `description`, `homepage`, `countryCodes`, `gameTypes`, `platform`, `programmingLang`, `initialPosition`, `teamId`, `teamName`, `teamVersion`, `isDroid`, `secret`
- **AND** field names match the server JSON schema exactly

#### Scenario: ServerHandshake DTO has all required fields

- **WHEN** a `ServerHandshake` message is received
- **THEN** it contains fields: `type`, `sessionId`, `name`, `variant`, `version`, `gameTypes`, `gameSetup`

#### Scenario: BotIntent DTO has all fields

- **WHEN** a developer constructs a `BotIntent` object
- **THEN** it contains fields: `type`, `turnRate`, `gunTurnRate`, `radarTurnRate`, `targetSpeed`, `firepower`, `adjustGunForBodyTurn`, `adjustRadarForBodyTurn`, `adjustRadarForGunTurn`, `rescan`, `fireAssist`, `bodyColor`, `turretColor`, `radarColor`, `bulletColor`, `scanColor`, `tracksColor`, `gunColor`, `stdOut`, `stdErr`, `teamMessages`, `debugGraphics`

#### Scenario: TickEventForBot DTO contains bot state and events

- **WHEN** a `TickEventForBot` message is received
- **THEN** it contains `turnNumber`, `roundNumber`, `botState`, `bulletStates`, and `events`
- **AND** each element in `events` has a `type` field for discriminated deserialization

#### Scenario: In-tick event DTOs match server schema

- **WHEN** a `TickEventForBot` message contains a `BotDeathEvent`
- **THEN** the event has `turnNumber` and `victimId` fields
- **AND** all 11 in-tick event types (`BotDeathEvent`, `BotHitBotEvent`, `BotHitWallEvent`, `BulletFiredEvent`, `BulletHitBotEvent`, `BulletHitBulletEvent`, `BulletHitWallEvent`, `ScannedBotEvent`, `SkippedTurnEvent`, `WonRoundEvent`, `TeamMessageEvent`) have fields matching the Java schema DTOs

---

### Requirement: RuntimeAdapter

The TypeScript Bot API SHALL provide a `RuntimeAdapter` interface that abstracts runtime-specific APIs (WebSocket
creation, environment variable access, process exit) per ADR-0029. Implementations SHALL be provided for Node.js and
browser environments, with automatic runtime detection.

#### Scenario: Node.js runtime adapter creates WebSocket via ws library

- **WHEN** `NodeRuntimeAdapter.createWebSocket(url)` is called
- **THEN** a `ws` library WebSocket instance is returned
- **AND** the instance implements the `WebSocketLike` interface

#### Scenario: Browser runtime adapter creates native WebSocket

- **WHEN** `BrowserRuntimeAdapter.createWebSocket(url)` is called
- **THEN** a native browser `WebSocket` instance is returned

#### Scenario: Node.js runtime adapter reads environment variables

- **WHEN** `NodeRuntimeAdapter.getEnvVar("SERVER_URL")` is called
- **THEN** the value of `process.env.SERVER_URL` is returned

#### Scenario: Browser runtime adapter returns undefined for environment variables

- **WHEN** `BrowserRuntimeAdapter.getEnvVar("SERVER_URL")` is called
- **THEN** `undefined` is returned

#### Scenario: Automatic runtime detection

- **WHEN** `detectRuntime()` is called in a Node.js environment
- **THEN** a `NodeRuntimeAdapter` instance is returned
- **WHEN** `detectRuntime()` is called in a browser environment
- **THEN** a `BrowserRuntimeAdapter` instance is returned

---

### Requirement: EnvVars

The TypeScript Bot API SHALL provide an `EnvVars` utility that reads bot configuration from environment variables using
`RuntimeAdapter.getEnvVar()`. Variable names SHALL match the Java reference implementation exactly.

#### Scenario: EnvVars reads server URL

- **WHEN** the `SERVER_URL` environment variable is set to `"ws://localhost:7654"`
- **THEN** `EnvVars.getServerUrl()` returns `"ws://localhost:7654"`

#### Scenario: EnvVars reads comma-separated authors

- **WHEN** the `BOT_AUTHORS` environment variable is set to `"Alice, Bob, Charlie"`
- **THEN** `EnvVars.getBotAuthors()` returns `["Alice", "Bob", "Charlie"]`
- **AND** whitespace around commas is trimmed (matching Java's `split("\\s*,\\s*")`)

#### Scenario: EnvVars constructs BotInfo from environment

- **WHEN** `BOT_NAME`, `BOT_VERSION`, and `BOT_AUTHORS` are set
- **THEN** `EnvVars.getBotInfo()` returns a valid `BotInfo` instance

#### Scenario: EnvVars throws on missing required fields

- **WHEN** `BOT_NAME` is not set
- **AND** `EnvVars.getBotInfo()` is called
- **THEN** a `BotException` is thrown with message `"Missing environment variable: BOT_NAME"`

#### Scenario: EnvVars reads all 16 environment variable names

- **WHEN** environment variables are configured
- **THEN** `EnvVars` supports exactly these names: `SERVER_URL`, `SERVER_SECRET`, `BOT_NAME`, `BOT_VERSION`, `BOT_AUTHORS`, `BOT_DESCRIPTION`, `BOT_HOMEPAGE`, `BOT_COUNTRY_CODES`, `BOT_GAME_TYPES`, `BOT_PLATFORM`, `BOT_PROG_LANG`, `BOT_INITIAL_POS`, `TEAM_ID`, `TEAM_NAME`, `TEAM_VERSION`, `BOT_BOOTED`

---

### Requirement: WebSocket Handler

The TypeScript Bot API SHALL provide a `WebSocketHandler` that manages the WebSocket connection to the Tank Royale
server, routes incoming messages by type, and sends outgoing messages. The handler SHALL use `RuntimeAdapter` for
WebSocket creation to support both Node.js and browser environments.

#### Scenario: WebSocket handler connects to server

- **WHEN** `WebSocketHandler.connect()` is called with a server URL
- **THEN** a WebSocket connection is established via `RuntimeAdapter.createWebSocket()`
- **AND** a `ConnectedEvent` is fired on successful connection

#### Scenario: WebSocket handler routes messages by type

- **WHEN** a JSON message with `"type": "TickEventForBot"` is received
- **THEN** the `handleTick` callback is invoked
- **AND** messages with types `ServerHandshake`, `GameStartedEventForBot`, `GameEndedEventForBot`, `RoundStartedEvent`, `RoundEndedEventForBot`, `SkippedTurnEvent`, `GameAbortedEvent` are each routed to their respective handlers

#### Scenario: WebSocket handler replies to server handshake

- **WHEN** a `ServerHandshake` message is received
- **THEN** the handler stores the server handshake data
- **AND** replies with a `BotHandshake` message containing the bot's identity and configuration

#### Scenario: WebSocket handler sends BotReady after game start

- **WHEN** a `GameStartedEventForBot` message is received
- **THEN** the handler extracts `myId`, `teammateIds`, `gameSetup`, and initial position
- **AND** sends a `BotReady` message to the server

#### Scenario: WebSocket handler sends BotIntent

- **WHEN** `sendBotIntent(intent)` is called
- **THEN** the intent is serialized to JSON and sent via the WebSocket

#### Scenario: WebSocket handler fires disconnect event

- **WHEN** the WebSocket connection closes
- **THEN** a `DisconnectedEvent` is fired with the status code and reason

#### Scenario: WebSocket handler fires connection error event

- **WHEN** a WebSocket error occurs
- **THEN** a `ConnectionErrorEvent` is fired

---

### Requirement: Mappers

The TypeScript Bot API SHALL provide mapper functions that transform protocol DTOs (schema types) into API-layer objects
(value objects and events). Mappers SHALL have 1:1 correspondence with the Java reference implementation.

#### Scenario: EventMapper maps TickEventForBot to TickEvent

- **WHEN** `EventMapper.map(tickEventForBot, myBotId)` is called
- **THEN** the result is a `TickEvent` with mapped `botState`, `bulletStates`, and `events`

#### Scenario: EventMapper distinguishes self-death from opponent death

- **WHEN** a `BotDeathEvent` has `victimId` equal to `myBotId`
- **THEN** it is mapped to a `DeathEvent`
- **WHEN** a `BotDeathEvent` has `victimId` not equal to `myBotId`
- **THEN** it is mapped to a `BotDeathEvent` (opponent death)

#### Scenario: EventMapper distinguishes hit-by-bullet from bullet-hit-bot

- **WHEN** a `BulletHitBotEvent` has `victimId` equal to `myBotId`
- **THEN** it is mapped to a `HitByBulletEvent` with bullet, damage, and energy
- **WHEN** a `BulletHitBotEvent` has `victimId` not equal to `myBotId`
- **THEN** it is mapped to a `BulletHitBotEvent` with victimId, bullet, damage, and energy

#### Scenario: BotStateMapper converts all fields including colors

- **WHEN** `BotStateMapper.map(schemaBotState)` is called
- **THEN** all 22 fields are mapped including 7 color fields converted via `ColorUtil.fromHexColor()`
- **AND** null color hex strings result in null/undefined Color values

#### Scenario: BulletStateMapper maps single and collection

- **WHEN** `BulletStateMapper.map(schemaBulletState)` is called with a single bullet
- **THEN** a `BulletState` is returned with `bulletId`, `ownerId`, `power`, `x`, `y`, `direction`, and `color`
- **WHEN** `BulletStateMapper.map(bulletStates)` is called with a collection
- **THEN** a Set of mapped `BulletState` objects is returned

#### Scenario: GameSetupMapper maps all fields

- **WHEN** `GameSetupMapper.map(schemaGameSetup)` is called
- **THEN** a `GameSetup` is returned with `gameType`, `arenaWidth`, `arenaHeight`, `numberOfRounds`, `gunCoolingRate`, `maxInactivityTurns`, `turnTimeout`, `readyTimeout`

#### Scenario: ResultsMapper maps all score fields

- **WHEN** `ResultsMapper.map(resultsForBot)` is called
- **THEN** a `BotResults` is returned with `rank`, `survival`, `lastSurvivorBonus`, `bulletDamage`, `bulletKillBonus`, `ramDamage`, `ramKillBonus`, `totalScore`, `firstPlaces`, `secondPlaces`, `thirdPlaces`

#### Scenario: InitialPositionMapper maps API to schema format

- **WHEN** `InitialPositionMapper.map(initialPosition)` is called with a non-null position
- **THEN** a schema `InitialPosition` is returned with `x`, `y`, `direction`
- **WHEN** `InitialPositionMapper.map(null)` is called
- **THEN** `null` is returned

---

### Requirement: JSON Utilities

The TypeScript Bot API SHALL provide JSON serialization utilities for protocol messages, including type-discriminated
deserialization of event arrays within `TickEventForBot` messages.

#### Scenario: Type-discriminated event deserialization

- **WHEN** a JSON event array contains `{"type": "BotDeathEvent", "turnNumber": 42, "victimId": 3}`
- **THEN** the deserialized object is a `BotDeathEvent` with `turnNumber = 42` and `victimId = 3`
- **AND** all 11 event types are correctly deserialized based on their `type` field

#### Scenario: JSON round-trip for BotIntent

- **WHEN** a `BotIntent` is serialized to JSON and deserialized back
- **THEN** all fields are preserved exactly

#### Scenario: Color serialization uses hex format

- **WHEN** a Color value is serialized for a protocol message
- **THEN** it is written as a hex string (e.g., `"#ff0000"`)
- **WHEN** a hex color string is deserialized
- **THEN** a Color object with correct R, G, B values is returned

