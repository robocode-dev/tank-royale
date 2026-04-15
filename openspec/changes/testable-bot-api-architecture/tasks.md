# Tasks: Testable Bot API Architecture

**Policy:** Every task producing tests must include both **positive** (happy-path) and **negative** (rejection/edge) test cases under the same TR-API ID. A task is only done when both sides are covered.

## Phase 0: Tag and Baseline

- [x] **0.1** Tag all existing tests as `LEGACY` on all 4 platforms (Java `@Tag`, C# `[Category]`, Python `@pytest.mark`, TypeScript `describe`)
- [x] **0.2** Fill coverage gaps in TEST-REGISTRY.md — assign TR-API IDs to untagged tests
- [x] **0.3** Verify `LEGACY` tag filtering works per platform (run with/without filter)

## Phase 1: Extract Functional Core

- [ ] **1.1** Create `IntentValidator.java` in Java with extracted pure functions
- [ ] **1.2** Update `BaseBotInternals.java` to delegate to `IntentValidator`
- [ ] **1.3** Create `IntentValidator.cs` in C# with extracted pure functions
- [ ] **1.4** Update `BaseBotInternals.cs` to delegate to `IntentValidator`
- [ ] **1.5** Create `intent_validator.py` in Python with extracted pure functions
- [ ] **1.6** Update `base_bot_internals.py` to delegate to `intent_validator`
- [ ] **1.7** Create `intentValidator.ts` in TypeScript with extracted pure functions
- [ ] **1.8** Update `baseBotInternals.ts` to delegate to `intentValidator`
- [ ] **1.9** Verify all existing tests pass (Java, C#, Python, TypeScript)

## Phase 2: Shared Test Definitions (Tier 1)

- [ ] **2.1** Create `bot-api/tests/shared/intent-validation.json`
- [ ] **2.2** Create `bot-api/tests/shared/movement-physics.json`
- [ ] **2.3** Create `bot-api/tests/shared/botinfo-validation.json`
- [ ] **2.4** Create `bot-api/tests/shared/color-values.json`
- [ ] **2.5** Create `bot-api/tests/shared/constants.json`

## Phase 3: Per-Platform Test Runners

- [ ] **3.1** Java: parameterized shared test runner
- [ ] **3.2** C#: parameterized shared test runner
- [ ] **3.3** Python: parameterized shared test runner
- [ ] **3.4** TypeScript: parameterized shared test runner
- [ ] **3.5** Verify all shared tests pass on all platforms

## Phase 4: Parity — Fill Platform Gaps (Tier 2)

- [ ] **4.1** TypeScript: add fire/movement/radar command tests (TR-API-CMD-001/002/003)
- [ ] **4.2** TypeScript: add bot constructor/lifecycle tests (TR-API-BOT-001a-d)
- [ ] **4.3** TypeScript: add protocol conformance test (TR-API-TCK-004)
- [ ] **4.4** Fill remaining ❌ entries in TEST-REGISTRY.md across all platforms
- [ ] **4.5** Update TEST-REGISTRY.md — all cells should be ✅

## Phase 5: Cleanup and Documentation

- [ ] **5.1** Delete `LEGACY`-tagged tests that are superseded by new tests
- [ ] **5.2** Update TESTING-GUIDE.md with IntentValidator architecture
- [ ] **5.3** Document how to add shared test cases

