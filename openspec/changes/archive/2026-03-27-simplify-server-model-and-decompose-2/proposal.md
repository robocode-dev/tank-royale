# Change: Simplify Server Model Classes and Decompose God Classes

## Why

`GameServer` (936 lines) and `ModelUpdater` (1140 lines) each carry 7-10 distinct responsibilities, making them hard to
reason about, test, and extend. The model layer has unnecessary mutable/immutable class duplication for simple value
objects (`MutablePoint`, `MutableBullet`) that adds complexity without meaningful safety benefit. The `Event` base class
is not sealed, so adding a new event type compiles but crashes at runtime in the mapper.

## What Changes

### God class decomposition

- Extract `ParticipantRegistry` from `GameServer` — participant lifecycle (join, leave, ready), ID assignment,
  participant map management
- Extract `GameLifecycleManager` from `GameServer` — state machine transitions, timer management, pause/resume
- Extract `MessageBroadcaster` from `GameServer` — send/broadcast helpers, bot-list-update logic
- Extract `CollisionDetector` from `ModelUpdater` — wall/bot/bullet collision geometry (~300 lines)
- Extract `BotInitializer` from `ModelUpdater` — `initializeBotStates()`, `randomBotPoint()`, grid placement
- Extract `GunEngine` from `ModelUpdater` — gun cooling, firing, bullet creation

### Model simplification

- **Eliminate `MutablePoint`** — change `MutableBot.position` from `MutablePoint` to `Point`; replace in-place `x`/`y`
  mutation with `position = Point(newX, newY)` reassignment
- **Eliminate `MutableBullet`** — replace `incrementTick()` with `copy(tick = tick + 1)` on immutable `Bullet`
- **Remove `IPoint` and `IBullet` interfaces** — single-implementation interfaces after above changes; use concrete
  types directly
- **Fix `MutableRound` inconsistency** — keep as internal accumulator (no immutable pair needed since it is never
  transmitted)
- **Keep `Bot`/`MutableBot` and `Turn`/`MutableTurn` splits** — justified by real behavioral differences

### Structural fixes

- Replace `Executors.newCachedThreadPool()` in `ClientWebSocketsHandler` with bounded `newFixedThreadPool(N)`
- Extract `ServerConfig` data class from `Server.Companion` mutable vars; inject via constructor
- Change `abstract class Event` to `sealed class Event`; remove `else` branch in `EventsMapper.when`

## Impact

- Affected code: `server/` module only (core, model, connection, mapper, event packages)
- No changes to observable protocol behavior — pure internal refactoring
- No impact on Bot APIs, GUI, or booter
- Depends on proposal `fix-server-concurrency-and-shared-state` being completed first
