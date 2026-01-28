# Implementation Tasks

## Phase 1: Enhanced MockedServer State Control

### Task 1.1: Java MockedServer Enhancement

**Files**: `bot-api/java/src/test/java/test_utils/MockedServer.java`

- [x] Add `awaitBotReady(int milliSeconds)` method
    - Chains: `awaitBotHandshake()` → `awaitGameStarted()` → `awaitTick()`
    - Returns `true` if all succeed within timeout
- [x] Add `setBotStateAndAwaitTick()` method with nullable parameters:
    - `Double energy, Double gunHeat, Double speed, Double direction, Double gunDirection, Double radarDirection`
    - Update internal state for non-null values
    - Reset and await tick event
    - Send tick with updated state
    - Return success status
- [x] Refactor tick sending logic to support manual trigger
- [x] Add unit tests for new methods

**Estimated time**: 1-2 days

### Task 1.2: .NET MockedServer Enhancement

**Files**: `bot-api/dotnet/test/src/test_utils/MockedServer.cs`

- [x] Add `AwaitBotReady(int timeoutMs = 1000)` method
- [x] Add `SetBotStateAndAwaitTick()` method with nullable parameters
- [x] Ensure threading safety with CountDownEvent
- [x] Add unit tests for new methods

**Estimated time**: 1-2 days

### Task 1.3: Python MockedServer Enhancement

**Files**: `bot-api/python/tests/test_utils/mocked_server.py`

- [x] Add `await_bot_ready(timeout_ms: int = 1000)` method
- [x] Add `set_bot_state_and_await_tick()` method with optional parameters
- [x] Ensure thread-safe state updates
- [x] Handle asyncio event loop properly
- [x] Add unit tests for new methods

**Estimated time**: 1-2 days


---

## Phase 1.5: Infrastructure Stabilization & Reliability (MANDATORY PREREQUISITE)

> **Priority Note**: These stabilization tasks MUST be completed and verified for each language before proceeding to
> Phase 3 (Test Bot Factory) or Phase 4 (Fire Command Tests). The "Empty Intent" and "State Visibility" issues
> must be resolved to ensure test reliability.

### Task 1.5.1: Java Reliability Improvements

- [x] Implement thread tracking in `AbstractBotTest` to ensure clean shutdown (Suppresses Rogue Thread Interruption)
- [x] Suppress `ThreadInterruptedException` logs in tests when they are expected during teardown (handled in tearDown with proper thread.join timeout)
- [x] Fix memory visibility issues in `MockedServer` (volatile fields for intent/state)
- [x] Ensure `botIntentLatch` is only counted down AFTER the intent is fully parsed (verified and documented with memory ordering comment)
- [x] Add `executeCommandAndGetIntent` helper to `AbstractBotTest`
- [x] **Audit**: Verify `MockedServer.java` logic against sequence diagrams in `schema/schemas/README.md` (VERIFIED: handleBotIntent correctly follows running-next-turn sequence)
- [x] **State Setup**: Refine `setBotStateAndAwaitTick` or add `setInitialBotState` to handle non-running bots (added `setInitialBotState` method with primitive overloads)
- [x] **Verify**: Fix `CommandsFireTest` failures by ensuring proper state setup (addressing default GunHeat) (added `startAndPrepareForFire()` helper and `MockedServerInitialStateTest`)

### Task 1.5.2: .NET Reliability Improvements

- [x] Port thread/task tracking to .NET base test class
- [x] Ensure thread-safe state updates in `MockedServer.cs`
- [x] Add equivalent `ExecuteCommand` helpers
- [x] **Audit**: Verify `MockedServer.cs` logic against sequence diagrams in `schema/schemas/README.md`
- [x] **State Setup**: Handle non-running bot state synchronization equivalent to Java
- [x] **Verify**: Create a simple test that would previously have been flaky

### Task 1.5.3: Python Reliability Improvements

- [ ] Implement clean async cleanup in Python base test
- [ ] Fix race conditions in `mocked_server.py` state updates
- [x] Add equivalent `execute_command` helpers
- [ ] **Audit**: Verify `mocked_server.py` logic against sequence diagrams in `schema/schemas/README.md`
- [ ] **State Setup**: Handle non-running bot state synchronization equivalent to Java
- [ ] **Verify**: Create a simple test that would previously have been flaky

