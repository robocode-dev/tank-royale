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

### Requirement: Bot Event Base Class

The TypeScript Bot API SHALL provide a `BotEvent` abstract class that serves as the base for all in-game events, with a
`turnNumber` property and an `isCritical()` method that defaults to `false`.

#### Scenario: BotEvent carries turn number

- **WHEN** a BotEvent subclass is constructed with turnNumber `42`
- **THEN** `getTurnNumber()` returns `42`

#### Scenario: BotEvent is not critical by default

- **WHEN** a non-critical BotEvent subclass is constructed
- **THEN** `isCritical()` returns `false`

---

### Requirement: Critical Events

The TypeScript Bot API SHALL mark `DeathEvent`, `WonRoundEvent`, and `SkippedTurnEvent` as critical events. Critical
events are never removed from the event queue due to age.

#### Scenario: DeathEvent is critical

- **WHEN** a `DeathEvent` is constructed
- **THEN** `isCritical()` returns `true`

#### Scenario: WonRoundEvent is critical

- **WHEN** a `WonRoundEvent` is constructed
- **THEN** `isCritical()` returns `true`

#### Scenario: SkippedTurnEvent is critical

- **WHEN** a `SkippedTurnEvent` is constructed
- **THEN** `isCritical()` returns `true`

---

### Requirement: TickEvent

The TypeScript Bot API SHALL provide a `TickEvent` class containing the round number, bot state, bullet states, and
nested events for the current turn.

#### Scenario: TickEvent fields

- **WHEN** a `TickEvent` is constructed with roundNumber `3`, a BotState, a list of BulletStates, and a list of BotEvents
- **THEN** `getRoundNumber()` returns `3`
- **AND** `getBotState()` returns the provided BotState
- **AND** `getBulletStates()` returns the provided BulletState collection
- **AND** `getEvents()` returns the provided BotEvent collection

---

### Requirement: ScannedBotEvent

The TypeScript Bot API SHALL provide a `ScannedBotEvent` with fields: scannedByBotId, scannedBotId, energy, x, y,
direction, and speed, all matching the Java reference implementation.

#### Scenario: ScannedBotEvent fields

- **WHEN** a `ScannedBotEvent` is constructed with scannedByBotId `1`, scannedBotId `2`, energy `78.5`, x `300`, y `400`, direction `45.0`, speed `6.0`
- **THEN** all getter methods return the corresponding values

---

### Requirement: Collision and Wall Events

The TypeScript Bot API SHALL provide `HitBotEvent` (victimId, energy, x, y, isRammed), `HitWallEvent` (turnNumber only),
and `HitByBulletEvent` (bullet, damage, energy) with fields matching the Java reference implementation.

#### Scenario: HitBotEvent with ram

- **WHEN** a `HitBotEvent` is constructed with victimId `5`, energy `60.0`, x `100`, y `200`, isRammed `true`
- **THEN** `getVictimId()` returns `5`
- **AND** `isRammed()` returns `true`

#### Scenario: HitByBulletEvent fields

- **WHEN** a `HitByBulletEvent` is constructed with a BulletState, damage `12.0`, energy `88.0`
- **THEN** `getBullet()` returns the provided BulletState
- **AND** `getDamage()` returns `12.0`
- **AND** `getEnergy()` returns `88.0`

#### Scenario: HitWallEvent has no extra fields

- **WHEN** a `HitWallEvent` is constructed with turnNumber `10`
- **THEN** `getTurnNumber()` returns `10`
- **AND** no additional fields exist beyond those inherited from BotEvent

---

### Requirement: Bullet Events

The TypeScript Bot API SHALL provide `BulletFiredEvent` (bullet), `BulletHitBotEvent` (victimId, bullet, damage, energy),
`BulletHitBulletEvent` (bullet, hitBullet), and `BulletHitWallEvent` (bullet) with fields matching the Java reference
implementation.

#### Scenario: BulletFiredEvent carries bullet state

- **WHEN** a `BulletFiredEvent` is constructed with a BulletState
- **THEN** `getBullet()` returns that BulletState

#### Scenario: BulletHitBotEvent fields

- **WHEN** a `BulletHitBotEvent` is constructed with victimId `3`, a BulletState, damage `8.0`, energy `92.0`
- **THEN** `getVictimId()` returns `3`
- **AND** `getBullet()` returns the provided BulletState
- **AND** `getDamage()` returns `8.0`

#### Scenario: BulletHitBulletEvent carries both bullets

- **WHEN** a `BulletHitBulletEvent` is constructed with two BulletState instances
- **THEN** `getBullet()` returns the firing bullet
- **AND** `getHitBullet()` returns the other bullet

---

### Requirement: Death and Victory Events

The TypeScript Bot API SHALL provide `BotDeathEvent` (victimId), `DeathEvent` (turnNumber only), and `WonRoundEvent`
(turnNumber only) with fields and criticality matching the Java reference implementation.

#### Scenario: BotDeathEvent carries victim ID

- **WHEN** a `BotDeathEvent` is constructed with victimId `7`
- **THEN** `getVictimId()` returns `7`

