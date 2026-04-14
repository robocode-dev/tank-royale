# ADR-0039: Server Testability — Physics Core Extraction and Test Framework

**Status:** Proposed
**Date:** 2026-04-14

---

## Context

The game server (`server/`) is the authoritative source for all game physics (ADR-0008), turn processing, collision detection, scoring, and event generation. It comprises 86 source files (~6,000+ lines), with the most critical components being:

| Component | Lines | Responsibility |
|-----------|-------|---------------|
| `GameServer.kt` | 812 | Main orchestrator, state machine, message routing |
| `ModelUpdater.kt` | 575 | Turn processing, state mutations, event generation |
| `CollisionDetector.kt` | 502 | Bot/bullet/wall collision detection and resolution |
| `Line.kt` | 306 | Geometry primitives (ray/segment intersection) |
| `GunEngine.kt` | 114 | Firing mechanics, gun cooling, bullet creation |
| `ClientWebSocketsHandler.kt` | 386 | WebSocket message routing |

**The problem:** An April 2026 audit found **~2,800 lines of pure, testable logic with zero tests**:

- `CollisionDetector` — 502 lines, 0 tests. Bot-bullet, bullet-bullet, bot-wall, bot-bot collisions completely untested.
- `ModelUpdater` — 575 lines, 0 tests. The entire per-turn physics pipeline is untested.
- `GunEngine` — 114 lines, 0 tests. Firing conditions, gun cooling untested.
- `Line` — 306 lines, 0 tests. Geometric intersection math untested.
- `EventsMapper` — 149 lines, 0 tests. Protocol mapping untested.

The existing ~68 tests cover speed calculation (67 data rows, well-done), BotIntent property updates (16 tests), timer scheduling (10 tests), and scoring (6 tests). Three GameServer test files use **reflection to access private fields** — a code smell indicating the class is not designed for testability.

All untested physics and scoring logic is **already pure** — no WebSocket, threading, or I/O dependencies. The barrier is not architecture; it's simply that tests were never written.

**References:**
- [ADR-0008: Server-Authoritative Physics](./0008-server-authoritative-physics.md)
- [ADR-0037: Functional Core Extraction (Bot API)](./0037-functional-core-bot-api-testability.md)
- [ADR-0012: Turn Timing Semantics](./0012-turn-timing-semantics.md)

---

## Decision

### 1. Tag all existing server tests as LEGACY

Use Kotest tags to mark current tests:

```kotlin
object Legacy : Tag()

class CollisionDetectorTest : FunSpec({
    tags(Legacy)
    // ...
})
```

Run without legacy: `./gradlew :server:test -Dkotest.tags="!Legacy"`

### 2. Extract ModelUpdater's pure logic into testable units

`ModelUpdater` (575 lines) mixes pure turn-processing logic with event broadcasting. Extract:

| Extract to | What | Lines |
|-----------|------|-------|
| Keep `CollisionDetector` as-is | Already a standalone class — just needs tests | 502 |
| Keep `GunEngine` as-is | Already standalone — just needs tests | 114 |
| Keep `Line` as-is | Already standalone — just needs tests | 306 |
| Extract `TurnProcessor` from `ModelUpdater` | Pure turn-step pipeline: apply intents → move → fire → collide → score | ~200 |

`ModelUpdater` becomes a thin orchestrator that calls `TurnProcessor` and then broadcasts results.

### 3. Make GameServer testable via constructor injection

Replace reflection-based tests with proper dependency injection:

```kotlin
// Before: tests use reflection to access private fields
val lifecycleManager = GameServer::class.java.getDeclaredField("lifecycleManager")

// After: dependencies injected via constructor
class GameServer(
    private val modelUpdater: ModelUpdater,
    private val broadcaster: MessageBroadcaster,
    private val connectionHandler: ConnectionHandler
)
```

### 4. Create comprehensive physics test suites

**Tier 1 — Pure unit tests (no I/O, no mocking):**

| Suite | Component | Test cases |
|-------|-----------|------------|
| `CollisionDetectorTest` | Bot-bullet, bullet-bullet, bot-wall, bot-bot | 40+ |
| `GunEngineTest` | Fire conditions, gun cooling, bullet creation | 15+ |
| `LineTest` | Ray/segment intersection, edge cases | 20+ |
| `TurnProcessorTest` | Full turn pipeline with known inputs/outputs | 30+ |
| `EventsMapperTest` | Internal event → protocol message mapping | 15+ |
| `ScoreTrackerTest` | Damage tracking, kill accounting | 10+ |

**Tier 2 — Integration tests (with mocked I/O):**

| Suite | What it tests |
|-------|-------------|
| `GameServerLifecycleTest` | Game/round state machine transitions |
| `GameServerScoringTest` | End-to-end scoring through multiple rounds |
| `ConnectionHandlerTest` | Bot connect/disconnect lifecycle |

### 5. Use Kotest property-based testing for physics

The existing `mathTest.kt` demonstrates the pattern well — extend it:

```kotlin
class CollisionDetectorTest : FunSpec({
    context("bullet-bot collisions") {
        withData(
            row(bulletAt(100, 100, heading = 0), botAt(108, 100), true),  // direct hit
            row(bulletAt(100, 100, heading = 0), botAt(200, 100), false), // miss
            row(bulletAt(100, 100, heading = 45), botAt(105, 105), true), // diagonal hit
        ) { (bullet, bot, shouldHit) ->
            val result = CollisionDetector.checkBulletBotCollision(bullet, bot)
            result.isHit shouldBe shouldHit
        }
    }
})
```

### 6. Add TR-SRV-xxx acceptance IDs for server tests

Extend the test registry scheme to server:

| ID Pattern | Category | Example |
|-----------|----------|---------|
| `TR-SRV-PHY-xxx` | Physics (collision, movement) | `TR-SRV-PHY-001` Bullet-bot collision |
| `TR-SRV-GUN-xxx` | Gun mechanics | `TR-SRV-GUN-001` Fire with cold gun |
| `TR-SRV-SCR-xxx` | Scoring | `TR-SRV-SCR-001` Bullet damage score |
| `TR-SRV-TRN-xxx` | Turn processing | `TR-SRV-TRN-001` Intent application |
| `TR-SRV-LCM-xxx` | Lifecycle (game/round) | `TR-SRV-LCM-001` Game start |

### 7. Every acceptance ID must have positive and negative tests

Each `TR-SRV-xxx` ID must include both:

- **Positive tests** — verify correct behavior with valid inputs and preconditions (e.g., bullet hits bot → damage applied, gun cold → bullet created).
- **Negative tests** — verify correct handling of invalid inputs, boundary conditions, and rejection scenarios (e.g., bullet misses bot → no damage, gun hot → no bullet created).

A test ID is only considered complete when both positive and negative cases are covered. This produces a natural test structure:

```kotlin
class GunEngineTest : FunSpec({
    context("TR-SRV-GUN-001: Fire with cold gun") {
        // Positive
        test("fires bullet when gun is cold and energy is sufficient") { /* ... */ }
        test("bullet power matches requested firepower") { /* ... */ }

        // Negative
        test("does not fire when gun is still hot") { /* ... */ }
        test("does not fire when energy is insufficient") { /* ... */ }
        test("does not fire when firepower is zero") { /* ... */ }
    }
})
```

This applies to both Tier 1 (pure unit) and Tier 2 (integration) tests.

---

## Rationale

**Why test pure physics first (not integration):**
The physics code is the most critical and most testable. `CollisionDetector` and `GunEngine` are already pure — they take data in, return results. Writing 100+ unit tests for these components gives the highest confidence-per-effort ratio. Integration tests (WebSocket, multi-bot scenarios) are valuable but require more infrastructure.

**Why constructor injection (not keep using reflection):**
Reflection-based tests are brittle, hard to read, and couple tests to implementation details. Constructor injection makes dependencies explicit and testable. This is a one-time refactor that pays for itself immediately.

**Why Kotest property-based testing:**
Physics has continuous input spaces (positions, angles, speeds). Property-based testing with `forAll`/`withData` catches edge cases that handwritten tests miss. The existing `mathTest.kt` proves the pattern works well in this codebase.

**Why a separate ADR from ADR-0037:**
ADR-0037 addresses Bot API testability (client-side, cross-platform). Server testability is a distinct concern: single platform (Kotlin), different architecture (game loop vs. intent building), different test infrastructure (Kotest vs. JUnit/NUnit/pytest). The functional-core principle is shared, but the implementation differs.

**Alternatives considered:**
1. **Only write integration tests** — Rejected. Integration tests are slow and don't isolate physics bugs. A collision detection bug would manifest as "bot didn't die" rather than "ray-segment intersection off by one."
2. **Rewrite server in a more testable framework** — Rejected. Too risky. The current code is well-structured; it just needs tests and minor refactoring for injection.
3. **Test via Bot API end-to-end** — Rejected. Bot API tests are already complex enough. Server physics should be verified independently.

---

## Consequences

### Positive

- Core physics validated by 100+ targeted unit tests
- Collision bugs caught at the math level, not via flaky integration tests
- GameServer becomes testable without reflection hacks
- Server test registry provides clear coverage visibility
- Property-based testing catches edge cases in continuous input spaces
- Foundation for future regression testing when game rules change

### Negative

- Constructor injection refactor touches `GameServer`, `ModelUpdater` constructors
- Existing reflection-based tests need migration (but they're already fragile)
- `TurnProcessor` extraction adds one layer of indirection in `ModelUpdater`

---

## References

- [ADR-0008: Server-Authoritative Physics](./0008-server-authoritative-physics.md)
- [ADR-0012: Turn Timing Semantics](./0012-turn-timing-semantics.md)
- [ADR-0037: Functional Core Extraction (Bot API)](./0037-functional-core-bot-api-testability.md)
- [ADR-0038: Cross-Platform Test Parity](./0038-shared-cross-platform-test-definitions.md)
- [Server Components C4 View](../c4-views/server-components.md)
