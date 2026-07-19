---
id: CAP-011-criteria
type: criteria
status: draft
links: [CAP-011]
title: Acceptance criteria for CAP-011 (typescript-bot-api)
ac-prefix: TBA
provenance: inferred
---

```gherkin
Feature: typescript-bot-api — TBD - created by archiving change add-typescript-protocol-layer-2. Update Purpose after archive.

  # Requirement: Protocol DTOs
  # The TypeScript Bot API SHALL provide protocol DTO interfaces matching the server JSON schema for all messages exchanged
  # between bot and server. DTOs SHALL use TypeScript interfaces (not classes) with camelCase field names matching the wire
  # format exactly.

  @TBA-001
  Scenario: MessageType enum covers all server message types
    When a developer accesses `MessageType.TICK_EVENT_FOR_BOT`
    Then the value is `"TickEventForBot"`
    And all message type string values match Java's `Message.Type` enum exactly

  @TBA-002
  Scenario: BotHandshake DTO has all required fields
    When a developer constructs a `BotHandshake` object
    Then it contains fields: `type`, `sessionId`, `name`, `version`, `authors`, `description`, `homepage`, `countryCodes`, `gameTypes`, `platform`, `programmingLang`, `initialPosition`, `teamId`, `teamName`, `teamVersion`, `isDroid`, `secret`
    And field names match the server JSON schema exactly

  @TBA-003
  Scenario: ServerHandshake DTO has all required fields
    When a `ServerHandshake` message is received
    Then it contains fields: `type`, `sessionId`, `name`, `variant`, `version`, `gameTypes`, `gameSetup`

  @TBA-004
  Scenario: BotIntent DTO has all fields
    When a developer constructs a `BotIntent` object
    Then it contains fields: `type`, `turnRate`, `gunTurnRate`, `radarTurnRate`, `targetSpeed`, `firepower`, `adjustGunForBodyTurn`, `adjustRadarForBodyTurn`, `adjustRadarForGunTurn`, `rescan`, `fireAssist`, `bodyColor`, `turretColor`, `radarColor`, `bulletColor`, `scanColor`, `tracksColor`, `gunColor`, `stdOut`, `stdErr`, `teamMessages`, `debugGraphics`

  @TBA-005
  Scenario: TickEventForBot DTO contains bot state and events
    When a `TickEventForBot` message is received
    Then it contains `turnNumber`, `roundNumber`, `botState`, `bulletStates`, and `events`
    And each element in `events` has a `type` field for discriminated deserialization

  @TBA-006
  Scenario: In-tick event DTOs match server schema
    When a `TickEventForBot` message contains a `BotDeathEvent`
    Then the event has `turnNumber` and `victimId` fields
    And all 11 in-tick event types (`BotDeathEvent`, `BotHitBotEvent`, `BotHitWallEvent`, `BulletFiredEvent`, `BulletHitBotEvent`, `BulletHitBulletEvent`, `BulletHitWallEvent`, `ScannedBotEvent`, `SkippedTurnEvent`, `WonRoundEvent`, `TeamMessageEvent`) have fields matching the Java schema DTOs
    # ---

  # Requirement: RuntimeAdapter
  # The TypeScript Bot API SHALL provide a `RuntimeAdapter` interface that abstracts runtime-specific APIs (WebSocket
  # creation, environment variable access, process exit) per ADR-0029. Implementations SHALL be provided for Node.js and
  # browser environments, with automatic runtime detection.

  @TBA-007
  Scenario: Node.js runtime adapter creates WebSocket via ws library
    When `NodeRuntimeAdapter.createWebSocket(url)` is called
    Then a `ws` library WebSocket instance is returned
    And the instance implements the `WebSocketLike` interface

  @TBA-008
  Scenario: Browser runtime adapter creates native WebSocket
    When `BrowserRuntimeAdapter.createWebSocket(url)` is called
    Then a native browser `WebSocket` instance is returned

  @TBA-009
  Scenario: Node.js runtime adapter reads environment variables
    When `NodeRuntimeAdapter.getEnvVar("SERVER_URL")` is called
    Then the value of `process.env.SERVER_URL` is returned

  @TBA-010
  Scenario: Browser runtime adapter returns undefined for environment variables
    When `BrowserRuntimeAdapter.getEnvVar("SERVER_URL")` is called
    Then `undefined` is returned

  @TBA-011
  Scenario: Automatic runtime detection
    When `detectRuntime()` is called in a Node.js environment
    Then a `NodeRuntimeAdapter` instance is returned
    When `detectRuntime()` is called in a browser environment
    Then a `BrowserRuntimeAdapter` instance is returned
    # ---

  # Requirement: EnvVars
  # The TypeScript Bot API SHALL provide an `EnvVars` utility that reads bot configuration from environment variables using
  # `RuntimeAdapter.getEnvVar()`. Variable names SHALL match the Java reference implementation exactly.

  @TBA-012
  Scenario: EnvVars reads server URL
    When the `SERVER_URL` environment variable is set to `"ws://localhost:7654"`
    Then `EnvVars.getServerUrl()` returns `"ws://localhost:7654"`

  @TBA-013
  Scenario: EnvVars reads comma-separated authors
    When the `BOT_AUTHORS` environment variable is set to `"Alice, Bob, Charlie"`
    Then `EnvVars.getBotAuthors()` returns `["Alice", "Bob", "Charlie"]`
    And whitespace around commas is trimmed (matching Java's `split("\\s*,\\s*")`)

  @TBA-014
  Scenario: EnvVars constructs BotInfo from environment
    When `BOT_NAME`, `BOT_VERSION`, and `BOT_AUTHORS` are set
    Then `EnvVars.getBotInfo()` returns a valid `BotInfo` instance

  @TBA-015
  Scenario: EnvVars throws on missing required fields
    When `BOT_NAME` is not set
    And `EnvVars.getBotInfo()` is called
    Then a `BotException` is thrown with message `"Missing environment variable: BOT_NAME"`

  @TBA-016
  Scenario: EnvVars reads all 16 environment variable names
    When environment variables are configured
    Then `EnvVars` supports exactly these names: `SERVER_URL`, `SERVER_SECRET`, `BOT_NAME`, `BOT_VERSION`, `BOT_AUTHORS`, `BOT_DESCRIPTION`, `BOT_HOMEPAGE`, `BOT_COUNTRY_CODES`, `BOT_GAME_TYPES`, `BOT_PLATFORM`, `BOT_PROG_LANG`, `BOT_INITIAL_POS`, `TEAM_ID`, `TEAM_NAME`, `TEAM_VERSION`, `BOT_BOOTED`
    # ---

  # Requirement: WebSocket Handler
  # The TypeScript Bot API SHALL provide a `WebSocketHandler` that manages the WebSocket connection to the Tank Royale
  # server, routes incoming messages by type, and sends outgoing messages. The handler SHALL use `RuntimeAdapter` for
  # WebSocket creation to support both Node.js and browser environments.

  @TBA-017
  Scenario: WebSocket handler connects to server
    When `WebSocketHandler.connect()` is called with a server URL
    Then a WebSocket connection is established via `RuntimeAdapter.createWebSocket()`
    And a `ConnectedEvent` is fired on successful connection

  @TBA-018
  Scenario: WebSocket handler routes messages by type
    When a JSON message with `"type": "TickEventForBot"` is received
    Then the `handleTick` callback is invoked
    And messages with types `ServerHandshake`, `GameStartedEventForBot`, `GameEndedEventForBot`, `RoundStartedEvent`, `RoundEndedEventForBot`, `SkippedTurnEvent`, `GameAbortedEvent` are each routed to their respective handlers

  @TBA-019
  Scenario: WebSocket handler replies to server handshake
    When a `ServerHandshake` message is received
    Then the handler stores the server handshake data
    And replies with a `BotHandshake` message containing the bot's identity and configuration

  @TBA-020
  Scenario: WebSocket handler sends BotReady after game start
    When a `GameStartedEventForBot` message is received
    Then the handler extracts `myId`, `teammateIds`, `gameSetup`, and initial position
    And sends a `BotReady` message to the server

  @TBA-021
  Scenario: WebSocket handler sends BotIntent
    When `sendBotIntent(intent)` is called
    Then the intent is serialized to JSON and sent via the WebSocket

  @TBA-022
  Scenario: WebSocket handler fires disconnect event
    When the WebSocket connection closes
    Then a `DisconnectedEvent` is fired with the status code and reason

  @TBA-023
  Scenario: WebSocket handler fires connection error event
    When a WebSocket error occurs
    Then a `ConnectionErrorEvent` is fired
    # ---

  # Requirement: Mappers
  # The TypeScript Bot API SHALL provide mapper functions that transform protocol DTOs (schema types) into API-layer objects
  # (value objects and events). Mappers SHALL have 1:1 correspondence with the Java reference implementation.

  @TBA-024
  Scenario: EventMapper maps TickEventForBot to TickEvent
    When `EventMapper.map(tickEventForBot, myBotId)` is called
    Then the result is a `TickEvent` with mapped `botState`, `bulletStates`, and `events`

  @TBA-025
  Scenario: EventMapper distinguishes self-death from opponent death
    When a `BotDeathEvent` has `victimId` equal to `myBotId`
    Then it is mapped to a `DeathEvent`
    When a `BotDeathEvent` has `victimId` not equal to `myBotId`
    Then it is mapped to a `BotDeathEvent` (opponent death)

  @TBA-026
  Scenario: EventMapper distinguishes hit-by-bullet from bullet-hit-bot
    When a `BulletHitBotEvent` has `victimId` equal to `myBotId`
    Then it is mapped to a `HitByBulletEvent` with bullet, damage, and energy
    When a `BulletHitBotEvent` has `victimId` not equal to `myBotId`
    Then it is mapped to a `BulletHitBotEvent` with victimId, bullet, damage, and energy

  @TBA-027
  Scenario: BotStateMapper converts all fields including colors
    When `BotStateMapper.map(schemaBotState)` is called
    Then all 22 fields are mapped including 7 color fields converted via `ColorUtil.fromHexColor()`
    And null color hex strings result in null/undefined Color values

  @TBA-028
  Scenario: BulletStateMapper maps single and collection
    When `BulletStateMapper.map(schemaBulletState)` is called with a single bullet
    Then a `BulletState` is returned with `bulletId`, `ownerId`, `power`, `x`, `y`, `direction`, and `color`
    When `BulletStateMapper.map(bulletStates)` is called with a collection
    Then a Set of mapped `BulletState` objects is returned

  @TBA-029
  Scenario: GameSetupMapper maps all fields
    When `GameSetupMapper.map(schemaGameSetup)` is called
    Then a `GameSetup` is returned with `gameType`, `arenaWidth`, `arenaHeight`, `numberOfRounds`, `gunCoolingRate`, `maxInactivityTurns`, `turnTimeout`, `readyTimeout`

  @TBA-030
  Scenario: ResultsMapper maps all score fields
    When `ResultsMapper.map(resultsForBot)` is called
    Then a `BotResults` is returned with `rank`, `survival`, `lastSurvivorBonus`, `bulletDamage`, `bulletKillBonus`, `ramDamage`, `ramKillBonus`, `totalScore`, `firstPlaces`, `secondPlaces`, `thirdPlaces`

  @TBA-031
  Scenario: InitialPositionMapper maps API to schema format
    When `InitialPositionMapper.map(initialPosition)` is called with a non-null position
    Then a schema `InitialPosition` is returned with `x`, `y`, `direction`
    When `InitialPositionMapper.map(null)` is called
    Then `null` is returned
    # ---

  # Requirement: JSON Utilities
  # The TypeScript Bot API SHALL provide JSON serialization utilities for protocol messages, including type-discriminated
  # deserialization of event arrays within `TickEventForBot` messages.

  @TBA-032
  Scenario: Type-discriminated event deserialization
    When a JSON event array contains `{"type": "BotDeathEvent", "turnNumber": 42, "victimId": 3}`
    Then the deserialized object is a `BotDeathEvent` with `turnNumber = 42` and `victimId = 3`
    And all 11 event types are correctly deserialized based on their `type` field

  @TBA-033
  Scenario: JSON round-trip for BotIntent
    When a `BotIntent` is serialized to JSON and deserialized back
    Then all fields are preserved exactly

  @TBA-034
  Scenario: Color serialization uses hex format
    When a Color value is serialized for a protocol message
    Then it is written as a hex string (e.g., `"#ff0000"`)
    When a hex color string is deserialized
    Then a Color object with correct R, G, B values is returned

  # Requirement: Bot Event Base Class
  # The TypeScript Bot API SHALL provide a `BotEvent` abstract class that serves as the base for all in-game events, with a
  # `turnNumber` property and an `isCritical()` method that defaults to `false`.

  @TBA-035
  Scenario: BotEvent carries turn number
    When a BotEvent subclass is constructed with turnNumber `42`
    Then `getTurnNumber()` returns `42`

  @TBA-036
  Scenario: BotEvent is not critical by default
    When a non-critical BotEvent subclass is constructed
    Then `isCritical()` returns `false`
    # ---

  # Requirement: Critical Events
  # The TypeScript Bot API SHALL mark `DeathEvent`, `WonRoundEvent`, and `SkippedTurnEvent` as critical events. Critical
  # events are never removed from the event queue due to age.

  @TBA-037
  Scenario: DeathEvent is critical
    When a `DeathEvent` is constructed
    Then `isCritical()` returns `true`

  @TBA-038
  Scenario: WonRoundEvent is critical
    When a `WonRoundEvent` is constructed
    Then `isCritical()` returns `true`

  @TBA-039
  Scenario: SkippedTurnEvent is critical
    When a `SkippedTurnEvent` is constructed
    Then `isCritical()` returns `true`
    # ---

  # Requirement: TickEvent
  # The TypeScript Bot API SHALL provide a `TickEvent` class containing the round number, bot state, bullet states, and
  # nested events for the current turn.

  @TBA-040
  Scenario: TickEvent fields
    When a `TickEvent` is constructed with roundNumber `3`, a BotState, a list of BulletStates, and a list of BotEvents
    Then `getRoundNumber()` returns `3`
    And `getBotState()` returns the provided BotState
    And `getBulletStates()` returns the provided BulletState collection
    And `getEvents()` returns the provided BotEvent collection
    # ---

  # Requirement: ScannedBotEvent
  # The TypeScript Bot API SHALL provide a `ScannedBotEvent` with fields: scannedByBotId, scannedBotId, energy, x, y,
  # direction, and speed, all matching the Java reference implementation.

  @TBA-041
  Scenario: ScannedBotEvent fields
    When a `ScannedBotEvent` is constructed with scannedByBotId `1`, scannedBotId `2`, energy `78.5`, x `300`, y `400`, direction `45.0`, speed `6.0`
    Then all getter methods return the corresponding values
    # ---

  # Requirement: Collision and Wall Events
  # The TypeScript Bot API SHALL provide `HitBotEvent` (victimId, energy, x, y, isRammed), `HitWallEvent` (turnNumber only),
  # and `HitByBulletEvent` (bullet, damage, energy) with fields matching the Java reference implementation.

  @TBA-042
  Scenario: HitBotEvent with ram
    When a `HitBotEvent` is constructed with victimId `5`, energy `60.0`, x `100`, y `200`, isRammed `true`
    Then `getVictimId()` returns `5`
    And `isRammed()` returns `true`

  @TBA-043
  Scenario: HitByBulletEvent fields
    When a `HitByBulletEvent` is constructed with a BulletState, damage `12.0`, energy `88.0`
    Then `getBullet()` returns the provided BulletState
    And `getDamage()` returns `12.0`
    And `getEnergy()` returns `88.0`

  @TBA-044
  Scenario: HitWallEvent has no extra fields
    When a `HitWallEvent` is constructed with turnNumber `10`
    Then `getTurnNumber()` returns `10`
    And no additional fields exist beyond those inherited from BotEvent
    # ---

  # Requirement: Bullet Events
  # The TypeScript Bot API SHALL provide `BulletFiredEvent` (bullet), `BulletHitBotEvent` (victimId, bullet, damage, energy),
  # `BulletHitBulletEvent` (bullet, hitBullet), and `BulletHitWallEvent` (bullet) with fields matching the Java reference
  # implementation.

  @TBA-045
  Scenario: BulletFiredEvent carries bullet state
    When a `BulletFiredEvent` is constructed with a BulletState
    Then `getBullet()` returns that BulletState

  @TBA-046
  Scenario: BulletHitBotEvent fields
    When a `BulletHitBotEvent` is constructed with victimId `3`, a BulletState, damage `8.0`, energy `92.0`
    Then `getVictimId()` returns `3`
    And `getBullet()` returns the provided BulletState
    And `getDamage()` returns `8.0`

  @TBA-047
  Scenario: BulletHitBulletEvent carries both bullets
    When a `BulletHitBulletEvent` is constructed with two BulletState instances
    Then `getBullet()` returns the firing bullet
    And `getHitBullet()` returns the other bullet
    # ---

  # Requirement: Death and Victory Events
  # The TypeScript Bot API SHALL provide `BotDeathEvent` (victimId), `DeathEvent` (turnNumber only), and `WonRoundEvent`
  # (turnNumber only) with fields and criticality matching the Java reference implementation.

  @TBA-048
  Scenario: BotDeathEvent carries victim ID
    When a `BotDeathEvent` is constructed with victimId `7`
    Then `getVictimId()` returns `7`

  @TBA-049
  Scenario: DeathEvent and WonRoundEvent have no extra fields
    When a `DeathEvent` or `WonRoundEvent` is constructed with turnNumber `50`
    Then `getTurnNumber()` returns `50`
    And `isCritical()` returns `true`
    # ---

  # Requirement: Team Message Event
  # The TypeScript Bot API SHALL provide a `TeamMessageEvent` with fields message and senderId. Construction with a null or
  # undefined message SHALL throw an error.

  @TBA-050
  Scenario: TeamMessageEvent fields
    When a `TeamMessageEvent` is constructed with message `"attack"` and senderId `2`
    Then `getMessage()` returns `"attack"`
    And `getSenderId()` returns `2`

  @TBA-051
  Scenario: TeamMessageEvent rejects null message
    When a `TeamMessageEvent` is constructed with a null message
    Then an error is thrown
    # ---

  # Requirement: Connection Events
  # The TypeScript Bot API SHALL provide `ConnectedEvent`, `DisconnectedEvent` (remote, statusCode?, reason?), and
  # `ConnectionErrorEvent` (error), all extending a `ConnectionEvent` base class with a serverUri property.

  @TBA-052
  Scenario: ConnectedEvent carries server URI
    When a `ConnectedEvent` is constructed with serverUri `"ws://localhost:7654"`
    Then `getServerUri()` returns `"ws://localhost:7654"`

  @TBA-053
  Scenario: DisconnectedEvent optional fields
    When a `DisconnectedEvent` is constructed with remote `true`, statusCode `1000`, reason `"Normal closure"`
    Then `isRemote()` returns `true`
    And `getStatusCode()` returns `1000`
    And `getReason()` returns `"Normal closure"`

  @TBA-054
  Scenario: DisconnectedEvent with missing optional fields
    When a `DisconnectedEvent` is constructed with remote `false`, no statusCode, no reason
    Then `getStatusCode()` returns `undefined`
    And `getReason()` returns `undefined`
    # ---

  # Requirement: Lifecycle Events
  # The TypeScript Bot API SHALL provide `GameStartedEvent` (myId, initialPosition, gameSetup), `GameEndedEvent`
  # (numberOfRounds, results), `RoundStartedEvent` (roundNumber), and `RoundEndedEvent` (roundNumber, turnNumber, results).
  # These implement `IEvent` but do NOT extend `BotEvent`.

  @TBA-055
  Scenario: GameStartedEvent fields
    When a `GameStartedEvent` is constructed with myId `1`, an InitialPosition, and a GameSetup
    Then `getMyId()` returns `1`
    And `getInitialPosition()` returns the provided InitialPosition
    And `getGameSetup()` returns the provided GameSetup

  @TBA-056
  Scenario: GameEndedEvent fields
    When a `GameEndedEvent` is constructed with numberOfRounds `10` and a BotResults
    Then `getNumberOfRounds()` returns `10`
    And `getResults()` returns the provided BotResults

  @TBA-057
  Scenario: RoundEndedEvent fields
    When a `RoundEndedEvent` is constructed with roundNumber `3`, turnNumber `150`, and a BotResults
    Then `getRoundNumber()` returns `3`
    And `getTurnNumber()` returns `150`
    And `getResults()` returns the provided BotResults
    # ---

  # Requirement: Condition Class
  # The TypeScript Bot API SHALL provide a `Condition` class with an optional name and a `test()` method. The test method
  # SHALL be overridable by subclasses or settable via a callable function passed to the constructor.

  @TBA-058
  Scenario: Condition with callable
    When a `Condition` is constructed with a callable that returns `true`
    Then `test()` returns `true`

  @TBA-059
  Scenario: Condition with name
    When a `Condition` is constructed with name `"LowEnergy"` and a callable
    Then `getName()` returns `"LowEnergy"`

  @TBA-060
  Scenario: Condition callable exception returns false
    When a `Condition`'s callable throws an exception
    Then `test()` returns `false`
    # ---

  # Requirement: NextTurnCondition
  # The TypeScript Bot API SHALL provide a `NextTurnCondition` that extends `Condition` and tests whether the current turn
  # number has advanced beyond the turn number at the time the condition was created.

  @TBA-061
  Scenario: NextTurnCondition triggers on turn advance
    When a `NextTurnCondition` is created at turn `5`
    And the current turn advances to `6`
    Then `test()` returns `true`

  @TBA-062
  Scenario: NextTurnCondition does not trigger on same turn
    When a `NextTurnCondition` is created at turn `5`
    And the current turn is still `5`
    Then `test()` returns `false`
    # ---

  # Requirement: Default Event Priorities
  # The TypeScript Bot API SHALL assign default priorities to all 15 bot event types with values identical to the Java
  # reference implementation. Higher values indicate higher priority.

  @TBA-063
  Scenario: All 15 default priorities match Java
    When the default event priorities are queried
    Then WON_ROUND = `150`
    And SKIPPED_TURN = `140`
    And TICK = `130`
    And CUSTOM = `120`
    And TEAM_MESSAGE = `110`
    And BOT_DEATH = `100`
    And BULLET_HIT_WALL = `90`
    And BULLET_HIT_BULLET = `80`
    And BULLET_HIT_BOT = `70`
    And BULLET_FIRED = `60`
    And HIT_BY_BULLET = `50`
    And HIT_WALL = `40`
    And HIT_BOT = `30`
    And SCANNED_BOT = `20`
    And DEATH = `10`

  @TBA-064
  Scenario: Custom priority overrides default
    When `setPriority` is called for ScannedBotEvent with priority `200`
    Then `getPriority` for ScannedBotEvent returns `200`
    # ---

  # Requirement: EventQueue Priority Dispatch
  # The TypeScript Bot API SHALL implement an `EventQueue` that dispatches events in priority order: critical events first,
  # then older events first, then higher priority first. The queue SHALL have a maximum size of 256 events.

  @TBA-065
  Scenario: Events dispatch in priority order
    When a ScannedBotEvent (priority 20) and a BulletFiredEvent (priority 60) are in the queue for the same turn
    Then the BulletFiredEvent is dispatched before the ScannedBotEvent

  @TBA-066
  Scenario: Critical events dispatch before non-critical
    When a DeathEvent (critical, priority 10) and a BulletFiredEvent (non-critical, priority 60) are in the queue
    Then the DeathEvent is dispatched first despite having lower priority value

  @TBA-067
  Scenario: Old non-critical events are removed
    When an event with turnNumber `5` exists in the queue
    And `dispatchEvents` is called with turnNumber `8`
    Then the event is removed from the queue (age > MAX_EVENT_AGE of 2)

  @TBA-068
  Scenario: Old critical events are preserved
    When a DeathEvent with turnNumber `5` exists in the queue
    And `dispatchEvents` is called with turnNumber `8`
    Then the DeathEvent is NOT removed (critical events survive aging)

  @TBA-069
  Scenario: Queue size limit
    When the queue already contains 256 events
    And `addEvent` is called with another event
    Then the new event is not added
    # ---

  # Requirement: Event Interruption
  # The TypeScript Bot API SHALL support event interruption semantics where a handler for a lower-priority event that has
  # been marked as interruptible can be interrupted when a higher-priority event of the same type arrives. Interruption
  # SHALL throw a `ThreadInterruptedException` equivalent to break out of the current handler.

  @TBA-070
  Scenario: Interruptible event is interrupted by same-priority event
    When an event handler for ScannedBotEvent is currently executing
    And the handler has called `setEventInterruptible(true)` for ScannedBotEvent
    And another ScannedBotEvent arrives with the same priority
    Then the current handler is interrupted

  @TBA-071
  Scenario: Non-interruptible event is not interrupted
    When an event handler for ScannedBotEvent is currently executing
    And `setEventInterruptible` has NOT been called
    And another ScannedBotEvent arrives
    Then the second event is skipped (not dispatched)
    # ---

  # Requirement: Custom Event Evaluation
  # The TypeScript Bot API SHALL evaluate all registered Conditions during each `dispatchEvents` call and add a
  # `CustomEvent` to the queue for each Condition whose `test()` returns `true`.

  @TBA-072
  Scenario: Condition triggers CustomEvent
    When a Condition named `"LowEnergy"` is registered via `addCustomEvent`
    And the Condition's `test()` returns `true` during `dispatchEvents`
    Then a `CustomEvent` is added to the queue with that Condition
    And the `onCustomEvent` handler receives the CustomEvent

  @TBA-073
  Scenario: Condition does not trigger when test is false
    When a Condition's `test()` returns `false` during `dispatchEvents`
    Then no CustomEvent is added for that Condition
    # ---

  # Requirement: Event Handler Registration
  # The TypeScript Bot API SHALL provide an `EventHandler<T>` class that supports subscribing listeners with optional
  # priority, unsubscribing, and publishing events to all listeners in priority order (highest first).

  @TBA-074
  Scenario: Subscribers called in priority order
    When subscriber A is registered with priority `1`
    And subscriber B is registered with priority `10`
    And an event is published
    Then subscriber B is called before subscriber A

  @TBA-075
  Scenario: Duplicate subscriber rejected
    When the same subscriber function is registered twice
    Then an error is thrown on the second registration
    # ---

  # Requirement: BotEventHandlers Dispatch Map
  # The TypeScript Bot API SHALL provide a `BotEventHandlers` class that maps each event type to its corresponding
  # `EventHandler` and dispatches events via a `fireEvent` method. It SHALL wire `IBaseBot` callback methods
  # (onTick, onScannedBot, onHitBot, onDeath, etc.) as default subscribers.

  @TBA-076
  Scenario: fireEvent dispatches to correct handler
    When `fireEvent` is called with a `ScannedBotEvent`
    Then the `onScannedBot` handler is invoked with that event

  @TBA-077
  Scenario: Unknown event type throws
    When `fireEvent` is called with an unrecognized event type
    Then an error is thrown

  # Requirement: BaseBot Connection and Lifecycle
  # The TypeScript `BaseBot` class SHALL connect to the game server via WebSocket, complete the server handshake, and block
  # on `start()` until the game ends and the connection closes. It SHALL delegate all internal state management to
  # `BaseBotInternals`, matching the Java reference implementation.

  @TBA-078
  Scenario: Bot connects and completes handshake
    When a developer calls `bot.start()`
    Then a WebSocket connection is opened to the server URL
    And a BotHandshake message is sent with the bot's info
    And the bot receives a ServerHandshake in response
    And `start()` blocks until the WebSocket connection closes

  @TBA-079
  Scenario: Bot receives game setup on game start
    When the server sends a GameStartedEventForBot message
    Then `getMyId()` returns the bot's assigned ID
    And `getGameType()`, `getArenaWidth()`, `getArenaHeight()`, `getNumberOfRounds()`, `getGunCoolingRate()`,
    # `getMaxInactivityTurns()`, and `getTurnTimeout()` return the game setup values

  @TBA-080
  Scenario: Bot state unavailable before game starts
    When a developer calls `getMyId()` before the game has started
    Then a `BotException` is thrown with a descriptive error message
    # ---

  # Requirement: Turn Execution via go()
  # The TypeScript `BaseBot.go()` method SHALL dispatch pending events for the current tick, send the accumulated BotIntent
  # to the server, and block until the next tick arrives. This matches the Java two-phase execution model.

  @TBA-081
  Scenario: go() dispatches events then sends intent
    When a bot calls `go()` during a tick
    Then all pending events for the current turn are dispatched to event handlers in priority order
    And the current BotIntent (turn rates, target speed, firepower) is sent to the server as JSON
    And the method blocks until the next tick arrives from the server

  @TBA-082
  Scenario: go() blocks via Atomics.wait
    When `go()` is called on the bot worker thread
    Then `BaseBotInternals.waitForNextTurn()` calls `Atomics.wait()` on a SharedArrayBuffer
    And the worker thread is truly blocked (no busy-waiting, no Promises)
    And the thread wakes when the main thread calls `Atomics.notify()` upon receiving the next tick

  @TBA-083
  Scenario: Duplicate go() calls in same turn are idempotent
    When `go()` is called multiple times for the same turn number
    Then the BotIntent is sent only once
    And subsequent calls block until the next turn arrives
    # ---

  # Requirement: Bot State Accessors
  # The TypeScript `BaseBot` SHALL expose all bot state accessors matching the Java `IBaseBot` interface, reading values
  # from the current tick event.

  @TBA-084
  Scenario: Position and direction accessors
    When a tick has been received
    Then `getX()`, `getY()`, `getDirection()`, `getGunDirection()`, `getRadarDirection()` return the current values
    And `getSpeed()` returns the current speed (positive = forward, negative = backward, zero = stopped)
    And `getEnergy()` returns the current energy level
    And `getGunHeat()` returns the current gun heat

  @TBA-085
  Scenario: Turn and round number accessors
    When a tick has been received
    Then `getTurnNumber()` returns the current turn number
    And `getRoundNumber()` returns the current round number
    And `getEnemyCount()` returns the number of enemies remaining
    # ---

  # Requirement: Rate Setters and Max Rate Configuration
  # The TypeScript `BaseBot` SHALL provide rate setters (`setTurnRate`, `setGunTurnRate`, `setRadarTurnRate`,
  # `setTargetSpeed`) and max rate configuration (`setMaxTurnRate`, `setMaxGunTurnRate`, `setMaxRadarTurnRate`,
  # `setMaxSpeed`) that accumulate into a BotIntent sent on the next `go()` call.

  @TBA-086
  Scenario: Setting turn rates
    When a developer calls `setTurnRate(5)` and then `go()`
    Then the BotIntent sent to the server contains `turnRate: 5`
    And the bot turns 5 degrees per turn (up to `MAX_TURN_RATE`)

  @TBA-087
  Scenario: Max rate clamping
    When a developer calls `setMaxSpeed(4)` and then `setTargetSpeed(8)`
    Then the actual target speed is clamped to `4`

  @TBA-088
  Scenario: Multiple setters before go()
    When a developer calls `setTurnRate(5)`, `setGunTurnRate(-10)`, `setTargetSpeed(8)`, and `setFire(2)` before `go()`
    Then all four values are included in the single BotIntent sent to the server
    And the bot moves, turns, turns gun, and fires in parallel on that turn
    # ---

  # Requirement: Adjustment Flags
  # The TypeScript `BaseBot` SHALL support gun-for-body, radar-for-body, and radar-for-gun adjustment flags matching the
  # Java reference implementation.

  @TBA-089
  Scenario: Adjust gun for body turn
    When a developer calls `setAdjustGunForBodyTurn(true)`
    Then `isAdjustGunForBodyTurn()` returns `true`
    And the gun turn rate is compensated for body rotation in the BotIntent

  @TBA-090
  Scenario: Adjust radar for gun turn disables fire assist
    When a developer calls `setAdjustRadarForGunTurn(true)`
    Then `isAdjustRadarForGunTurn()` returns `true`
    And fire assistance is automatically disabled
    # ---

  # Requirement: Stop and Resume (Setter)
  # The TypeScript `BaseBot` SHALL provide `setStop()` and `setResume()` methods that save and restore all movement state.

  @TBA-091
  Scenario: setStop saves movement state
    When a bot is moving forward with `setForward(100)` and turning with `setTurnLeft(90)`
    And `setStop()` is called
    Then `isStopped()` returns `true`
    And all turn rates and target speed are set to zero in the BotIntent
    And the remaining distances and turns are saved internally

  @TBA-092
  Scenario: setResume restores movement state
    When `setResume()` is called after `setStop()`
    Then `isStopped()` returns `false`
    And the previously saved turn rates, target speed, and remaining distances are restored

  @TBA-093
  Scenario: setStop with overwrite
    When `setStop(true)` is called while already stopped
    Then the saved movement state is overwritten with the current (zero) movement
    # ---

  # Requirement: Custom Events
  # The TypeScript `BaseBot` SHALL support adding and removing custom event conditions that trigger `onCustomEvent` when
  # their test function returns `true`.

  @TBA-094
  Scenario: Custom event fires when condition met
    When a developer adds a custom event with `addCustomEvent(condition)` where `condition.test()` checks `getEnergy() < 50`
    And the bot's energy drops below 50
    Then `onCustomEvent` is called with a `CustomEvent` containing the condition

  @TBA-095
  Scenario: Remove custom event
    When a developer calls `removeCustomEvent(condition)` for a previously added condition
    Then the condition is no longer evaluated
    And `onCustomEvent` is no longer triggered for that condition
    # ---

  # Requirement: Event Handler Methods
  # The TypeScript `BaseBot` SHALL provide overridable event handler methods matching the Java `IBaseBot` interface. All
  # handlers fire on the bot worker thread during `dispatchEvents()`.

  @TBA-096
  Scenario: Event handlers fire during go()
    When the server sends a tick containing a `ScannedBotEvent`
    And the bot calls `go()`
    Then `onScannedBot(event)` is called on the bot worker thread before `go()` returns
    And the event handler can call setter methods (e.g., `setFire(3)`) to take action on the same turn

  @TBA-097
  Scenario: All event handlers available
    When a developer extends `BaseBot`
    Then the following methods are available for override: `onConnected`, `onDisconnected`, `onConnectionError`,
    # `onGameStarted`, `onGameEnded`, `onRoundStarted`, `onRoundEnded`, `onTick`, `onBotDeath`, `onDeath`, `onHitBot`,
    # `onHitWall`, `onBulletFired`, `onHitByBullet`, `onBulletHitBullet`, `onBulletHitWall`, `onScannedBot`,
    # `onSkippedTurn`, `onCustomEvent`, `onTeamMessage`, `onWonRound`
    # ---

  # Requirement: Bot Blocking Movement Methods
  # The TypeScript `Bot` class SHALL provide blocking movement methods (`forward`, `back`, `turnLeft`, `turnRight`,
  # `turnGunLeft`, `turnGunRight`, `turnRadarLeft`, `turnRadarRight`) that block the bot worker thread until the movement
  # completes. The developer-facing API is fully synchronous -- no `async`/`await`.

  @TBA-098
  Scenario: forward() blocks until distance traveled
    When a developer calls `forward(100)`
    Then `setForward(100)` is called internally
    And the method repeatedly calls `go()` (blocking via Atomics.wait per turn)
    And the method returns only when `getDistanceRemaining() === 0` and `getSpeed() === 0`

  @TBA-099
  Scenario: back() negates distance
    When a developer calls `back(100)`
    Then it is equivalent to `forward(-100)`

  @TBA-100
  Scenario: turnLeft() blocks until turn completed
    When a developer calls `turnLeft(90)`
    Then `setTurnLeft(90)` is called internally
    And the method blocks until `getTurnRemaining() === 0`

  @TBA-101
  Scenario: turnRight() negates degrees
    When a developer calls `turnRight(90)`
    Then it is equivalent to `turnLeft(-90)`

  @TBA-102
  Scenario: turnGunLeft() blocks until gun turn completed
    When a developer calls `turnGunLeft(360)`
    Then `setTurnGunLeft(360)` is called internally
    And the method blocks until `getGunTurnRemaining() === 0`

  @TBA-103
  Scenario: turnRadarRight() blocks until radar turn completed
    When a developer calls `turnRadarRight(180)`
    Then it is equivalent to `turnRadarLeft(-180)`
    And the method blocks until `getRadarTurnRemaining() === 0`

  @TBA-104
  Scenario: Blocking method skips turn when stopped
    When a bot has been stopped via `stop()`
    And a blocking method like `forward(100)` is called
    Then the method calls `go()` once and returns immediately without setting movement
    # ---

  # Requirement: Bot fire() Blocking Method
  # The TypeScript `Bot.fire()` method SHALL fire the gun and block for one turn, matching the Java reference.

  @TBA-105
  Scenario: fire() sends firepower and advances one turn
    When a developer calls `fire(2)`
    Then `setFire(2)` is called internally
    And `go()` is called to advance one turn
    And the method returns after the turn completes
    # ---

  # Requirement: Bot stop() and resume() Blocking Methods
  # The TypeScript `Bot` class SHALL provide blocking `stop()` and `resume()` methods.

  @TBA-106
  Scenario: stop() saves state and advances turn
    When a developer calls `stop()`
    Then `setStop(false)` is called internally
    And `go()` is called to advance one turn

  @TBA-107
  Scenario: resume() restores state and advances turn
    When a developer calls `resume()`
    Then `setResume()` is called internally
    And `go()` is called to advance one turn
    # ---

  # Requirement: Bot waitFor(condition)
  # The TypeScript `Bot.waitFor()` method SHALL block the bot worker thread until the provided condition is met, calling
  # `go()` each turn to advance.

  @TBA-108
  Scenario: waitFor blocks until condition is true
    When a developer calls `waitFor(condition)` where `condition.test()` checks a custom state
    Then the method calls `go()` in a loop
    And it returns when `condition.test()` returns `true`
    And it also returns if `isRunning()` becomes `false`
    # ---

  # Requirement: Bot rescan() Blocking Method
  # The TypeScript `Bot.rescan()` method SHALL trigger a radar rescan and advance one turn.

  @TBA-109
  Scenario: rescan() rescans and advances turn
    When a developer calls `rescan()`
    Then `ScannedBotEvent` interruptibility is set to `true`
    And `setRescan()` is called
    And `go()` is called to advance one turn
    # ---

  # Requirement: Web Worker Threading Model
  # The TypeScript Bot API SHALL use Web Workers with SharedArrayBuffer and Atomics for the two-thread model defined in
  # ADR-0028. Bot code runs synchronously in a Worker; the WebSocket runs on the main thread.

  @TBA-110
  Scenario: Bot worker thread starts on first tick
    When the first tick of a round is received (turn 1)
    Then a new Worker thread is spawned for the bot
    And the Worker receives a SharedArrayBuffer for synchronization
    And `bot.run()` is called on the Worker thread

  @TBA-111
  Scenario: Worker blocks via Atomics.wait
    When the bot calls `go()` on the Worker thread
    Then `Atomics.wait()` is called on the SharedArrayBuffer synchronization slot
    And the Worker thread is truly blocked (CPU idle, no polling)
    And the Worker wakes when the main thread calls `Atomics.notify()`

  @TBA-112
  Scenario: Worker stops via stop flag
    When the main thread needs to stop the bot (round ended, death, game ended)
    Then a stop flag is set in the SharedArrayBuffer
    And `Atomics.notify()` is called to wake the Worker
    And the Worker detects the stop flag and throws `BotStoppedException`
    And `dispatchFinalTurnEvents()` is called before the Worker exits
    # ---

  # Requirement: Bot Lifecycle Across Rounds
  # The TypeScript Bot API SHALL correctly manage the bot Worker thread across round transitions, matching the Java
  # reference implementation's thread lifecycle.

  @TBA-113
  Scenario: New round starts new Worker
    When a new round begins (turn 1 of round N+1)
    Then any existing bot Worker is stopped
    And remaining values (distanceRemaining, turnRemaining, etc.) are cleared
    And a new Worker is spawned
    And `bot.run()` is called in the new Worker

  @TBA-114
  Scenario: Round end stops Worker
    When a `RoundEndedEvent` is received
    Then `stopThread()` is called
    And `isRunning()` is set to `false` before the Worker is interrupted
    And `enableEventHandling(false)` is called on the main thread (not the Worker) to prevent race conditions

  @TBA-115
  Scenario: Death stops Worker
    When the bot dies (DeathEvent received)
    Then the bot Worker is stopped
    And `dispatchFinalTurnEvents()` ensures the DeathEvent is delivered to the bot's event handler

  @TBA-116
  Scenario: Game end stops Worker and unblocks start()
    When a `GameEndedEvent` is received
    Then the bot Worker is stopped
    And the WebSocket connection closes
    And `start()` unblocks and returns
    # ---

  # Requirement: Distance and Turn Remaining Tracking
  # The TypeScript `BotInternals` SHALL track remaining distances and turns per tick, using the same update logic as the
  # Java reference implementation.

  @TBA-117
  Scenario: Distance remaining decreases each tick
    When `setForward(100)` has been called
    And the bot moves 8 units in a tick
    Then `getDistanceRemaining()` decreases by the distance traveled
    And when `getDistanceRemaining()` reaches 0, the bot stops moving

  @TBA-118
  Scenario: Turn remaining decreases each tick
    When `setTurnLeft(90)` has been called
    And the bot turns 10 degrees in a tick
    Then `getTurnRemaining()` decreases by the degrees turned
    And when `getTurnRemaining()` reaches 0, the bot stops turning

  @TBA-119
  Scenario: Movement reset on hit wall
    When the bot hits a wall
    Then `getDistanceRemaining()` is set to 0

  @TBA-120
  Scenario: Movement reset on ram
    When the bot rams another bot (HitBotEvent with `isRammed() === true`)
    Then `getDistanceRemaining()` is set to 0
    # ---

  # Requirement: Final Tick Event Delivery
  # The TypeScript Bot API SHALL ensure final-tick events (WonRoundEvent, DeathEvent, SkippedTurnEvent) are always
  # dispatched, even when the bot thread is being stopped. This matches the fix documented for the Java, C#, and Python APIs.

  @TBA-121
  Scenario: dispatchFinalTurnEvents after run() exits
    When `bot.run()` exits (normally or via BotStoppedException)
    Then `dispatchFinalTurnEvents()` is called
    And any WonRoundEvent or DeathEvent in the current tick's event queue is dispatched to the bot's event handlers

  @TBA-122
  Scenario: dispatchFinalTurnEvents after idle loop exits
    When the idle `go()` loop exits after `run()` has completed
    Then `dispatchFinalTurnEvents()` is called again
    And events that arrived during the idle phase are dispatched
    # ---

  # Requirement: BotInternals Stop and Resume State
  # The TypeScript `BotInternals` SHALL implement the `IStopResumeListener` interface to save and restore all movement
  # state on stop/resume.

  @TBA-123
  Scenario: Stop saves all remaining values
    When `onStop()` is called
    Then `distanceRemaining`, `turnRemaining`, `gunTurnRemaining`, `radarTurnRemaining` are saved
    And previous direction values (body, gun, radar) are saved

  @TBA-124
  Scenario: Resume restores all remaining values
    When `onResume()` is called
    Then all values saved by `onStop()` are restored
    And movement continues from where it was interrupted
    # ---

  # Requirement: Sample Bot Runtime Distribution
  # The TypeScript sample bot archive SHALL include a `deps/` folder containing all runtime
  # prerequisites so that bots can be started directly from `.ts` source without any pre-compilation
  # step. The `deps/` folder SHALL contain:
  # - `install-dependencies.cmd` and `install-dependencies.sh` — platform-specific installers
  # - `robocode.dev-tank-royale-bot-api-X.Y.Z.tgz` — the bot API npm tarball (produced by `npm pack`)
  # - `package.json` — specifying `@robocode.dev/tank-royale-bot-api` (file reference to local tarball)
  # and `tsx` as runtime dependencies

  @TBA-125
  Scenario: Deps folder is present after Gradle build
    When `./gradlew :sample-bots:typescript:build` completes
    Then `build/archive/deps/` exists and contains `install-dependencies.cmd`,
    # `install-dependencies.sh`, `package.json`, and the bot API `.tgz` tarball

  @TBA-126
  Scenario: install-dependencies script is idempotent
    When `install-dependencies.cmd` (or `.sh`) is run more than once from the same `deps/` folder
    Then only the first run performs `npm install`; subsequent runs exit immediately because `.deps_installed` marker exists

  @TBA-127
  Scenario: install-dependencies handles concurrent launch
    When two bots are started simultaneously from the same archive
    Then only one `npm install` runs; the other waits on the `.deps_lock` mutex directory

  @TBA-128
  Scenario: install-dependencies fails clearly when Node.js is absent
    When `node` is not on the PATH
    Then the script prints an actionable error message and exits with a non-zero code
    # ---

  # Requirement: Sample Bot Launch via tsx
  # TypeScript sample bot launch scripts SHALL execute the bot directly from its `.ts` source file
  # using the locally installed `tsx` binary from `deps/node_modules/.bin/tsx`. No pre-compilation
  # to JavaScript SHALL be required.

  @TBA-129
  Scenario: Windows bot launch
    When `MyFirstBot.cmd` is executed on Windows
    Then `install-dependencies.cmd` is called first
    And the bot is started with `..\deps\node_modules\.bin\tsx MyFirstBot.ts`

  @TBA-130
  Scenario: Unix bot launch
    When `MyFirstBot.sh` is executed on Linux or macOS
    Then `install-dependencies.sh` is called first
    And the bot is started with `../deps/node_modules/.bin/tsx MyFirstBot.ts`

  @TBA-131
  Scenario: Bot connects to Robocode server after launch
    When a TypeScript sample bot is started via its launch script
    Then it connects to the Robocode server using the `@robocode.dev/tank-royale-bot-api`
    # installed in `deps/node_modules/`
```
