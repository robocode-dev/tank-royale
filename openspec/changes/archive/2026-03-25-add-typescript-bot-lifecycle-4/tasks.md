## 1. IBaseBot Interface

- [x] 1.1 Define `IBaseBot` interface with all state accessors: `getMyId()`, `getVariant()`, `getVersion()`, `getGameType()`, `getArenaWidth()`, `getArenaHeight()`, `getNumberOfRounds()`, `getGunCoolingRate()`, `getMaxInactivityTurns()`, `getTurnTimeout()`, `getTimeLeft()`, `getRoundNumber()`, `getTurnNumber()`, `getEnemyCount()`, `getEnergy()`, `isDisabled()`, `getX()`, `getY()`, `getDirection()`, `getGunDirection()`, `getRadarDirection()`, `getSpeed()`, `getGunHeat()`, `getBulletStates()`, `getEvents()`, `clearEvents()`
- [x] 1.2 Define rate setters/getters: `getTurnRate()`/`setTurnRate()`, `getMaxTurnRate()`/`setMaxTurnRate()`, `getGunTurnRate()`/`setGunTurnRate()`, `getMaxGunTurnRate()`/`setMaxGunTurnRate()`, `getRadarTurnRate()`/`setRadarTurnRate()`, `getMaxRadarTurnRate()`/`setMaxRadarTurnRate()`, `getTargetSpeed()`/`setTargetSpeed()`, `getMaxSpeed()`/`setMaxSpeed()`
- [x] 1.3 Define fire methods: `setFire(firepower): boolean`, `getFirepower()`
- [x] 1.4 Define adjustment methods: `setAdjustGunForBodyTurn(adjust)`, `isAdjustGunForBodyTurn()`, `setAdjustRadarForBodyTurn(adjust)`, `isAdjustRadarForBodyTurn()`, `setAdjustRadarForGunTurn(adjust)`, `isAdjustRadarForGunTurn()`
- [x] 1.5 Define stop/resume: `setStop()`, `setStop(overwrite)`, `setResume()`, `isStopped()`
- [x] 1.6 Define scan: `setRescan()`
- [x] 1.7 Define custom events: `addCustomEvent(condition): boolean`, `removeCustomEvent(condition): boolean`
- [x] 1.8 Define interruptible: `setInterruptible(interruptible)`
- [x] 1.9 Define color getters/setters: `getBodyColor()`/`setBodyColor()`, `getTurretColor()`/`setTurretColor()`, `getRadarColor()`/`setRadarColor()`, `getBulletColor()`/`setBulletColor()`, `getScanColor()`/`setScanColor()`, `getTracksColor()`/`setTracksColor()`, `getGunColor()`/`setGunColor()`
- [x] 1.10 Define team methods: `getTeammateIds()`, `isTeammate(botId)`, `broadcastTeamMessage(message)`, `sendTeamMessage(teammateId, message)`
- [x] 1.11 Define fire assist: `setFireAssist(enable)`
- [x] 1.12 Define lifecycle: `start()`, `go()`
- [x] 1.13 Define constants: `TEAM_MESSAGE_MAX_SIZE`, `MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN`

## 2. IBot Interface

- [x] 2.1 Define `IBot` interface extending `IBaseBot`
- [x] 2.2 Define `run()` (default empty), `isRunning(): boolean`
- [x] 2.3 Define setter movement methods: `setForward(distance)`, `setBack(distance)`, `setTurnLeft(degrees)`, `setTurnRight(degrees)`, `setTurnGunLeft(degrees)`, `setTurnGunRight(degrees)`, `setTurnRadarLeft(degrees)`, `setTurnRadarRight(degrees)`
- [x] 2.4 Define blocking movement methods: `forward(distance)`, `back(distance)`, `turnLeft(degrees)`, `turnRight(degrees)`, `turnGunLeft(degrees)`, `turnGunRight(degrees)`, `turnRadarLeft(degrees)`, `turnRadarRight(degrees)`
- [x] 2.5 Define remaining getters: `getDistanceRemaining()`, `getTurnRemaining()`, `getGunTurnRemaining()`, `getRadarTurnRemaining()`
- [x] 2.6 Define `fire(firepower)` (blocking), `stop()`, `stop(overwrite)`, `resume()`, `rescan()`, `waitFor(condition)`

## 3. BaseBot Implementation

