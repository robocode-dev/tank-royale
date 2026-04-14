# Change Proposal: Testable Server Architecture

**Status:** Proposed
**Date:** 2026-04-14
**ADR:** [ADR-0039](../../../docs-internal/architecture/adr/0039-server-testability.md)

---

## Problem

The game server has ~2,800 lines of pure, testable physics and game logic with **zero tests**:
- `CollisionDetector` (502 lines) — bullet/bot/wall collisions completely untested
- `ModelUpdater` (575 lines) — entire turn-processing pipeline untested
- `GunEngine` (114 lines) — firing mechanics untested
- `Line` (306 lines) — geometric intersection math untested

The existing ~68 tests cover speed calculation, BotIntent updates, timers, and basic scoring. Three `GameServer` test files use reflection to access private fields — indicating the class isn't designed for testability.

All untested logic is already pure (no I/O dependencies). Tests were simply never written.

## Approach

**Phase 0:** Tag existing tests as `LEGACY` and establish the server test registry.

**Phase 1:** Write pure unit tests for existing standalone components (`CollisionDetector`, `GunEngine`, `Line`) without any refactoring. These are already testable as-is.

**Phase 2:** Extract pure turn-processing logic from `ModelUpdater` into a `TurnProcessor`, and refactor `GameServer` to accept dependencies via constructor injection (eliminating reflection-based tests).

**Phase 3:** Write integration tests for game lifecycle, scoring through rounds, and connection handling.

**Phase 4:** Migrate LEGACY tests to the new framework and clean up.

## Sequencing

This change is the **foundation** for the broader testability initiative. It must complete (at least Phase 1) before `testable-bot-api-architecture` begins, because the server defines the ground truth for game physics (ADR-0008).

```
testable-server-architecture (this change)
  Phase 1: Pure physics tests ← START HERE
  Phase 2: Extract & inject
      ↓
testable-bot-api-architecture
  Phase 0: Tag LEGACY
  Phase 1: Extract IntentValidator
  Phase 2: Shared JSON test definitions
```

## Scope

**In scope:**
- Pure physics test suites (collision, gun, geometry, movement)
- Turn-processing extraction and tests
- Constructor injection for GameServer
- Server test registry (TR-SRV-xxx IDs)
- Property-based testing with Kotest

**Out of scope:**
- WebSocket protocol testing (covered separately by Bot API tests)
- GUI/observer testing
- Performance benchmarking
- Bot API changes (covered by `testable-bot-api-architecture`)

## Risk

**Low risk for Phase 1:** Writing tests for existing pure components requires zero production code changes.

**Medium risk for Phase 2:** `TurnProcessor` extraction and constructor injection touch `ModelUpdater` and `GameServer`. Mitigated by running existing tests throughout the refactor.

## Success Criteria

- All `CollisionDetector` scenarios covered: bullet-bot, bullet-bullet, bot-wall, bot-bot
- `GunEngine` validated: fire conditions, gun cooling, bullet creation
- `Line` geometry tested: ray/segment intersection, edge cases, parallel lines
- `ModelUpdater` turn pipeline testable without WebSocket
- `GameServer` testable without reflection
- All tests tagged with TR-SRV-xxx IDs and tracked in test registry