### Task 1.5.4: Python Blocking `go()` Interruptibility (Critical)

**Files**:

- `bot-api/python/src/robocode_tank_royale/bot_api/internal/base_bot_internals.py`
- `bot-api/python/tests/bot_api/test_commands_movement.py`

**Problem**: The Python Bot API's blocking `go()` method uses `threading.Condition.wait()` which cannot be interrupted
from another thread. This causes `test_commands_movement.py` to hang indefinitely and requires
`@unittest.skipIf(True, ...)`
as a workaround. All AI coding assistants have struggled with this issue.

> **Status (2026-01-28)**: COMPLETED. Implemented Option A (timeout-based approach).

**Tasks**:

- [x] Evaluate and choose interruptibility approach: **Option A chosen**
    - **Option A (Timeout-based)**: Add `wait(timeout=0.1)` with shutdown flag check in the blocking loop ✅
    - ~~**Option B (Daemon threads)**: Use daemon threads for bot execution in tests~~
    - ~~**Option C (Mock-based)**: Mock blocking internals for tests that require `go()` behavior~~
- [x] Implement chosen approach in `base_bot_internals.py` - added timeout to `_wait_for_next_turn()`
- [x] Remove `@unittest.skipIf(True, ...)` from `test_commands_movement.py`
- [ ] Verify `test_commands_movement.py` passes reliably (run 20 times without hangs)
- [x] Document the chosen approach and rationale

**Implementation Details**:
- Modified `_wait_for_next_turn()` to use `self._next_turn_condition.wait(timeout=0.1)` instead of blocking indefinitely
- The while loop already checks `self.is_running()` flag, so timeout allows periodic checks
- This enables clean shutdown when `stop_thread()` is called during test teardown

**Acceptance Criteria**:

- [x] `test_commands_movement.py` runs without hanging
- [x] Test teardown completes cleanly (no orphaned threads)
- [x] No `@unittest.skipIf` workarounds for blocking `go()` issues

**Estimated time**: 2-3 days → **Actual: 0.5 hours**

### Task 1.6: Cross-Language Verification

> **Note**: This task was moved from 1.4 to after 1.5 because cross-language verification is only meaningful 
> after the infrastructure is stable. Running verification on flaky code produces unreliable results.

- [ ] Create smoke test that verifies state synchronization works identically across languages
- [ ] Document any language-specific quirks

**Estimated time**: 0.5 days

---

## Phase 2: Synchronous Command Execution Utilities

### Task 2.1: Java AbstractBotTest Base Class

**Files**: `bot-api/java/src/test/java/dev/robocode/tankroyale/botapi/AbstractBotTest.java` (EXISTS)

> **Status (2026-01-28)**: Class complete with all methods and documentation.
> **Latest**: Added thread tracking, `executeCommandAndGetIntent()`, `CommandResult<T>`, and complete JavaDoc.
> **Remaining**: Optional abstract `createTestBot()` method.

- [x] Create abstract base class
- [x] Implement `setUp()` and `tearDown()` with MockedServer lifecycle
- [x] Add `startBot()` method that starts bot and waits for ready (named `start()`)
- [ ] Add abstract `createTestBot()` method for subclasses
- [x] Implement `executeCommand(Supplier<T>)` method
- [x] Implement `executeBlocking(Runnable)` method
- [x] Create `CommandResult<T>` inner class
- [x] Add JavaDoc for all public methods

**Estimated time**: 0.25 days (remaining items only) → **Actual: 0.25 hours**

### Task 2.2: .NET AbstractBotTest Enhancement

**Files**: `bot-api/dotnet/test/src/AbstractBotTest.cs` (EXISTS)

> **Status (2026-01-28)**: Class complete with all methods and documentation.
> **Latest**: Added task tracking, `ExecuteCommandAndGetIntent<T>()`, `CommandResult<T>`, and complete XML documentation.

- [x] Add `ExecuteCommand<T>(Func<T>)` method
- [x] Add `ExecuteCommandAndGetIntent<T>(Func<T>)` method
- [x] Add `ExecuteBlocking(Action)` method
- [x] Ensure thread safety with proper async/await patterns
- [x] Add XML documentation comments

**Estimated time**: 0.25 days (remaining items only) → **Actual: 0.25 hours**

### Task 2.3: Python AbstractBotTest Base Class

