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

### Requirement: BaseBot Connection and Lifecycle

The TypeScript `BaseBot` class SHALL connect to the game server via WebSocket, complete the server handshake, and block
on `start()` until the game ends and the connection closes. It SHALL delegate all internal state management to
`BaseBotInternals`, matching the Java reference implementation.

#### Scenario: Bot connects and completes handshake

- **WHEN** a developer calls `bot.start()`
- **THEN** a WebSocket connection is opened to the server URL
- **AND** a BotHandshake message is sent with the bot's info
- **AND** the bot receives a ServerHandshake in response
- **AND** `start()` blocks until the WebSocket connection closes

#### Scenario: Bot receives game setup on game start

- **WHEN** the server sends a GameStartedEventForBot message
- **THEN** `getMyId()` returns the bot's assigned ID
- **AND** `getGameType()`, `getArenaWidth()`, `getArenaHeight()`, `getNumberOfRounds()`, `getGunCoolingRate()`,
  `getMaxInactivityTurns()`, and `getTurnTimeout()` return the game setup values

#### Scenario: Bot state unavailable before game starts

- **WHEN** a developer calls `getMyId()` before the game has started
- **THEN** a `BotException` is thrown with a descriptive error message

---

### Requirement: Turn Execution via go()

The TypeScript `BaseBot.go()` method SHALL dispatch pending events for the current tick, send the accumulated BotIntent
to the server, and block until the next tick arrives. This matches the Java two-phase execution model.

#### Scenario: go() dispatches events then sends intent

- **WHEN** a bot calls `go()` during a tick
- **THEN** all pending events for the current turn are dispatched to event handlers in priority order
- **AND** the current BotIntent (turn rates, target speed, firepower) is sent to the server as JSON
- **AND** the method blocks until the next tick arrives from the server

#### Scenario: go() blocks via Atomics.wait

- **WHEN** `go()` is called on the bot worker thread
- **THEN** `BaseBotInternals.waitForNextTurn()` calls `Atomics.wait()` on a SharedArrayBuffer
- **AND** the worker thread is truly blocked (no busy-waiting, no Promises)
- **AND** the thread wakes when the main thread calls `Atomics.notify()` upon receiving the next tick

#### Scenario: Duplicate go() calls in same turn are idempotent

- **WHEN** `go()` is called multiple times for the same turn number
- **THEN** the BotIntent is sent only once
- **AND** subsequent calls block until the next turn arrives

---

### Requirement: Bot State Accessors

The TypeScript `BaseBot` SHALL expose all bot state accessors matching the Java `IBaseBot` interface, reading values
from the current tick event.

#### Scenario: Position and direction accessors

- **WHEN** a tick has been received
- **THEN** `getX()`, `getY()`, `getDirection()`, `getGunDirection()`, `getRadarDirection()` return the current values
- **AND** `getSpeed()` returns the current speed (positive = forward, negative = backward, zero = stopped)
- **AND** `getEnergy()` returns the current energy level
- **AND** `getGunHeat()` returns the current gun heat

#### Scenario: Turn and round number accessors

- **WHEN** a tick has been received
- **THEN** `getTurnNumber()` returns the current turn number
- **AND** `getRoundNumber()` returns the current round number
- **AND** `getEnemyCount()` returns the number of enemies remaining

---

### Requirement: Rate Setters and Max Rate Configuration

The TypeScript `BaseBot` SHALL provide rate setters (`setTurnRate`, `setGunTurnRate`, `setRadarTurnRate`,
`setTargetSpeed`) and max rate configuration (`setMaxTurnRate`, `setMaxGunTurnRate`, `setMaxRadarTurnRate`,
`setMaxSpeed`) that accumulate into a BotIntent sent on the next `go()` call.

#### Scenario: Setting turn rates

- **WHEN** a developer calls `setTurnRate(5)` and then `go()`
- **THEN** the BotIntent sent to the server contains `turnRate: 5`
- **AND** the bot turns 5 degrees per turn (up to `MAX_TURN_RATE`)