#### Scenario: DeathEvent and WonRoundEvent have no extra fields

- **WHEN** a `DeathEvent` or `WonRoundEvent` is constructed with turnNumber `50`
- **THEN** `getTurnNumber()` returns `50`
- **AND** `isCritical()` returns `true`

---

### Requirement: Team Message Event

The TypeScript Bot API SHALL provide a `TeamMessageEvent` with fields message and senderId. Construction with a null or
undefined message SHALL throw an error.

#### Scenario: TeamMessageEvent fields

- **WHEN** a `TeamMessageEvent` is constructed with message `"attack"` and senderId `2`
- **THEN** `getMessage()` returns `"attack"`
- **AND** `getSenderId()` returns `2`

#### Scenario: TeamMessageEvent rejects null message

- **WHEN** a `TeamMessageEvent` is constructed with a null message
- **THEN** an error is thrown

---

### Requirement: Connection Events

The TypeScript Bot API SHALL provide `ConnectedEvent`, `DisconnectedEvent` (remote, statusCode?, reason?), and
`ConnectionErrorEvent` (error), all extending a `ConnectionEvent` base class with a serverUri property.

#### Scenario: ConnectedEvent carries server URI

- **WHEN** a `ConnectedEvent` is constructed with serverUri `"ws://localhost:7654"`
- **THEN** `getServerUri()` returns `"ws://localhost:7654"`

#### Scenario: DisconnectedEvent optional fields

- **WHEN** a `DisconnectedEvent` is constructed with remote `true`, statusCode `1000`, reason `"Normal closure"`
- **THEN** `isRemote()` returns `true`
- **AND** `getStatusCode()` returns `1000`
- **AND** `getReason()` returns `"Normal closure"`

#### Scenario: DisconnectedEvent with missing optional fields

- **WHEN** a `DisconnectedEvent` is constructed with remote `false`, no statusCode, no reason
- **THEN** `getStatusCode()` returns `undefined`
- **AND** `getReason()` returns `undefined`

---

### Requirement: Lifecycle Events

The TypeScript Bot API SHALL provide `GameStartedEvent` (myId, initialPosition, gameSetup), `GameEndedEvent`
(numberOfRounds, results), `RoundStartedEvent` (roundNumber), and `RoundEndedEvent` (roundNumber, turnNumber, results).
These implement `IEvent` but do NOT extend `BotEvent`.

#### Scenario: GameStartedEvent fields

- **WHEN** a `GameStartedEvent` is constructed with myId `1`, an InitialPosition, and a GameSetup
- **THEN** `getMyId()` returns `1`
- **AND** `getInitialPosition()` returns the provided InitialPosition
- **AND** `getGameSetup()` returns the provided GameSetup

#### Scenario: GameEndedEvent fields

- **WHEN** a `GameEndedEvent` is constructed with numberOfRounds `10` and a BotResults
- **THEN** `getNumberOfRounds()` returns `10`
- **AND** `getResults()` returns the provided BotResults

#### Scenario: RoundEndedEvent fields

- **WHEN** a `RoundEndedEvent` is constructed with roundNumber `3`, turnNumber `150`, and a BotResults
- **THEN** `getRoundNumber()` returns `3`
- **AND** `getTurnNumber()` returns `150`
- **AND** `getResults()` returns the provided BotResults

---

### Requirement: Condition Class

The TypeScript Bot API SHALL provide a `Condition` class with an optional name and a `test()` method. The test method
SHALL be overridable by subclasses or settable via a callable function passed to the constructor.

#### Scenario: Condition with callable

- **WHEN** a `Condition` is constructed with a callable that returns `true`
- **THEN** `test()` returns `true`

#### Scenario: Condition with name

- **WHEN** a `Condition` is constructed with name `"LowEnergy"` and a callable
- **THEN** `getName()` returns `"LowEnergy"`

#### Scenario: Condition callable exception returns false

- **WHEN** a `Condition`'s callable throws an exception
- **THEN** `test()` returns `false`

---

### Requirement: NextTurnCondition

The TypeScript Bot API SHALL provide a `NextTurnCondition` that extends `Condition` and tests whether the current turn
number has advanced beyond the turn number at the time the condition was created.

#### Scenario: NextTurnCondition triggers on turn advance

- **WHEN** a `NextTurnCondition` is created at turn `5`
- **AND** the current turn advances to `6`
- **THEN** `test()` returns `true`

#### Scenario: NextTurnCondition does not trigger on same turn

- **WHEN** a `NextTurnCondition` is created at turn `5`
- **AND** the current turn is still `5`
- **THEN** `test()` returns `false`

---

### Requirement: Default Event Priorities

The TypeScript Bot API SHALL assign default priorities to all 15 bot event types with values identical to the Java
reference implementation. Higher values indicate higher priority.

#### Scenario: All 15 default priorities match Java

