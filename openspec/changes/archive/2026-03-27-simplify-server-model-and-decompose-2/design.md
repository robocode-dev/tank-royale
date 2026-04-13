# Design: Server Model Simplification and God Class Decomposition

## Context

`GameServer` and `ModelUpdater` are monolithic classes that mix many concerns. The model layer has unnecessary
mutable/immutable class duplication for simple value objects. This proposal restructures both while preserving identical
observable behavior.

## Goals

- Reduce `GameServer` and `ModelUpdater` to <300 lines each by extracting focused collaborators
- Eliminate unnecessary model class duplication (MutablePoint, MutableBullet, IPoint, IBullet)
- Make the `Event` hierarchy sealed for compile-time safety
- Replace global mutable config with injected immutable config

## Non-Goals

- Changing the threading model (single-threaded event loop, coroutines)
- Changing the game simulation algorithm or rules
- Modifying any external protocol or API

## Decisions

### D1: Model Simplification — Why MutablePoint and MutableBullet Are Unnecessary

**MutablePoint** exists solely so `MutableBot.x` and `MutableBot.y` setters can mutate `position.x`/`position.y`
in-place. But `MutableBot.position` is already a `var` — reassigning `position = Point(newX, newY)` achieves the same
result. The allocation cost of a `Point` (two `Double` fields = 16 bytes) is negligible per turn per bot.

Mutation sites in `ModelUpdater.kt`:
- Lines 446-447: `bot.x = x; bot.y = y` → becomes `bot.position = Point(x, y)`
- Lines 622-623: Same pattern in wall collision adjustment
- Lines 531, 535-536: Already use `bot.position = MutablePoint(x, y)` → change to `Point(x, y)`
- `moveToNewPosition()`: Reassign `position` instead of mutating `x`/`y`

**MutableBullet** exists for a single `var tick` field with `incrementTick()`. Since bullets are stored in a
`MutableSet` that is rebuilt each turn anyway, replacing `bullet.incrementTick()` with
`bullet.copy(tick = bullet.tick + 1)` and reassigning is straightforward. There are typically <20 bullets in flight.

### D2: Keep Bot/MutableBot and Turn/MutableTurn

**Bot/MutableBot** — `MutableBot` has 20+ mutable fields and behavior methods (`addDamage`, `changeEnergy`,
`moveToNewPosition`). `Bot` is the frozen snapshot transmitted to clients. This is a genuine working-state vs.
snapshot split.

**Turn/MutableTurn** — `MutableTurn` accumulates events and bot/bullet sets during turn computation. `Turn` is the
immutable record stored in round history. Both representations serve distinct purposes.

### D3: Remove IPoint and IBullet interfaces

With `MutablePoint` and `MutableBullet` eliminated, these interfaces have a single implementation each. Single-
implementation interfaces add indirection without abstraction benefit. Code should use `Point` and `Bullet` directly.

`IBot` is retained — it still bridges `Bot` (immutable) and `MutableBot` (mutable) and is used polymorphically in
mappers and turn snapshots.

### D4: GameServer Decomposition

```
GameServer (coordinator, ~200 lines)
├── ParticipantRegistry     — participant lifecycle, ID assignment, participant map
├── GameLifecycleManager    — state machine, timers, pause/resume/abort
└── MessageBroadcaster      — send/broadcast, bot-list-update
```

**ParticipantRegistry** owns: `participants`, `readyParticipants`, `participantIds`, `participantMap`,
`participantsLock`. Exposes: `addParticipant()`, `removeParticipant()`, `markReady()`, `getParticipantId()`,
`allReady()`.

**GameLifecycleManager** owns: `serverState`, `readyTimeoutTimer`, `turnTimeoutTimer`, `startGameLock`. Exposes:
`state`, `prepareGame()`, `startGame()`, `pauseGame()`, `resumeGame()`, `abortGame()`, `onReadyTimeout()`.

**MessageBroadcaster** owns: `botListUpdateMessage` (volatile snapshot). Exposes: `send()`, `broadcast()`,
`broadcastToObserversAndControllers()`, `updateAndBroadcastBotList()`.

### D5: ModelUpdater Decomposition

```
ModelUpdater (coordinator, ~300 lines)
├── CollisionDetector    — wall/bot/bullet collision geometry
├── BotInitializer       — bot state initialization, random placement
└── GunEngine            — gun cooling, firing, bullet creation
```

**CollisionDetector** receives: bot map, bullet set, arena dimensions. Returns: collision events, position adjustments.
Pure computation — no state.

**BotInitializer** receives: game setup, participant list, initial positions. Returns: populated bot map. Stateless.

**GunEngine** receives: bot, intent, bullet set. Returns: updated gun heat, optionally new bullet. Stateless.

### D6: ServerConfig Injection

```kotlin
data class ServerConfig(
    val port: String,
    val gameTypes: Set<String>,
    val controllerSecrets: Set<String>,
    val botSecrets: Set<String>,
    val initialPositionEnabled: Boolean,
    val tps: Int,
)
```

Built by CLI entry point, injected into `GameServer` constructor. `Server.Companion` retains only constants
(`DEFAULT_PORT`, `DEFAULT_TURNS_PER_SECOND`, etc.).

Note: `tps` can change at runtime via `handleChangeTps()`. The config provides the *initial* value; runtime `tps` lives
on `GameLifecycleManager` (or stays on `GameServer` as a `@Volatile var`).

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| Large refactor touching 2 central classes | Phase the work: model simplification first (phases 1-4), then decomposition (phases 5-8). Each phase is independently testable. |
| Extracted classes may need access to each other's state | Use constructor injection and callback interfaces, not circular dependencies. If two extracted classes need to communicate, route through the coordinator. |
| MutableBot.x/y setter change may have cascading effects | Grep for all `bot.x =` and `bot.y =` patterns; update systematically. Compiler will catch type mismatches. |
| Removing IBullet/IPoint may break external consumers | These are `internal` to the server module. No external consumers exist. |

## Migration Plan

1. Model simplification (phases 1-4) — can be done independently, small PRs
2. ServerConfig + thread pool (phases 5-6) — small standalone changes
3. GameServer decomposition (phase 7) — single PR, requires careful review
4. ModelUpdater decomposition (phase 8) — single PR, requires careful review
