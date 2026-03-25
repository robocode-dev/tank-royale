## ADDED Requirements

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
