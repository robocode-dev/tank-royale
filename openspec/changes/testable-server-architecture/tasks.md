# Tasks: Testable Server Architecture

**Policy:** Every task producing tests must include both **positive** (happy-path) and **negative** (rejection/edge) test cases under the same TR-SRV ID. A task is only done when both sides are covered.

## Phase 0: Tag and Baseline

- [ ] **0.1** Tag all existing server tests as `LEGACY` using Kotest `Tag()` mechanism
- [ ] **0.2** Create server section in TEST-REGISTRY.md with TR-SRV-xxx IDs
- [ ] **0.3** Verify LEGACY tag filtering works (`-Dkotest.tags="!Legacy"`)

## Phase 1: Pure Physics Tests (no refactoring needed)

- [ ] **1.1** `CollisionDetectorTest` — bullet-bot collisions: hit ✅, miss ❌, edge, diagonal
- [ ] **1.2** `CollisionDetectorTest` — bullet-bullet collisions: hit ✅, near-miss ❌
- [ ] **1.3** `CollisionDetectorTest` — bot-wall collisions: impact ✅, safe distance ❌
- [ ] **1.4** `CollisionDetectorTest` — bot-bot collisions: overlap ✅, clear spacing ❌
- [ ] **1.5** `GunEngineTest` — fire: cold gun ✅, hot gun ❌, sufficient energy ✅, insufficient energy ❌
- [ ] **1.6** `LineTest` — intersection: crossing ✅, parallel ❌, coincident, endpoint edge cases
- [ ] **1.7** `EventsMapperTest` — valid event ✅, unknown/malformed event ❌
- [ ] **1.8** `ScoreTrackerTest` — damage applied ✅, zero-damage hit ❌, overkill capping
- [ ] **1.9** Verify all Phase 1 tests pass alongside LEGACY tests

## Phase 2: Extract and Inject

- [ ] **2.1** Extract `TurnProcessor` from `ModelUpdater` (pure turn-step pipeline)
- [ ] **2.2** `TurnProcessorTest` — full turn with known inputs, verify outputs
- [ ] **2.3** Refactor `GameServer` to accept dependencies via constructor injection
- [ ] **2.4** Migrate reflection-based GameServer tests to injection-based tests
- [ ] **2.5** Verify all existing tests still pass after refactoring

## Phase 3: Integration Tests

- [ ] **3.1** `GameLifecycleTest` — game/round state machine transitions
- [ ] **3.2** `GameScoringTest` — end-to-end scoring through multiple rounds
- [ ] **3.3** `ConnectionLifecycleTest` — bot connect/disconnect/reconnect

## Phase 4: Cleanup

- [ ] **4.1** Remove LEGACY tag from migrated tests (or delete superseded ones)
- [ ] **4.2** Update TEST-REGISTRY.md — all TR-SRV-xxx cells should be ✅
- [ ] **4.3** Document server test patterns in a server TESTING-GUIDE.md