#### Scenario: Max rate clamping

- **WHEN** a developer calls `setMaxSpeed(4)` and then `setTargetSpeed(8)`
- **THEN** the actual target speed is clamped to `4`

#### Scenario: Multiple setters before go()

- **WHEN** a developer calls `setTurnRate(5)`, `setGunTurnRate(-10)`, `setTargetSpeed(8)`, and `setFire(2)` before `go()`
- **THEN** all four values are included in the single BotIntent sent to the server
- **AND** the bot moves, turns, turns gun, and fires in parallel on that turn

---

### Requirement: Adjustment Flags

The TypeScript `BaseBot` SHALL support gun-for-body, radar-for-body, and radar-for-gun adjustment flags matching the
Java reference implementation.

#### Scenario: Adjust gun for body turn

- **WHEN** a developer calls `setAdjustGunForBodyTurn(true)`
- **THEN** `isAdjustGunForBodyTurn()` returns `true`
- **AND** the gun turn rate is compensated for body rotation in the BotIntent

#### Scenario: Adjust radar for gun turn disables fire assist

- **WHEN** a developer calls `setAdjustRadarForGunTurn(true)`
- **THEN** `isAdjustRadarForGunTurn()` returns `true`
- **AND** fire assistance is automatically disabled

---

### Requirement: Stop and Resume (Setter)

The TypeScript `BaseBot` SHALL provide `setStop()` and `setResume()` methods that save and restore all movement state.

#### Scenario: setStop saves movement state

- **WHEN** a bot is moving forward with `setForward(100)` and turning with `setTurnLeft(90)`
- **AND** `setStop()` is called
- **THEN** `isStopped()` returns `true`
- **AND** all turn rates and target speed are set to zero in the BotIntent
- **AND** the remaining distances and turns are saved internally

#### Scenario: setResume restores movement state

- **WHEN** `setResume()` is called after `setStop()`
- **THEN** `isStopped()` returns `false`
- **AND** the previously saved turn rates, target speed, and remaining distances are restored

#### Scenario: setStop with overwrite

- **WHEN** `setStop(true)` is called while already stopped
- **THEN** the saved movement state is overwritten with the current (zero) movement

---

### Requirement: Custom Events

The TypeScript `BaseBot` SHALL support adding and removing custom event conditions that trigger `onCustomEvent` when
their test function returns `true`.

#### Scenario: Custom event fires when condition met

- **WHEN** a developer adds a custom event with `addCustomEvent(condition)` where `condition.test()` checks `getEnergy() < 50`
- **AND** the bot's energy drops below 50
- **THEN** `onCustomEvent` is called with a `CustomEvent` containing the condition

#### Scenario: Remove custom event

- **WHEN** a developer calls `removeCustomEvent(condition)` for a previously added condition
- **THEN** the condition is no longer evaluated
- **AND** `onCustomEvent` is no longer triggered for that condition

---

### Requirement: Event Handler Methods

The TypeScript `BaseBot` SHALL provide overridable event handler methods matching the Java `IBaseBot` interface. All
handlers fire on the bot worker thread during `dispatchEvents()`.

#### Scenario: Event handlers fire during go()

- **WHEN** the server sends a tick containing a `ScannedBotEvent`
- **AND** the bot calls `go()`
- **THEN** `onScannedBot(event)` is called on the bot worker thread before `go()` returns
- **AND** the event handler can call setter methods (e.g., `setFire(3)`) to take action on the same turn

#### Scenario: All event handlers available

- **WHEN** a developer extends `BaseBot`
- **THEN** the following methods are available for override: `onConnected`, `onDisconnected`, `onConnectionError`,
  `onGameStarted`, `onGameEnded`, `onRoundStarted`, `onRoundEnded`, `onTick`, `onBotDeath`, `onDeath`, `onHitBot`,
  `onHitWall`, `onBulletFired`, `onHitByBullet`, `onBulletHitBullet`, `onBulletHitWall`, `onScannedBot`,
  `onSkippedTurn`, `onCustomEvent`, `onTeamMessage`, `onWonRound`

