## 1. IBaseBot Interface

- [ ] 1.1 Define `IBaseBot` interface with all state accessors: `getMyId()`, `getVariant()`, `getVersion()`, `getGameType()`, `getArenaWidth()`, `getArenaHeight()`, `getNumberOfRounds()`, `getGunCoolingRate()`, `getMaxInactivityTurns()`, `getTurnTimeout()`, `getTimeLeft()`, `getRoundNumber()`, `getTurnNumber()`, `getEnemyCount()`, `getEnergy()`, `isDisabled()`, `getX()`, `getY()`, `getDirection()`, `getGunDirection()`, `getRadarDirection()`, `getSpeed()`, `getGunHeat()`, `getBulletStates()`, `getEvents()`, `clearEvents()`
- [ ] 1.2 Define rate setters/getters: `getTurnRate()`/`setTurnRate()`, `getMaxTurnRate()`/`setMaxTurnRate()`, `getGunTurnRate()`/`setGunTurnRate()`, `getMaxGunTurnRate()`/`setMaxGunTurnRate()`, `getRadarTurnRate()`/`setRadarTurnRate()`, `getMaxRadarTurnRate()`/`setMaxRadarTurnRate()`, `getTargetSpeed()`/`setTargetSpeed()`, `getMaxSpeed()`/`setMaxSpeed()`
- [ ] 1.3 Define fire methods: `setFire(firepower): boolean`, `getFirepower()`
- [ ] 1.4 Define adjustment methods: `setAdjustGunForBodyTurn(adjust)`, `isAdjustGunForBodyTurn()`, `setAdjustRadarForBodyTurn(adjust)`, `isAdjustRadarForBodyTurn()`, `setAdjustRadarForGunTurn(adjust)`, `isAdjustRadarForGunTurn()`
- [ ] 1.5 Define stop/resume: `setStop()`, `setStop(overwrite)`, `setResume()`, `isStopped()`
- [ ] 1.6 Define scan: `setRescan()`
- [ ] 1.7 Define custom events: `addCustomEvent(condition): boolean`, `removeCustomEvent(condition): boolean`
- [ ] 1.8 Define interruptible: `setInterruptible(interruptible)`
- [ ] 1.9 Define color getters/setters: `getBodyColor()`/`setBodyColor()`, `getTurretColor()`/`setTurretColor()`, `getRadarColor()`/`setRadarColor()`, `getBulletColor()`/`setBulletColor()`, `getScanColor()`/`setScanColor()`, `getTracksColor()`/`setTracksColor()`, `getGunColor()`/`setGunColor()`
- [ ] 1.10 Define team methods: `getTeammateIds()`, `isTeammate(botId)`, `broadcastTeamMessage(message)`, `sendTeamMessage(teammateId, message)`
- [ ] 1.11 Define fire assist: `setFireAssist(enable)`
- [ ] 1.12 Define lifecycle: `start()`, `go()`
- [ ] 1.13 Define constants: `TEAM_MESSAGE_MAX_SIZE`, `MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN`

## 2. IBot Interface

- [ ] 2.1 Define `IBot` interface extending `IBaseBot`
- [ ] 2.2 Define `run()` (default empty), `isRunning(): boolean`
- [ ] 2.3 Define setter movement methods: `setForward(distance)`, `setBack(distance)`, `setTurnLeft(degrees)`, `setTurnRight(degrees)`, `setTurnGunLeft(degrees)`, `setTurnGunRight(degrees)`, `setTurnRadarLeft(degrees)`, `setTurnRadarRight(degrees)`
- [ ] 2.4 Define blocking movement methods: `forward(distance)`, `back(distance)`, `turnLeft(degrees)`, `turnRight(degrees)`, `turnGunLeft(degrees)`, `turnGunRight(degrees)`, `turnRadarLeft(degrees)`, `turnRadarRight(degrees)`
- [ ] 2.5 Define remaining getters: `getDistanceRemaining()`, `getTurnRemaining()`, `getGunTurnRemaining()`, `getRadarTurnRemaining()`
- [ ] 2.6 Define `fire(firepower)` (blocking), `stop()`, `stop(overwrite)`, `resume()`, `rescan()`, `waitFor(condition)`

## 3. BaseBot Implementation