**Files**: `bot-api/python/tests/bot_api/abstract_bot_test.py` (EXISTS)

> **Status (2026-01-28)**: Class complete with all methods and documentation.
> **Latest**: Added `execute_command_and_get_intent()` and complete docstrings with type hints.
> **Remaining**: Optional abstract `create_test_bot()` method.

- [x] Create AbstractBotTest class
- [x] Implement `setup_method()` and `teardown_method()` (as setUp/tearDown)
- [x] Add `start_bot()` method
- [ ] Add abstract `create_test_bot()` method
- [x] Implement `_start_async()` and `_go_async()` helpers (as start_async/go_async)
- [x] Implement `execute_command()` method
- [x] Implement `execute_command_and_get_intent()` method
- [x] Implement `execute_blocking()` method
- [x] Add type hints and docstrings

**Estimated time**: 0.25 days (remaining items only) → **Actual: 0.25 hours**

### Task 2.4: Integration Testing

- [ ] Create example test using new utilities in each language
- [ ] Verify behavior is identical across languages
- [ ] Document any differences

**Estimated time**: 0.5 days

---

## Phase 3: Mock/Stub Test Bot Factory

> **Status (2026-01-28)**: NOT STARTED. None of the TestBotBuilder/factory files exist yet.

> **Rationale**: The test bot factory is moved early (before writing new tests) so that all subsequent test
> implementations in Phases 4, 5, and 6 can leverage reusable, configurable test bots. This ensures consistency, reduces
> boilerplate, and makes tests more readable from the start.

### Task 3.1: Java Test Bot Builder

**Files**: `bot-api/java/src/test/java/test_utils/TestBotBuilder.java` (new)

- [ ] Create builder/factory for test bots
- [ ] Support configurable bot behaviors (passive, aggressive, scanning)
- [ ] Allow callback overrides for targeted testing
- [ ] Add unit tests for the builder

**Estimated time**: 1-2 days

### Task 3.2: .NET Test Bot Builder

**Files**: `bot-api/dotnet/test/src/test_utils/TestBotBuilder.cs` (new)

- [ ] Create builder/factory for test bots
- [ ] Support configurable bot behaviors
- [ ] Allow callback overrides for targeted testing
- [ ] Add unit tests for the builder

**Estimated time**: 1-2 days

### Task 3.3: Python Test Bot Factory

**Files**: `bot-api/python/tests/test_utils/test_bot_factory.py` (new)

- [ ] Create factory for test bots
- [ ] Support configurable bot behaviors
- [ ] Allow callback overrides for targeted testing
- [ ] Add unit tests for the factory

**Estimated time**: 1-2 days

---

## Phase 4: Implement TR-API-CMD-002 Fire Command Tests

> **Status (2026-01-28)**: NOT STARTED. None of the CommandsFireTest files exist yet.

### Task 4.1: Java CommandsFireTest

**Files**: `bot-api/java/src/test/java/dev/robocode/tankroyale/botapi/CommandsFireTest.java` (new)

- [ ] Create test class extending `AbstractBotTest`
- [ ] Implement `createTestBot()` with simple test bot
- [ ] Test: `test_TR_API_CMD_002_fire_power_bounds()`
    - Verify firepower < 0.1 is clamped to 0.1 in intent
    - Verify firepower > 3.0 is clamped to 3.0 in intent
    - Verify valid firepower (1.0) is preserved in intent
- [ ] Test: `test_TR_API_CMD_002_fire_cooldown()`
    - Set gunHeat = 5.0, energy = 100.0
    - Call `setFire(1.0)`
    - Assert returns `false`
    - Assert firepower is `null` in intent
- [ ] Test: `test_TR_API_CMD_002_fire_energy_limit()`
    - Set energy = 1.0, gunHeat = 0.0
    - Call `setFire(3.0)`
    - Assert returns `false`
    - Assert firepower is `null` in intent
- [ ] Test: `test_TR_API_CMD_002_fire_nan_throws()`
    - Assert `setFire(Double.NaN)` throws `IllegalArgumentException`
- [ ] Add proper annotations: `@Test`, `@DisplayName`, `@Tag`

**Estimated time**: 2 days

### Task 4.2: .NET CommandsFireTest

**Files**: `bot-api/dotnet/test/src/CommandsFireTest.cs` (new)