- **WHEN** the default event priorities are queried
- **THEN** WON_ROUND = `150`
- **AND** SKIPPED_TURN = `140`
- **AND** TICK = `130`
- **AND** CUSTOM = `120`
- **AND** TEAM_MESSAGE = `110`
- **AND** BOT_DEATH = `100`
- **AND** BULLET_HIT_WALL = `90`
- **AND** BULLET_HIT_BULLET = `80`
- **AND** BULLET_HIT_BOT = `70`
- **AND** BULLET_FIRED = `60`
- **AND** HIT_BY_BULLET = `50`
- **AND** HIT_WALL = `40`
- **AND** HIT_BOT = `30`
- **AND** SCANNED_BOT = `20`
- **AND** DEATH = `10`

#### Scenario: Custom priority overrides default

- **WHEN** `setPriority` is called for ScannedBotEvent with priority `200`
- **THEN** `getPriority` for ScannedBotEvent returns `200`

---

### Requirement: EventQueue Priority Dispatch

The TypeScript Bot API SHALL implement an `EventQueue` that dispatches events in priority order: critical events first,
then older events first, then higher priority first. The queue SHALL have a maximum size of 256 events.

#### Scenario: Events dispatch in priority order

- **WHEN** a ScannedBotEvent (priority 20) and a BulletFiredEvent (priority 60) are in the queue for the same turn
- **THEN** the BulletFiredEvent is dispatched before the ScannedBotEvent

#### Scenario: Critical events dispatch before non-critical

- **WHEN** a DeathEvent (critical, priority 10) and a BulletFiredEvent (non-critical, priority 60) are in the queue
- **THEN** the DeathEvent is dispatched first despite having lower priority value

#### Scenario: Old non-critical events are removed

- **WHEN** an event with turnNumber `5` exists in the queue
- **AND** `dispatchEvents` is called with turnNumber `8`
- **THEN** the event is removed from the queue (age > MAX_EVENT_AGE of 2)

#### Scenario: Old critical events are preserved

- **WHEN** a DeathEvent with turnNumber `5` exists in the queue
- **AND** `dispatchEvents` is called with turnNumber `8`
- **THEN** the DeathEvent is NOT removed (critical events survive aging)

#### Scenario: Queue size limit

- **WHEN** the queue already contains 256 events
- **AND** `addEvent` is called with another event
- **THEN** the new event is not added

---

### Requirement: Event Interruption

The TypeScript Bot API SHALL support event interruption semantics where a handler for a lower-priority event that has
been marked as interruptible can be interrupted when a higher-priority event of the same type arrives. Interruption
SHALL throw a `ThreadInterruptedException` equivalent to break out of the current handler.

#### Scenario: Interruptible event is interrupted by same-priority event

- **WHEN** an event handler for ScannedBotEvent is currently executing
- **AND** the handler has called `setEventInterruptible(true)` for ScannedBotEvent
- **AND** another ScannedBotEvent arrives with the same priority
- **THEN** the current handler is interrupted

#### Scenario: Non-interruptible event is not interrupted

- **WHEN** an event handler for ScannedBotEvent is currently executing
- **AND** `setEventInterruptible` has NOT been called
- **AND** another ScannedBotEvent arrives
- **THEN** the second event is skipped (not dispatched)

---

### Requirement: Custom Event Evaluation

The TypeScript Bot API SHALL evaluate all registered Conditions during each `dispatchEvents` call and add a
`CustomEvent` to the queue for each Condition whose `test()` returns `true`.

#### Scenario: Condition triggers CustomEvent

- **WHEN** a Condition named `"LowEnergy"` is registered via `addCustomEvent`
- **AND** the Condition's `test()` returns `true` during `dispatchEvents`
- **THEN** a `CustomEvent` is added to the queue with that Condition
- **AND** the `onCustomEvent` handler receives the CustomEvent

#### Scenario: Condition does not trigger when test is false

- **WHEN** a Condition's `test()` returns `false` during `dispatchEvents`
- **THEN** no CustomEvent is added for that Condition

---

### Requirement: Event Handler Registration

The TypeScript Bot API SHALL provide an `EventHandler<T>` class that supports subscribing listeners with optional
priority, unsubscribing, and publishing events to all listeners in priority order (highest first).

#### Scenario: Subscribers called in priority order

- **WHEN** subscriber A is registered with priority `1`
- **AND** subscriber B is registered with priority `10`
- **AND** an event is published
- **THEN** subscriber B is called before subscriber A

#### Scenario: Duplicate subscriber rejected

- **WHEN** the same subscriber function is registered twice
- **THEN** an error is thrown on the second registration

---

### Requirement: BotEventHandlers Dispatch Map

The TypeScript Bot API SHALL provide a `BotEventHandlers` class that maps each event type to its corresponding
`EventHandler` and dispatches events via a `fireEvent` method. It SHALL wire `IBaseBot` callback methods
(onTick, onScannedBot, onHitBot, onDeath, etc.) as default subscribers.

#### Scenario: fireEvent dispatches to correct handler

- **WHEN** `fireEvent` is called with a `ScannedBotEvent`
- **THEN** the `onScannedBot` handler is invoked with that event

#### Scenario: Unknown event type throws

- **WHEN** `fireEvent` is called with an unrecognized event type
- **THEN** an error is thrown