- [ ] 3.1 Implement `BaseBot` abstract class implementing `IBaseBot`
- [ ] 3.2 Implement constructors: no-arg (auto-detect config file + env vars), `(botInfo)`, `(botInfo, serverUrl)`, `(botInfo, serverUrl, serverSecret)`
- [ ] 3.3 Implement `start()` -- delegates to `BaseBotInternals.start()`
- [ ] 3.4 Implement `go()` -- dispatch events for current tick, then call `BaseBotInternals.execute()`
- [ ] 3.5 Delegate all state accessors to `BaseBotInternals`
- [ ] 3.6 Delegate rate setters/getters, fire, adjustment, stop/resume, scan, color, team, custom event methods
- [ ] 3.7 Implement overridable event handler methods: `onConnected()`, `onDisconnected()`, `onConnectionError()`, `onGameStarted()`, `onGameEnded()`, `onRoundStarted()`, `onRoundEnded()`, `onTick()`, `onBotDeath()`, `onDeath()`, `onHitBot()`, `onHitWall()`, `onBulletFired()`, `onHitByBullet()`, `onBulletHitBullet()`, `onBulletHitWall()`, `onScannedBot()`, `onSkippedTurn()`, `onCustomEvent()`, `onTeamMessage()`, `onWonRound()`
- [ ] 3.8 Implement `calcBearing()`, `calcGunBearing()`, `calcRadarBearing()`, `calcDeltaAngle()`, `normalizeAbsoluteAngle()`, `normalizeRelativeAngle()`, `directionTo()`, `bearingTo()`, `gunBearingTo()`, `radarBearingTo()`, `distanceTo()`, `getGraphics()`

## 4. BaseBotInternals

