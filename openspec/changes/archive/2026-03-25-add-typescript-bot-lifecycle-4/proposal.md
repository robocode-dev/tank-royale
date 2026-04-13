# Change: Add TypeScript Bot API Lifecycle (BaseBot, Bot, Internals)

## Why

BaseBot and Bot are the core classes that bot developers extend. They provide the connection lifecycle, turn-based
execution model (`go()`), state accessors, event handler registration, and blocking movement/turning methods. Without
these classes, no bot can be written against the TypeScript Bot API.

This is proposal 4 of 5 for a TypeScript Bot API with 1:1 semantic equivalence to the Java reference implementation.
Proposals 1-3 (foundation, protocol, events) are prerequisites.

## What Changes

- **IBaseBot interface** -- All state accessors (position, direction, energy, speed, gun heat, etc.), rate setters
  (setTurnRate, setGunTurnRate, setRadarTurnRate, setTargetSpeed), max rate setters, setFire, setRescan, setStop/setResume,
  setInterruptible, setAdjustGunForBodyTurn/setAdjustRadarForBodyTurn/setAdjustRadarForGunTurn, color setters/getters,
  custom events, team messaging, start(), go()
- **IBot interface** -- Blocking movement methods (forward, back, turnLeft, turnRight, turnGunLeft, turnGunRight,
  turnRadarLeft, turnRadarRight), setter counterparts (setForward, setBack, setTurnLeft, etc.), remaining-distance/turn
  getters, fire (blocking), stop/resume (blocking), rescan (blocking), waitFor(Condition), run(), isRunning()
- **BaseBot class** -- Implements IBaseBot. Delegates to BaseBotInternals. Constructors accept optional BotInfo,
  server URL, server secret. Overridable event handler methods (onTick, onScannedBot, onHitBot, onDeath, etc.)
- **BaseBotInternals** -- WebSocket connection, server handshake, tick processing, BotIntent construction and
  transmission, event queue coordination, waitForNextTurn via Atomics.wait (SharedArrayBuffer), thread start/stop
  lifecycle, enableEventHandling, dispatchEvents, dispatchFinalTurnEvents, stdout/stderr capture
- **Bot class** -- Extends BaseBot, implements IBot. Delegates to BotInternals. Blocking methods call
  setX + waitFor(condition)
- **BotInternals** -- Movement tracking (distanceRemaining, turnRemaining, gunTurnRemaining, radarTurnRemaining),
  processTurn() updates per tick, stop/resume state save/restore (IStopResumeListener), thread lifecycle subscriptions
  (onFirstTurn starts bot worker, onRoundEnded/onDeath/onGameEnded stops it), overDriving logic
- **Web Worker entry point** -- Bot code runs in a Worker thread; main thread owns the WebSocket. SharedArrayBuffer +
  Atomics.wait/notify synchronize the two threads (ADR-0028). BotStoppedException replaces Java's
  ThreadInterruptedException. A stop flag in SharedArrayBuffer replaces Thread.interrupt()
- **Integration tests** -- Full lifecycle tests with mock server: connect, handshake, run loop, blocking methods,
  round transitions, game end

## Impact

- Affected specs: `typescript-bot-api` (extends capability from proposals 1-3)
- Affected code: `bot-api/typescript/` (extends module)
- No impact on existing Bot APIs (Java, C#, Python)