---

### Requirement: Bot Blocking Movement Methods

The TypeScript `Bot` class SHALL provide blocking movement methods (`forward`, `back`, `turnLeft`, `turnRight`,
`turnGunLeft`, `turnGunRight`, `turnRadarLeft`, `turnRadarRight`) that block the bot worker thread until the movement
completes. The developer-facing API is fully synchronous -- no `async`/`await`.

#### Scenario: forward() blocks until distance traveled

- **WHEN** a developer calls `forward(100)`
- **THEN** `setForward(100)` is called internally
- **AND** the method repeatedly calls `go()` (blocking via Atomics.wait per turn)
- **AND** the method returns only when `getDistanceRemaining() === 0` and `getSpeed() === 0`

#### Scenario: back() negates distance

- **WHEN** a developer calls `back(100)`
- **THEN** it is equivalent to `forward(-100)`

#### Scenario: turnLeft() blocks until turn completed

- **WHEN** a developer calls `turnLeft(90)`
- **THEN** `setTurnLeft(90)` is called internally
- **AND** the method blocks until `getTurnRemaining() === 0`

#### Scenario: turnRight() negates degrees

- **WHEN** a developer calls `turnRight(90)`
- **THEN** it is equivalent to `turnLeft(-90)`

#### Scenario: turnGunLeft() blocks until gun turn completed

- **WHEN** a developer calls `turnGunLeft(360)`
- **THEN** `setTurnGunLeft(360)` is called internally
- **AND** the method blocks until `getGunTurnRemaining() === 0`

#### Scenario: turnRadarRight() blocks until radar turn completed

- **WHEN** a developer calls `turnRadarRight(180)`
- **THEN** it is equivalent to `turnRadarLeft(-180)`
- **AND** the method blocks until `getRadarTurnRemaining() === 0`

#### Scenario: Blocking method skips turn when stopped

- **WHEN** a bot has been stopped via `stop()`
- **AND** a blocking method like `forward(100)` is called
- **THEN** the method calls `go()` once and returns immediately without setting movement

---

### Requirement: Bot fire() Blocking Method

The TypeScript `Bot.fire()` method SHALL fire the gun and block for one turn, matching the Java reference.

#### Scenario: fire() sends firepower and advances one turn

- **WHEN** a developer calls `fire(2)`
- **THEN** `setFire(2)` is called internally
- **AND** `go()` is called to advance one turn
- **AND** the method returns after the turn completes

---

### Requirement: Bot stop() and resume() Blocking Methods

The TypeScript `Bot` class SHALL provide blocking `stop()` and `resume()` methods.

#### Scenario: stop() saves state and advances turn

- **WHEN** a developer calls `stop()`
- **THEN** `setStop(false)` is called internally
- **AND** `go()` is called to advance one turn

#### Scenario: resume() restores state and advances turn

- **WHEN** a developer calls `resume()`
- **THEN** `setResume()` is called internally
- **AND** `go()` is called to advance one turn

---

### Requirement: Bot waitFor(condition)

The TypeScript `Bot.waitFor()` method SHALL block the bot worker thread until the provided condition is met, calling
`go()` each turn to advance.

#### Scenario: waitFor blocks until condition is true

- **WHEN** a developer calls `waitFor(condition)` where `condition.test()` checks a custom state
- **THEN** the method calls `go()` in a loop
- **AND** it returns when `condition.test()` returns `true`
- **AND** it also returns if `isRunning()` becomes `false`

---

### Requirement: Bot rescan() Blocking Method

The TypeScript `Bot.rescan()` method SHALL trigger a radar rescan and advance one turn.

#### Scenario: rescan() rescans and advances turn

- **WHEN** a developer calls `rescan()`
- **THEN** `ScannedBotEvent` interruptibility is set to `true`
- **AND** `setRescan()` is called
- **AND** `go()` is called to advance one turn