- [ ] 4.1 Implement constructor: store baseBot, botInfo, serverUrl, serverSecret; create EventQueue, BotEventHandlers, InternalEventHandlers
- [ ] 4.2 Implement `start()` -- connect WebSocket, await close latch (Atomics.wait on a close signal)
- [ ] 4.3 Implement `connect()` -- create WebSocket on main thread, set up WebSocketHandler
- [ ] 4.4 Implement `execute()` -- send BotIntent, handle movement reset after first turn, call `waitForNextTurn()`
- [ ] 4.5 Implement `sendIntent()` -- serialize BotIntent to JSON, send via WebSocket
- [ ] 4.6 Implement `waitForNextTurn()` using `Atomics.wait(sharedBuffer, ...)` -- blocks bot worker until main thread signals via `Atomics.notify()` (replaces Java's `monitor.wait()`/`notifyAll()`)
- [ ] 4.7 Implement `stopRogueThread()` -- check stop flag in SharedArrayBuffer, throw `BotStoppedException` if set
- [ ] 4.8 Implement `startThread()` / `stopThread()` -- post messages to start/stop bot Worker; `enableEventHandling()` called on WebSocket (main) thread to prevent race conditions
- [ ] 4.9 Implement `createRunnable()` logic in Worker -- run bot.run(), catch BotStoppedException, call `dispatchFinalTurnEvents()`, idle loop with go() until stopped, call `dispatchFinalTurnEvents()` again
- [ ] 4.10 Implement `dispatchEvents(turnNumber)` and `dispatchFinalTurnEvents()`
- [ ] 4.11 Implement `enableEventHandling(enable)` and `isEventHandlingDisabled()` -- manages `eventHandlingDisabledTurn` to prevent stale event dispatch
- [ ] 4.12 Implement state accessors: `getMyId()`, `getGameSetup()`, `getCurrentTickOrThrow()`, `getCurrentTickOrNull()`, `isRunning()`, `setRunning()`
- [ ] 4.13 Implement rate/speed management: `setTurnRate()`, `setGunTurnRate()`, `setRadarTurnRate()`, `setTargetSpeed()`, `getNewTargetSpeed()`, `getDistanceTraveledUntilStop()`, max rate fields and clamping
- [ ] 4.14 Implement stop/resume: `setStop(overwrite)`, `setResume()`, `isStopped()`, `IStopResumeListener` callback
- [ ] 4.15 Implement BotIntent field management: resetMovement(), adjustment flags, color setters, firepower, rescan
- [ ] 4.16 Implement stdout/stderr capture and transfer to BotIntent
- [ ] 4.17 Implement `addEvent()` for SkippedTurnEvent routing through EventQueue

## 5. Bot Implementation

- [ ] 5.1 Implement `Bot` abstract class extending `BaseBot`, implementing `IBot`
- [ ] 5.2 Implement constructors delegating to BaseBot
- [ ] 5.3 Override `setTurnRate()`, `setGunTurnRate()`, `setRadarTurnRate()`, `setTargetSpeed()` to delegate to BotInternals (which tracks remaining values)
- [ ] 5.4 Implement `isRunning()` -- delegates to `BaseBotInternals.isRunning()`
- [ ] 5.5 Implement setter movement: `setForward()`, `setBack()` (negates distance), `setTurnLeft()`, `setTurnRight()` (negates), `setTurnGunLeft()`, `setTurnGunRight()` (negates), `setTurnRadarLeft()`, `setTurnRadarRight()` (negates)
- [ ] 5.6 Implement blocking movement: `forward()` calls `setForward()` + `waitFor(() => distanceRemaining === 0 && speed === 0)`, `back()` calls `forward(-distance)`, `turnLeft()` calls `setTurnLeft()` + `waitFor(() => turnRemaining === 0)`, and same pattern for all turn methods
- [ ] 5.7 Implement remaining getters: `getDistanceRemaining()`, `getTurnRemaining()`, `getGunTurnRemaining()`, `getRadarTurnRemaining()`
- [ ] 5.8 Implement `fire(firepower)` -- calls `setFire()` + `go()`
- [ ] 5.9 Implement `stop(overwrite?)` -- calls `setStop(overwrite)` + `go()`, `resume()` -- calls `setResume()` + `go()`
- [ ] 5.10 Implement `rescan()` -- sets ScannedBotEvent interruptible, calls `setRescan()` + `go()`
- [ ] 5.11 Implement `waitFor(condition)` -- loop calling `go()` until condition is true or bot stops running

## 6. BotInternals

- [ ] 6.1 Implement constructor: subscribe to internal events (onNextTurn at priority 110, onGameAborted, onRoundEnded, onGameEnded, onDisconnected, onDeath, onHitWall, onHitBot)
- [ ] 6.2 Implement `onNextTurn()` -- on turn 1 call `onFirstTurn()` (stop old thread, clearRemaining, start new thread), then `processTurn()`
- [ ] 6.3 Implement `processTurn()` -- if disabled, clearRemaining; else updateTurnRemaining, updateGunTurnRemaining, updateRadarTurnRemaining, updateMovement
- [ ] 6.4 Implement `updateTurnRemaining()` -- compute delta angle from previous direction, subtract from remaining, update turn rate on BaseBotInternals
- [ ] 6.5 Implement `updateGunTurnRemaining()` and `updateRadarTurnRemaining()` -- same pattern as body turn
- [ ] 6.6 Implement `updateMovement()` -- Nat Pavasant's optimal velocity algorithm, overdrive detection, distance remaining tracking
- [ ] 6.7 Implement `onStop()` / `onResume()` (IStopResumeListener) -- save/restore previous directions and all remaining values
- [ ] 6.8 Implement override flags (`overrideTurnRate`, `overrideGunTurnRate`, `overrideRadarTurnRate`, `overrideTargetSpeed`)
- [ ] 6.9 Implement `onHitWall()` -- reset distanceRemaining to 0; `onHitBot(event)` -- reset distanceRemaining if rammed

## 7. Web Worker Entry Point and SharedArrayBuffer Setup

- [ ] 7.1 Create Worker entry point script that receives bot class reference and SharedArrayBuffer via initial message
- [ ] 7.2 Implement SharedArrayBuffer layout: synchronization signal slot, stop flag slot
- [ ] 7.3 Implement main-thread side: allocate SharedArrayBuffer, spawn Worker, `Atomics.notify()` on tick arrival
- [ ] 7.4 Implement worker-thread side: `Atomics.wait()` in `waitForNextTurn()`, check stop flag on wake
- [ ] 7.5 Implement `BotStoppedException` (replaces Java's `ThreadInterruptedException`)
- [ ] 7.6 Handle Node.js (`worker_threads`) and browser (`Worker`) differences via runtime abstraction from proposal 1
- [ ] 7.7 Implement event data transfer between main thread and worker (postMessage for event payloads, shared memory for sync signals)

## 8. Integration Tests

- [ ] 8.1 Test: BaseBot connects to mock server and completes handshake (BotHandshake sent, ServerHandshake received)
- [ ] 8.2 Test: Bot.start() blocks until game ends and WebSocket closes
- [ ] 8.3 Test: go() dispatches events and sends BotIntent to server
- [ ] 8.4 Test: forward(100) blocks for multiple turns until distance traveled
- [ ] 8.5 Test: turnLeft(90) blocks until turn completed
- [ ] 8.6 Test: event handlers fire during go() (onScannedBot, onHitBot, etc.)
- [ ] 8.7 Test: round transition -- onRoundEnded fires, bot thread stops, new round starts new thread
- [ ] 8.8 Test: game end -- onGameEnded fires, bot thread stops, start() unblocks
- [ ] 8.9 Test: stop()/resume() saves and restores movement state
- [ ] 8.10 Test: waitFor(condition) blocks until condition is true
- [ ] 8.11 Test: isRunning() returns false after round ended / death / game ended
- [ ] 8.12 Test: dispatchFinalTurnEvents() delivers WonRoundEvent and DeathEvent on final tick
- [ ] 8.13 Test: setAdjustGunForBodyTurn/setAdjustRadarForGunTurn compensate turn rates correctly
