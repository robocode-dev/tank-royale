# Change: Refactor server game engine toward functional-core / imperative-shell

## Why

The server's core game engine (ModelUpdater, CollisionDetector, GunEngine) tightly
entangles pure game-logic computations — collision detection, damage calculation,
round-over checks — with state mutation and event dispatch. This makes individual
steps impossible to test, reason about, or reorder in isolation. An FP audit
surfaced 3 HIGH and 5 MEDIUM findings, all rooted in the same structural issue:
computation and effect live in the same functions.

## What Changes

- **Separate pure computation from mutation** in CollisionDetector, GunEngine, and
  ModelUpdater by introducing outcome data classes (extending the existing
  `BulletHitOutcome` and `RoundOutcome` patterns) and moving all state writes into
  a thin apply phase.
- **Make Score immutable** — change all `var` fields to `val`, remove `accumulate()`,
  use `plus` operator with `fold` in `AccumulatedScoreCalculator`.
- **Return immutable game-state snapshots** from `ModelUpdater.update()` instead of
  sharing mutable internal references.
- **Extract higher-order pairwise iteration** to eliminate duplicated nested
  for-loops in CollisionDetector.
- **Replace thrown exceptions with nullable returns** in `ScoreTracker.getScoreAndDamage()`.
- **Make `BotInitializer.randomBotPoint` total** by shuffling available cells instead
  of retrying randomly.

All changes are behavior-preserving internal refactorings. No protocol, API, or
game-physics changes.

## Impact

- Affected specs: `battle-runner` (internal architecture, no behavior change)
- Affected code:
  - `server/src/main/kotlin/.../core/ModelUpdater.kt`
  - `server/src/main/kotlin/.../core/CollisionDetector.kt`
  - `server/src/main/kotlin/.../core/GunEngine.kt`
  - `server/src/main/kotlin/.../core/BotInitializer.kt`
  - `server/src/main/kotlin/.../model/Score.kt`
  - `server/src/main/kotlin/.../model/GameState.kt`
  - `server/src/main/kotlin/.../score/AccumulatedScoreCalculator.kt`
  - `server/src/main/kotlin/.../score/ScoreTracker.kt`
- Risk: LOW — all changes are internal refactorings behind the existing public API.
  Existing tests must continue to pass.