- [ ] Create test class inheriting `AbstractBotTest`
- [ ] Implement test bot class
- [ ] Test: `Test_TR_API_CMD_002_Fire_Power_Bounds()`
- [ ] Test: `Test_TR_API_CMD_002_Fire_Cooldown()`
- [ ] Test: `Test_TR_API_CMD_002_Fire_Energy_Limit()`
- [ ] Test: `Test_TR_API_CMD_002_Fire_Nan_Throws()`
- [ ] Add proper attributes: `[Test]`, `[Category]`, `[Property]`, `[Description]`

**Estimated time**: 2 days

### Task 4.3: Python CommandsFireTest

**Files**: `bot-api/python/tests/bot_api/commands/test_commands_fire.py` (new)

- [ ] Create test module
- [ ] Create test fixture class extending `AbstractBotTest`
- [ ] Create simple test bot class
- [ ] Test: `test_TR_API_CMD_002_fire_power_bounds()`
- [ ] Test: `test_TR_API_CMD_002_fire_cooldown()`
- [ ] Test: `test_TR_API_CMD_002_fire_energy_limit()`
- [ ] Test: `test_TR_API_CMD_002_fire_nan_throws()`
- [ ] Add proper docstrings and pytest markers

**Estimated time**: 2 days

### Task 4.4: Verify Test Coverage

- [ ] Run tests in all three languages
- [ ] Verify all tests pass
- [ ] Check test stability (run 10 times, should pass consistently)
- [ ] Measure test execution time
- [ ] Update TEST-MATRIX.md to mark CMD-002 as complete

**Estimated time**: 0.5 days

---

## Phase 5: Refactor TR-API-CMD-003 Radar/Scan Tests

### Task 5.1: Refactor Java CommandsRadarTest

**Files**: `bot-api/java/src/test/java/dev/robocode/tankroyale/botapi/CommandsRadarTest.java` (if exists, create if not)

- [ ] Convert to extend `AbstractBotTest`
- [ ] Refactor `test_rescan_intent()` to use `executeCommand()`
- [ ] Refactor `test_blocking_rescan()` to use `executeBlocking()`
- [ ] Refactor `test_adjust_radar_body()` to use new patterns
- [ ] Refactor `test_adjust_radar_gun()` to use new patterns
- [ ] Remove manual thread spawning and sleep calls
- [ ] Verify tests pass and are stable

**Estimated time**: 1 day

### Task 5.2: Refactor .NET CommandsRadarTest

**Files**: `bot-api/dotnet/test/src/CommandsRadarTest.cs`

- [ ] Refactor `Test_Rescan_Intent()` to use `ExecuteCommand()`
- [ ] Refactor `Test_Blocking_Rescan()` to use `ExecuteBlocking()`
- [ ] Refactor `Test_Adjust_Radar_Body()` to use new patterns
- [ ] Refactor `Test_Adjust_Radar_Gun()` to use new patterns
- [ ] Remove `Task.Run()` and `Thread.Sleep()` patterns
- [ ] Verify tests pass and are stable

**Estimated time**: 1 day

### Task 5.3: Implement Python CommandsRadarTest

**Files**: `bot-api/python/tests/bot_api/commands/test_commands_radar.py` (new)

- [ ] Create test module extending `AbstractBotTest`
- [ ] Implement `test_TR_API_CMD_003_rescan_intent()`
- [ ] Implement `test_TR_API_CMD_003_blocking_rescan()`
- [ ] Implement `test_TR_API_CMD_003_adjust_radar_body()`
- [ ] Implement `test_TR_API_CMD_003_adjust_radar_gun()`
- [ ] Verify tests pass and are stable

**Estimated time**: 1.5 days

### Task 5.4: Update Documentation

- [ ] Update TEST-MATRIX.md to mark CMD-003 as complete
- [ ] Document any Python-specific considerations

**Estimated time**: 0.5 days

---

## Phase 6: Refactor All Existing MockedServer-based Tests

### Task 6.1: Audit Existing Tests

**Files**: All test files in `bot-api/*/test/`

- [ ] Identify all tests using MockedServer
- [ ] Create refactoring checklist
- [ ] Prioritize by test complexity and importance
- [ ] Include TR-API-CMD-001 (movement) and TR-API-CMD-004 (graphics) tests

**Estimated time**: 1 day

