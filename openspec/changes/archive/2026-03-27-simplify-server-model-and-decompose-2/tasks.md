## 1. Model Simplification — Eliminate MutablePoint

- [x] 1.1 Change `MutableBot.position` type from `MutablePoint` to `Point`
- [x] 1.2 Update `MutableBot.x` setter to `position = Point(value, position.y)`
- [x] 1.3 Update `MutableBot.y` setter to `position = Point(position.x, value)`
- [x] 1.4 Update `MutableBot.moveToNewPosition()` to create new `Point` instead of mutating in-place
- [x] 1.5 Update all `bot.position = MutablePoint(x, y)` calls in `ModelUpdater.kt` to `bot.position = Point(x, y)`
- [x] 1.6 Update `MutableTurn.copyBot()` — no longer needs `position.toPoint()`, position is already `Point`
- [x] 1.7 Remove `MutablePoint.kt` and `IPoint.kt`; remove `toMutablePoint()` from `Point.kt`
- [x] 1.8 Change `IBot.position` type from `IPoint` to `Point`; update `Bot.position` accordingly
- [x] 1.9 Update any remaining references to `IPoint` or `MutablePoint` across the server module

## 2. Model Simplification — Eliminate MutableBullet

- [x] 2.1 Replace all `MutableBullet` usage in `ModelUpdater` with `Bullet` (immutable)
- [x] 2.2 Replace `bullet.incrementTick()` calls with `bullet.copy(tick = bullet.tick + 1)` and reassign in the bullets collection
- [x] 2.3 Update bullet collections from `MutableSet<MutableBullet>` to `MutableSet<Bullet>`
- [x] 2.4 Remove `MutableBullet.kt` and `IBullet.kt`
- [x] 2.5 Update `MutableTurn.copyBullet()` — source is now `Bullet`, can be added directly
- [x] 2.6 Update any remaining references to `IBullet` or `MutableBullet` across the server module

## 3. Model Simplification — Fix MutableRound

- [x] 3.1 Document `MutableRound` as an internal-only accumulator (add KDoc)
- [x] 3.2 Remove `IRound` interface if it only has one implementation

## 4. Seal the Event Hierarchy

- [x] 4.1 Change `abstract class Event` to `sealed class Event` in `Event.kt`
- [x] 4.2 Remove `else` branch from `EventsMapper.map()` `when` expression
- [x] 4.3 Verify compiler enforces exhaustiveness (build should fail if a subtype is missing)

## 5. Extract ServerConfig

- [x] 5.1 Create `ServerConfig` data class with fields: `port`, `gameTypes`, `controllerSecrets`, `botSecrets`, `initialPositionEnabled`, `tps`
- [x] 5.2 Inject `ServerConfig` into `GameServer` constructor
- [x] 5.3 Update CLI entry point to build `ServerConfig` and pass it
- [x] 5.4 Remove mutable `var` fields from `Server.Companion` (keep only constants)

## 6. Bound the Thread Pool

- [x] 6.1 Replace `Executors.newCachedThreadPool()` in `ClientWebSocketsHandler` with `Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())`
- [x] 6.2 Verify backpressure behavior under high message volume

## 7. Decompose GameServer

- [x] 7.1 Extract `ParticipantRegistry` — `participants`, `readyParticipants`, `participantIds`, `participantMap`, join/leave/ready methods, `participantsLock`
- [x] 7.2 Extract `GameLifecycleManager` — `serverState`, state machine transitions, `readyTimeoutTimer`, `turnTimeoutTimer`, `startGameLock`, pause/resume/abort
- [x] 7.3 Extract `MessageBroadcaster` — `send()`, `broadcastToAll()`, `broadcastToObserverAndControllers()`, bot-list-update, `botListUpdateMessage`
- [x] 7.4 Reduce `GameServer` to a thin coordinator wiring the three components
- [x] 7.5 Verify all existing handler entry points still work through the coordinator

## 8. Decompose ModelUpdater

- [x] 8.1 Extract `CollisionDetector` — `checkBotWallCollisions()`, `checkBotBotCollisions()`, `checkBulletWallCollisions()`, `checkBulletBotCollisions()`, `checkBulletBulletCollisions()`, geometry helpers
- [x] 8.2 Extract `BotInitializer` — `initializeBotStates()`, `randomBotPoint()`, grid placement logic
- [x] 8.3 Extract `GunEngine` — `checkIfGunMustFire()`, `coolDownGun()`, `fireBullet()`, bullet ID generation
- [x] 8.4 Reduce `ModelUpdater` to a coordinator that owns `botsMap`, `turn`, and round state; delegates to extracted classes
- [x] 8.5 Verify turn execution produces identical results (use existing tests)

## 9. Design Document and Verification

- [x] 9.1 Write `design.md` with decomposition boundaries and dependency graph
- [x] 9.2 Run full server test suite
- [x] 9.3 Manual verification: multi-bot game with GUI, confirm identical behavior
