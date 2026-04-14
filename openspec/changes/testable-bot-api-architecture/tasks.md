# Tasks: Testable Bot API Architecture

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

## Phase 2: Shared Test Definitions

- [ ] **2.1** Create `bot-api/tests/shared/intent-validation.json`
- [ ] **2.2** Create `bot-api/tests/shared/movement-physics.json`
- [ ] **2.3** Create `bot-api/tests/shared/state-management.json`

## Phase 3: Per-Platform Test Runners

- [ ] **3.1** Java: parameterized shared test runner
- [ ] **3.2** C#: parameterized shared test runner
- [ ] **3.3** Python: parameterized shared test runner
- [ ] **3.4** TypeScript: parameterized shared test runner
- [ ] **3.5** Verify all shared tests pass on all platforms

## Phase 4: Documentation

- [ ] **4.1** Update TESTING-GUIDE.md with IntentValidator architecture
- [ ] **4.2** Document how to add shared test cases