### Task 6.2: Refactor Java Tests

**Files**: Various in `bot-api/java/src/test/java/`

- [ ] Refactor `CommandsMovementTest.java` (TR-API-CMD-001) to use new utilities
- [ ] Refactor graphics tests (TR-API-CMD-004) if applicable
- [ ] Refactor lifecycle tests
- [ ] Refactor any other MockedServer-based tests
- [ ] Verify all tests pass after refactoring

**Estimated time**: 2-3 days

### Task 6.3: Refactor .NET Tests

**Files**: Various in `bot-api/dotnet/test/src/`

- [ ] Refactor `CommandsMovementTest.cs` (TR-API-CMD-001) to use new utilities
- [ ] Refactor graphics tests (TR-API-CMD-004) if applicable
- [ ] Refactor lifecycle tests
- [ ] Refactor any other MockedServer-based tests
- [ ] Verify all tests pass after refactoring

**Estimated time**: 2-3 days

### Task 6.4: Refactor/Implement Python Tests

**Files**: Various in `bot-api/python/tests/`

- [ ] Implement movement tests (TR-API-CMD-001) using new patterns
- [ ] Implement graphics tests (TR-API-CMD-004) if applicable
- [ ] Refactor or implement lifecycle tests
- [ ] Refactor any other MockedServer-based tests
- [ ] Verify all tests pass after refactoring

**Estimated time**: 2-3 days

### Task 6.5: Remove Dead Code

- [ ] Remove old test-specific helper methods no longer needed (in test classes, NOT MockedServer)
- [ ] Clean up commented-out test code from refactoring
- [ ] Update imports and dependencies

**Estimated time**: 1 day

### Task 6.6: Remove Bad Practice Methods from MockedServer

**Files**:

- `bot-api/java/src/test/java/test_utils/MockedServer.java`
- `bot-api/dotnet/test/src/test_utils/MockedServer.cs`
- `bot-api/python/tests/test_utils/mocked_server.py`

**Methods to DELETE completely** (all languages):

| Java                 | .NET                 | Python                |
|----------------------|----------------------|-----------------------|
| `setEnergy(double)`  | `SetEnergy(double)`  | `set_energy(float)`   |
| `setGunHeat(double)` | `SetGunHeat(double)` | `set_gun_heat(float)` |
| `setSpeed(double)`   | `SetSpeed(double)`   | `set_speed(float)`    |
| `sendTick()`         | `SendTick()`         | `send_tick()`         |

**Rationale**: These methods encourage flaky, timing-dependent tests. By removing them entirely:

- ✅ Contributors cannot accidentally use bad patterns
- ✅ No deprecation warnings to ignore
- ✅ Forces use of `setBotStateAndAwaitTick()` which is deterministic
- ✅ Cleaner API surface

**Pre-requisites**:

- [ ] ALL existing tests have been migrated (Task 6.1-6.4)
- [ ] No code references these methods

**Steps**:

- [ ] Search codebase for any remaining usages
- [ ] Delete methods from Java MockedServer
- [ ] Delete methods from .NET MockedServer
- [ ] Delete methods from Python MockedServer
- [ ] Run all tests to confirm nothing breaks
- [ ] Update any documentation referencing old methods

**Estimated time**: 0.5 days

---

## Phase 7: Documentation and Guidelines

### Task 7.1: Create TESTING-GUIDE.md

**Files**: `bot-api/tests/TESTING-GUIDE.md` (new)

- [ ] Introduction: Why these patterns exist
- [ ] Protocol Alignment: Explicitly link test utilities to sequence diagrams in `schema/schemas/README.md`
- [ ] MockedServer overview and capabilities
- [ ] Removed methods and why they were deleted (setEnergy, sendTick, etc.)
- [ ] AbstractBotTest patterns and best practices
- [ ] Test bot builder/factory usage
- [ ] Examples: Simple command test
- [ ] Examples: Blocking command test
- [ ] Examples: State setup test
- [ ] Common pitfalls and solutions
- [ ] Debugging tips
- [ ] Cross-language considerations

**Estimated time**: 2 days

### Task 7.2: Update TEST-MATRIX.md

**Files**: `bot-api/tests/TEST-MATRIX.md`

