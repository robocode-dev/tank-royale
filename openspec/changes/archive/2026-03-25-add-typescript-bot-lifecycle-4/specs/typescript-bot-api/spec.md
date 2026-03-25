## ADDED Requirements

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
