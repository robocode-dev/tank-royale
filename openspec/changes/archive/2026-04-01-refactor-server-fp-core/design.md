## Context

The server game engine runs a tick-based simulation where each turn involves:
position updates → gun cooling/firing → collision detection → event dispatch →
round-over checks. Today these steps mutate shared class-level state
(`ModelUpdater.botsMap`, `.bullets`, `.turn`, etc.) directly, producing a deeply
entangled call graph where every function depends on mutations from prior functions.

The recent `computeRoundOutcome()` / `RoundOutcome` data-class split in
ModelUpdater is an example of the target pattern already applied to one step.
This proposal extends that pattern to all remaining steps.

## Goals / Non-Goals

**Goals:**
- Every game-logic step returns an explicit outcome value; mutations are deferred
  to a single apply phase at the end of `nextTurn()`.
- `Score` becomes fully immutable (all `val` fields).
- `ModelUpdater.update()` returns an immutable snapshot that callers cannot
  accidentally mutate.
- Duplicated pairwise-loop boilerplate is eliminated via a higher-order function.

**Non-Goals:**
- Full immutable game state (threading through all state as values). The server is
  a performance-sensitive real-time game engine; we keep `MutableBot` and the
  working model mutable within the compute phase.
- Introducing a functional effects library (Arrow, etc.). We use plain Kotlin
  idioms: data classes, sealed classes, extension functions.
- Changing the external protocol, API, or game physics.

## Decisions

### 1. Outcome data classes per step
Each "check-and-handle" method becomes two functions:
- A **pure detection** function returning an outcome data class (e.g.,
  `BotCollisionOutcome`, `BulletWallResult`, `InactivityCheck`).
- An **apply** function that writes the outcome to the mutable model.

This mirrors the existing `BulletHitOutcome` and `RoundOutcome` patterns.

**Alternatives considered:**
- Full state monad / immutable state threading — rejected; too much allocation
  overhead for a 60 FPS game loop.
- Keep current approach but improve comments — rejected; the problem is structural,
  not documentation.

### 2. Score immutability
Replace `var` fields with `val`, delete `accumulate()`, and use the existing `plus`
operator. `AccumulatedScoreCalculator.addScores()` becomes a `map` + `fold`.

**Alternatives considered:**
- Keep `var` but hide `accumulate()` as `internal` — rejected; half-measures leave
  the inconsistency in place.

### 3. Immutable GameState snapshot
`ModelUpdater.update()` returns a new `GameStateSnapshot` (or immutable `GameState`)
each turn. The mutable internal model stays private.

**Alternatives considered:**
- Defensive copy of `GameState` on return — rejected; allocates full deep copy each
  turn. A lightweight snapshot with immutable lists is cheaper.

### 4. Deterministic bot placement
Replace `while(true)` random retry in `BotInitializer.randomBotPoint()` with
Fisher-Yates shuffle of available cells + linear pick. Guarantees O(n) termination.

## Risks / Trade-offs

- **Risk:** Refactoring touches core game loop; subtle ordering bugs possible.
  → Mitigation: All existing server tests must pass. Add targeted unit tests for
  each new outcome data class.
- **Risk:** Increased short-lived object allocation from outcome data classes.
  → Mitigation: JVM escape analysis will stack-allocate most of these. Profile
  after implementation to verify.

## Open Questions

- Should the pairwise iteration utility be a top-level extension function or a
  method on CollisionDetector? (Preference: top-level in a `collections` util file.)