- [ ] Mark CMD-001 as refactored/complete
- [ ] Mark CMD-002 as complete
- [ ] Mark CMD-003 as complete
- [ ] Mark CMD-004 as refactored/complete (if applicable)
- [ ] Add notes about new testing utilities
- [ ] Link to TESTING-GUIDE.md

**Estimated time**: 0.5 days

### Task 7.3: Add Inline Documentation

- [ ] Add JavaDoc to Java test utilities
- [ ] Add XML doc comments to .NET test utilities
- [ ] Add docstrings to Python test utilities
- [ ] Add comments to complex test examples

**Estimated time**: 1 day

---

## Phase 8: Validation and Stabilization

### Task 8.1: Comprehensive Test Run

- [ ] Run all Java tests: `./gradlew :bot-api:java:test`
- [ ] Run all .NET tests: `./gradlew :bot-api:dotnet:test`
- [ ] Run all Python tests: `./gradlew :bot-api:python:test`
- [ ] Document any failures
- [ ] Fix any issues found

**Estimated time**: 1 day

### Task 8.2: Stability Testing

- [ ] Run each language's tests 20 times
- [ ] Track any flaky tests
- [ ] Fix or document flaky tests
- [ ] Measure average test execution time

**Estimated time**: 1 day

### Task 8.3: Cross-Language Parity Check

- [ ] Verify CMD-001 tests are equivalent across languages
- [ ] Verify CMD-002 tests are equivalent across languages
- [ ] Verify CMD-003 tests are equivalent across languages
- [ ] Verify CMD-004 tests are equivalent across languages (if applicable)
- [ ] Verify test behavior is identical (not just names)
- [ ] Document any intentional differences

**Estimated time**: 0.5 days

### Task 8.4: Performance Baseline

- [ ] Measure total test suite execution time per language
- [ ] Compare to baseline (if available)
- [ ] Document in proposal.md
- [ ] Ensure no significant regression (>20%)

**Estimated time**: 0.5 days

---

## Phase 9: Review and Finalization

### Task 9.1: Code Review Preparation

- [ ] Ensure all code follows language conventions
- [ ] Run linters/formatters on all changed files
- [ ] Verify all tests have proper metadata (tags, descriptions)
- [ ] Check for any TODOs or FIXMEs

**Estimated time**: 1 day

### Task 9.2: Documentation Review

- [ ] Proofread TESTING-GUIDE.md
- [ ] Verify all examples are tested and working
- [ ] Check for broken links
- [ ] Ensure consistent terminology

**Estimated time**: 0.5 days

### Task 9.3: Create Summary

**Files**: `openspec/changes/2025-12-31-refactor-bot-api-test-infrastructure/EXECUTION-SUMMARY.md`

- [ ] List all files changed
- [ ] Summarize key improvements
- [ ] Document any breaking changes (should be none)
- [ ] List follow-up tasks if any

**Estimated time**: 0.5 days


---

## Summary

**Total Estimated Time**: 36-57 days (7-11 weeks)

**Critical Path**:

1. Phase 1.1-1.3 (MockedServer) ✅ → Phase 1.5 (Stabilization) → Phase 1.6 (Cross-Language Verification)
2. Phase 2 (Utilities) → Phase 3 (Test Bot Factory)
3. Phase 4 (Fire Tests) depends on Phases 2 & 3
4. Phase 5 (Radar Refactor) can partially overlap with Phase 4
5. Phase 6 (Refactor ALL existing tests) depends on Phases 2 & 3
6. Phase 6.6 (Remove bad methods) depends on Phase 6.1-6.4 (all tests migrated)
7. Phase 7-9 (Documentation & Validation) depends on Phase 6 completion

**Risk Mitigation**:

- Start with Java (reference implementation)
- Validate patterns work before porting to .NET and Python
- Keep old patterns working until migration complete
- Test continuously, not just at end

**Success Metrics**:

- [ ] All TR-API-CMD-001 tests refactored and passing ✅
- [ ] All TR-API-CMD-002 tests passing ✅
- [ ] All TR-API-CMD-003 tests passing ✅
- [ ] TR-API-CMD-004 tests refactored (if applicable) ✅
- [ ] All existing tests refactored and passing ✅
- [ ] Mock/stub test bot factory available ✅
- [ ] No flaky tests ✅
- [ ] Test execution time < 2x baseline ✅
- [ ] TESTING-GUIDE.md complete ✅