- [x] 3.1 Implement `BaseBot` abstract class implementing `IBaseBot`
- [x] 3.2 Implement constructors: no-arg (auto-detect config file + env vars), `(botInfo)`, `(botInfo, serverUrl)`, `(botInfo, serverUrl, serverSecret)`
- [x] 3.3 Implement `start()` -- delegates to `BaseBotInternals.start()`
- [x] 3.4 Implement `go()` -- dispatch events for current tick, then call `BaseBotInternals.execute()`
- [x] 3.5 Delegate all state accessors to `BaseBotInternals`
- [x] 3.6 Delegate rate setters/getters, fire, adjustment, stop/resume, scan, color, team, custom event methods
- [x] 3.7 Implement overridable event handler methods: `onConnected()`, `onDisconnected()`, `onConnectionError()`, `onGameStarted()`, `onGameEnded()`, `onRoundStarted()`, `onRoundEnded()`, `onTick()`, `onBotDeath()`, `onDeath()`, `onHitBot()`, `onHitWall()`, `onBulletFired()`, `onHitByBullet()`, `onBulletHitBullet()`, `onBulletHitWall()`, `onScannedBot()`, `onSkippedTurn()`, `onCustomEvent()`, `onTeamMessage()`, `onWonRound()`
- [x] 3.8 Implement `calcBearing()`, `calcGunBearing()`, `calcRadarBearing()`, `calcDeltaAngle()`, `normalizeAbsoluteAngle()`, `normalizeRelativeAngle()`, `directionTo()`, `bearingTo()`, `gunBearingTo()`, `radarBearingTo()`, `distanceTo()`, `getGraphics()`

## 4. BaseBotInternals