---

### Requirement: Web Worker Threading Model

The TypeScript Bot API SHALL use Web Workers with SharedArrayBuffer and Atomics for the two-thread model defined in
ADR-0028. Bot code runs synchronously in a Worker; the WebSocket runs on the main thread.

#### Scenario: Bot worker thread starts on first tick

- **WHEN** the first tick of a round is received (turn 1)
- **THEN** a new Worker thread is spawned for the bot
- **AND** the Worker receives a SharedArrayBuffer for synchronization
- **AND** `bot.run()` is called on the Worker thread

#### Scenario: Worker blocks via Atomics.wait

- **WHEN** the bot calls `go()` on the Worker thread
- **THEN** `Atomics.wait()` is called on the SharedArrayBuffer synchronization slot
- **AND** the Worker thread is truly blocked (CPU idle, no polling)
- **AND** the Worker wakes when the main thread calls `Atomics.notify()`

#### Scenario: Worker stops via stop flag

- **WHEN** the main thread needs to stop the bot (round ended, death, game ended)
- **THEN** a stop flag is set in the SharedArrayBuffer
- **AND** `Atomics.notify()` is called to wake the Worker
- **AND** the Worker detects the stop flag and throws `BotStoppedException`
- **AND** `dispatchFinalTurnEvents()` is called before the Worker exits

---

### Requirement: Bot Lifecycle Across Rounds

The TypeScript Bot API SHALL correctly manage the bot Worker thread across round transitions, matching the Java
reference implementation's thread lifecycle.

#### Scenario: New round starts new Worker

- **WHEN** a new round begins (turn 1 of round N+1)
- **THEN** any existing bot Worker is stopped
- **AND** remaining values (distanceRemaining, turnRemaining, etc.) are cleared
- **AND** a new Worker is spawned
- **AND** `bot.run()` is called in the new Worker

#### Scenario: Round end stops Worker

- **WHEN** a `RoundEndedEvent` is received
- **THEN** `stopThread()` is called
- **AND** `isRunning()` is set to `false` before the Worker is interrupted
- **AND** `enableEventHandling(false)` is called on the main thread (not the Worker) to prevent race conditions

#### Scenario: Death stops Worker

- **WHEN** the bot dies (DeathEvent received)
- **THEN** the bot Worker is stopped
- **AND** `dispatchFinalTurnEvents()` ensures the DeathEvent is delivered to the bot's event handler

#### Scenario: Game end stops Worker and unblocks start()

- **WHEN** a `GameEndedEvent` is received
- **THEN** the bot Worker is stopped
- **AND** the WebSocket connection closes
- **AND** `start()` unblocks and returns

---

### Requirement: Distance and Turn Remaining Tracking

The TypeScript `BotInternals` SHALL track remaining distances and turns per tick, using the same update logic as the
Java reference implementation.

#### Scenario: Distance remaining decreases each tick

- **WHEN** `setForward(100)` has been called
- **AND** the bot moves 8 units in a tick
- **THEN** `getDistanceRemaining()` decreases by the distance traveled
- **AND** when `getDistanceRemaining()` reaches 0, the bot stops moving

#### Scenario: Turn remaining decreases each tick

- **WHEN** `setTurnLeft(90)` has been called
- **AND** the bot turns 10 degrees in a tick
- **THEN** `getTurnRemaining()` decreases by the degrees turned
- **AND** when `getTurnRemaining()` reaches 0, the bot stops turning

#### Scenario: Movement reset on hit wall

- **WHEN** the bot hits a wall
- **THEN** `getDistanceRemaining()` is set to 0

#### Scenario: Movement reset on ram

- **WHEN** the bot rams another bot (HitBotEvent with `isRammed() === true`)
- **THEN** `getDistanceRemaining()` is set to 0

---

### Requirement: Final Tick Event Delivery

The TypeScript Bot API SHALL ensure final-tick events (WonRoundEvent, DeathEvent, SkippedTurnEvent) are always
dispatched, even when the bot thread is being stopped. This matches the fix documented for the Java, C#, and Python APIs.

