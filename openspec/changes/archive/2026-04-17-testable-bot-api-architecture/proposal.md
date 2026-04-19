# Change: Testable Bot API Architecture

## Why

The Bot API internals are tightly coupled, making testing unnecessarily difficult:

1. **Intent-building logic is buried inside I/O classes.** `BaseBotInternals` (700+ lines) mixes pure validation/clamping with WebSocket I/O, thread management, and stdout capture. Testing "does setFire(1.5) set firepower=1.5?" requires spinning up a WebSocket server, a MockedServer, multiple threads, and a 5-step semaphore protocol.

2. **No shared validation spec across platforms.** Java, C#, Python, and TypeScript all implement identical validation rules independently. There are no shared test cases to enforce semantic parity (required by ADR-0003). Rules have drifted — the recent cross-platform bug-fixing session found identical bugs replicated across platforms.

3. **Testing infrastructure has a steep learning curve.** The intent-capture protocol (5-step semaphore gate) was undocumented until this change. Contributors cargo-culted patterns and introduced bugs (e.g., missing `continueBotIntent()` calls that caused test hangs).

This change addresses the root causes rather than symptoms, superseding the archived `refactor-bot-api-test-infrastructure` change.

## Prerequisite

**This change depends on `testable-server-architecture`** (ADR-0039). The server is the source of truth for all game physics (ADR-0008). Server physics tests must be in place first — if collision detection or gun mechanics are wrong at the server level, Bot API tests will validate against incorrect behavior.

Execution order:
1. Server physics tests (ground truth) → `testable-server-architecture`
2. Bot API functional core extraction → this change
3. Shared cross-platform Bot API tests → this change

## What Changes

### Phase 1: Extract Functional Core (ADR-0037)

Extract pure validation and intent-building logic from `BaseBotInternals` into a dedicated `IntentValidator` module in each platform:

**Java:** `botapi/internal/IntentValidator.java`
**C#:** `BotApi/Internal/IntentValidator.cs`
**Python:** `bot_api/internal/intent_validator.py`
**TypeScript:** `bot-api/internal/intentValidator.ts`

Functions to extract:
- `validateAndSetFire(intent, firepower, energy, gunHeat)` — fire validation
- `clampTurnRate(intent, turnRate, maxTurnRate)` — turn rate clamping
- `clampGunTurnRate(intent, gunTurnRate, maxGunTurnRate)` — gun turn rate clamping
- `clampRadarTurnRate(intent, radarTurnRate, maxRadarTurnRate)` — radar turn rate clamping
- `clampTargetSpeed(intent, targetSpeed, maxSpeed)` — target speed clamping
- `getNewTargetSpeed(speed, distance, maxSpeed)` — optimal velocity algorithm
- `getDistanceTraveledUntilStop(speed)` — stopping distance calculation

`BaseBotInternals` delegates to `IntentValidator` — zero public API changes.

### Phase 2: Create Shared Test Definitions (ADR-0038)

Define JSON test case files in `bot-api/tests/shared/`:
- `intent-validation.json` — fire, turn rates, target speed
- `movement-physics.json` — optimal velocity, stopping distance
- `state-management.json` — stop/resume, movement reset

Each platform provides a parameterized test runner (~50–100 lines) that loads JSON, invokes `IntentValidator`, and asserts expected results.

### Phase 3: Per-Platform Unit Tests

Write unit tests for `IntentValidator` in each platform:
- Java: JUnit 5 parameterized tests
- C#: NUnit `[TestCaseSource]` tests
- Python: pytest parameterized tests
- TypeScript: Vitest parameterized tests

These tests run WITHOUT WebSocket, MockedServer, or threads — pure function input/output.

### Phase 4: Documentation

- Update `bot-api/tests/TESTING-GUIDE.md` with the new architecture
- Add `IntentValidator` API reference in each platform's test docs
- Document how to add new shared test cases

## Scope

**In scope:**
- Internal refactoring of `BaseBotInternals` (all 4 platforms)
- New `IntentValidator` module (all 4 platforms)
- Shared JSON test definitions
- Per-platform test runners
- Documentation updates

**Out of scope:**
- Changes to the public Bot API surface (`BaseBot`, `Bot`, `IBot`)
- Server-side changes
- MockedServer replacement (it remains for integration tests)
- New protocol features

## Risk Assessment

**Low risk:**
- Extraction is mechanical — move code, add delegation
- Zero public API changes
- Existing integration tests remain valid
- Each phase is independently valuable and shippable

**Medium risk:**
- Cross-platform consistency of `IntentValidator` signatures
- JSON test runner must handle language-specific edge cases (exception names, NaN)

## Success Criteria

1. A contributor can test "does setFire(1.5) set firepower=1.5?" without WebSocket or threads
2. Adding one JSON test case automatically validates all 4 platforms
3. `BaseBotInternals` is <400 lines of I/O orchestration (down from 700+)
4. All existing tests continue to pass unchanged

## References

- [ADR-0037: Functional Core Extraction](../../docs-internal/architecture/adr/0037-functional-core-bot-api-testability.md)
- [ADR-0038: Shared Cross-Platform Test Definitions](../../docs-internal/architecture/adr/0038-shared-cross-platform-test-definitions.md)
- [ADR-0003: Cross-Platform Bot API Strategy](../../docs-internal/architecture/adr/0003-cross-platform-bot-api-strategy.md)
- [ADR-0004: Java as Reference Implementation](../../docs-internal/architecture/adr/0004-java-reference-implementation.md)
- [Archived: refactor-bot-api-test-infrastructure](../archive/refactor-bot-api-test-infrastructure/proposal.md)
- [TESTING-GUIDE.md](../../bot-api/tests/TESTING-GUIDE.md)