- [x] 4.1 Implement constructor: store baseBot, botInfo, serverUrl, serverSecret; create EventQueue, BotEventHandlers, InternalEventHandlers
- [x] 4.2 Implement `start()` -- connect WebSocket, await close latch (Atomics.wait on a close signal)
- [x] 4.3 Implement `connect()` -- create WebSocket on main thread, set up WebSocketHandler
- [x] 4.4 Implement `execute()` -- send BotIntent, handle movement reset after first turn, call `waitForNextTurn()`
- [x] 4.5 Implement `sendIntent()` -- serialize BotIntent to JSON, send via WebSocket
- [x] 4.6 Implement `waitForNextTurn()` using `Atomics.wait(sharedBuffer, ...)` -- blocks bot worker until main thread signals via `Atomics.notify()` (replaces Java's `monitor.wait()`/`notifyAll()`)
- [x] 4.7 Implement `stopRogueThread()` -- check stop flag in SharedArrayBuffer, throw `BotStoppedException` if set
- [x] 4.8 Implement `startThread()` / `stopThread()` -- post messages to start/stop bot Worker; `enableEventHandling()` called on WebSocket (main) thread to prevent race conditions
- [x] 4.9 Implement `createRunnable()` logic in Worker -- run bot.run(), catch BotStoppedException, call `dispatchFinalTurnEvents()`, idle loop with go() until stopped, call `dispatchFinalTurnEvents()` again
- [x] 4.10 Implement `dispatchEvents(turnNumber)` and `dispatchFinalTurnEvents()`
- [x] 4.11 Implement `enableEventHandling(enable)` and `isEventHandlingDisabled()` -- manages `eventHandlingDisabledTurn` to prevent stale event dispatch
- [x] 4.12 Implement state accessors: `getMyId()`, `getGameSetup()`, `getCurrentTickOrThrow()`, `getCurrentTickOrNull()`, `isRunning()`, `setRunning()`
- [x] 4.13 Implement rate/speed management: `setTurnRate()`, `setGunTurnRate()`, `setRadarTurnRate()`, `setTargetSpeed()`, `getNewTargetSpeed()`, `getDistanceTraveledUntilStop()`, max rate fields and clamping
- [x] 4.14 Implement stop/resume: `setStop(overwrite)`, `setResume()`, `isStopped()`, `IStopResumeListener` callback
- [x] 4.15 Implement BotIntent field management: resetMovement(), adjustment flags, color setters, firepower, rescan
- [x] 4.16 Implement stdout/stderr capture and transfer to BotIntent
- [x] 4.17 Implement `addEvent()` for SkippedTurnEvent routing through EventQueue

## 5. Bot Implementation

- [x] 5.1 Implement `Bot` abstract class extending `BaseBot`, implementing `IBot`
- [x] 5.2 Implement constructors delegating to BaseBot
- [x] 5.3 Override `setTurnRate()`, `setGunTurnRate()`, `setRadarTurnRate()`, `setTargetSpeed()` to delegate to BotInternals (which tracks remaining values)
- [x] 5.4 Implement `isRunning()` -- delegates to `BaseBotInternals.isRunning()`
- [x] 5.5 Implement setter movement: `setForward()`, `setBack()` (negates distance), `setTurnLeft()`, `setTurnRight()` (negates), `setTurnGunLeft()`, `setTurnGunRight()` (negates), `setTurnRadarLeft()`, `setTurnRadarRight()` (negates)
- [x] 5.6 Implement blocking movement: `forward()` calls `setForward()` + `waitFor(() => distanceRemaining === 0 && speed === 0)`, `back()` calls `forward(-distance)`, `turnLeft()` calls `setTurnLeft()` + `waitFor(() => turnRemaining === 0)`, and same pattern for all turn methods
- [x] 5.7 Implement remaining getters: `getDistanceRemaining()`, `getTurnRemaining()`, `getGunTurnRemaining()`, `getRadarTurnRemaining()`
- [x] 5.8 Implement `fire(firepower)` -- calls `setFire()` + `go()`
- [x] 5.9 Implement `stop(overwrite?)` -- calls `setStop(overwrite)` + `go()`, `resume()` -- calls `setResume()` + `go()`
- [x] 5.10 Implement `rescan()` -- sets ScannedBotEvent interruptible, calls `setRescan()` + `go()`
- [x] 5.11 Implement `waitFor(condition)` -- loop calling `go()` until condition is true or bot stops running

## 6. BotInternals

- [x] 6.1 Implement constructor: subscribe to internal events (onNextTurn at priority 110, onGameAborted, onRoundEnded, onGameEnded, onDisconnected, onDeath, onHitWall, onHitBot)
- [x] 6.2 Implement `onNextTurn()` -- on turn 1 call `onFirstTurn()` (stop old thread, clearRemaining, start new thread), then `processTurn()`
- [x] 6.3 Implement `processTurn()` -- if disabled, clearRemaining; else updateTurnRemaining, updateGunTurnRemaining, updateRadarTurnRemaining, updateMovement
- [x] 6.4 Implement `updateTurnRemaining()` -- compute delta angle from previous direction, subtract from remaining, update turn rate on BaseBotInternals
- [x] 6.5 Implement `updateGunTurnRemaining()` and `updateRadarTurnRemaining()` -- same pattern as body turn
- [x] 6.6 Implement `updateMovement()` -- Nat Pavasant's optimal velocity algorithm, overdrive detection, distance remaining tracking
- [x] 6.7 Implement `onStop()` / `onResume()` (IStopResumeListener) -- save/restore previous directions and all remaining values
- [x] 6.8 Implement override flags (`overrideTurnRate`, `overrideGunTurnRate`, `overrideRadarTurnRate`, `overrideTargetSpeed`)
- [x] 6.9 Implement `onHitWall()` -- reset distanceRemaining to 0; `onHitBot(event)` -- reset distanceRemaining if rammed

## 7. Web Worker Entry Point and SharedArrayBuffer Setup

- [x] 7.1 Create Worker entry point script that receives bot class reference and SharedArrayBuffer via initial message
- [x] 7.2 Implement SharedArrayBuffer layout: synchronization signal slot, stop flag slot
- [x] 7.3 Implement main-thread side: allocate SharedArrayBuffer, spawn Worker, `Atomics.notify()` on tick arrival
- [x] 7.4 Implement worker-thread side: `Atomics.wait()` in `waitForNextTurn()`, check stop flag on wake
- [x] 7.5 Implement `BotStoppedException` (replaces Java's `ThreadInterruptedException`)
- [x] 7.6 Handle Node.js (`worker_threads`) and browser (`Worker`) differences via runtime abstraction from proposal 1
- [x] 7.7 Implement event data transfer between main thread and worker (postMessage for event payloads, shared memory for sync signals)

## 8. Integration Tests

- [x] 8.1 Test: BaseBot connects to mock server and completes handshake (BotHandshake sent, ServerHandshake received)
- [x] 8.2 Test: Bot.start() blocks until game ends and WebSocket closes
- [x] 8.3 Test: go() dispatches events and sends BotIntent to server
- [x] 8.4 Test: forward(100) blocks for multiple turns until distance traveled
- [x] 8.5 Test: turnLeft(90) blocks until turn completed
- [x] 8.6 Test: event handlers fire during go() (onScannedBot, onHitBot, etc.)
- [x] 8.7 Test: round transition -- onRoundEnded fires, bot thread stops, new round starts new thread
- [x] 8.8 Test: game end -- onGameEnded fires, bot thread stops, start() unblocks
- [x] 8.9 Test: stop()/resume() saves and restores movement state
- [x] 8.10 Test: waitFor(condition) blocks until condition is true
- [x] 8.11 Test: isRunning() returns false after round ended / death / game ended
- [x] 8.12 Test: dispatchFinalTurnEvents() delivers WonRoundEvent and DeathEvent on final tick
- [x] 8.13 Test: setAdjustGunForBodyTurn/setAdjustRadarForGunTurn compensate turn rates correctly