#### Scenario: dispatchFinalTurnEvents after run() exits

- **WHEN** `bot.run()` exits (normally or via BotStoppedException)
- **THEN** `dispatchFinalTurnEvents()` is called
- **AND** any WonRoundEvent or DeathEvent in the current tick's event queue is dispatched to the bot's event handlers

#### Scenario: dispatchFinalTurnEvents after idle loop exits

- **WHEN** the idle `go()` loop exits after `run()` has completed
- **THEN** `dispatchFinalTurnEvents()` is called again
- **AND** events that arrived during the idle phase are dispatched

---

### Requirement: BotInternals Stop and Resume State

The TypeScript `BotInternals` SHALL implement the `IStopResumeListener` interface to save and restore all movement
state on stop/resume.

#### Scenario: Stop saves all remaining values

- **WHEN** `onStop()` is called
- **THEN** `distanceRemaining`, `turnRemaining`, `gunTurnRemaining`, `radarTurnRemaining` are saved
- **AND** previous direction values (body, gun, radar) are saved

#### Scenario: Resume restores all remaining values

- **WHEN** `onResume()` is called
- **THEN** all values saved by `onStop()` are restored
- **AND** movement continues from where it was interrupted

---

### Requirement: Sample Bot Runtime Distribution

The TypeScript sample bot archive SHALL include a `deps/` folder containing all runtime
prerequisites so that bots can be started directly from `.ts` source without any pre-compilation
step. The `deps/` folder SHALL contain:

- `install-dependencies.cmd` and `install-dependencies.sh` — platform-specific installers
- `robocode.dev-tank-royale-bot-api-X.Y.Z.tgz` — the bot API npm tarball (produced by `npm pack`)
- `package.json` — specifying `@robocode.dev/tank-royale-bot-api` (file reference to local tarball)
  and `tsx` as runtime dependencies

#### Scenario: Deps folder is present after Gradle build

- **WHEN** `./gradlew :sample-bots:typescript:build` completes
- **THEN** `build/archive/deps/` exists and contains `install-dependencies.cmd`,
  `install-dependencies.sh`, `package.json`, and the bot API `.tgz` tarball

#### Scenario: install-dependencies script is idempotent

- **WHEN** `install-dependencies.cmd` (or `.sh`) is run more than once from the same `deps/` folder
- **THEN** only the first run performs `npm install`; subsequent runs exit immediately because `.deps_installed` marker exists

#### Scenario: install-dependencies handles concurrent launch

- **WHEN** two bots are started simultaneously from the same archive
- **THEN** only one `npm install` runs; the other waits on the `.deps_lock` mutex directory

#### Scenario: install-dependencies fails clearly when Node.js is absent

- **WHEN** `node` is not on the PATH
- **THEN** the script prints an actionable error message and exits with a non-zero code

---

### Requirement: Sample Bot Launch via tsx

TypeScript sample bot launch scripts SHALL execute the bot directly from its `.ts` source file
using the locally installed `tsx` binary from `deps/node_modules/.bin/tsx`. No pre-compilation
to JavaScript SHALL be required.

#### Scenario: Windows bot launch

- **WHEN** `MyFirstBot.cmd` is executed on Windows
- **THEN** `install-dependencies.cmd` is called first
- **AND** the bot is started with `..\deps\node_modules\.bin\tsx MyFirstBot.ts`

#### Scenario: Unix bot launch

- **WHEN** `MyFirstBot.sh` is executed on Linux or macOS
- **THEN** `install-dependencies.sh` is called first
- **AND** the bot is started with `../deps/node_modules/.bin/tsx MyFirstBot.ts`

#### Scenario: Bot connects to Robocode server after launch

- **WHEN** a TypeScript sample bot is started via its launch script
- **THEN** it connects to the Robocode server using the `@robocode.dev/tank-royale-bot-api`
  installed in `deps/node_modules/`

