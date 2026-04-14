# ADR-0038: Shared Cross-Platform Bot API Test Definitions

**Status:** Proposed  
**Date:** 2026-04-14

---

## Context

ADR-0003 requires "1:1 semantic equivalence" across all Bot API platforms (Java, C#, Python, TypeScript). ADR-0004 makes Java the authoritative reference. However, there are **no shared test cases** — each platform writes its own tests independently. A deep audit confirmed that all four platforms implement identical validation rules (NaN checks, clamping, energy/gunHeat guards, optimal velocity), but this equivalence is verified only by manual inspection.

**The problem:** Validation rules can drift silently between platforms. When a bug is fixed in Java, the same fix may not be applied to Python or C#. There is no mechanism to enforce that all platforms behave identically for the same inputs.

**Prerequisite:** ADR-0037 (Functional Core Extraction) provides a pure, I/O-free validation layer in each platform that can be tested without WebSocket or threads.

**References:**
- [ADR-0003: Cross-Platform Bot API Strategy](./0003-cross-platform-bot-api-strategy.md)
- [ADR-0004: Java as Reference Implementation](./0004-java-reference-implementation.md)
- [ADR-0037: Functional Core Extraction](./0037-functional-core-bot-api-testability.md)

---

## Decision

Define shared test cases as **JSON files** stored in `bot-api/tests/shared/`. Each platform provides a thin **test runner** (~50–100 lines) that loads these JSON files, invokes its `IntentValidator` (from ADR-0037), and asserts the expected results.

### Test Definition Format

```json
{
  "suite": "intent-validation",
  "description": "Validates intent-building logic across all platforms",
  "tests": [
    {
      "id": "fire-valid",
      "description": "setFire with sufficient energy and cold gun sets firepower",
      "method": "setFire",
      "setup": { "energy": 100.0, "gunHeat": 0.0 },
      "args": [1.5],
      "expected": { "firepower": 1.5, "returns": true }
    },
    {
      "id": "fire-insufficient-energy",
      "description": "setFire with insufficient energy does not fire",
      "method": "setFire",
      "setup": { "energy": 0.5, "gunHeat": 0.0 },
      "args": [1.5],
      "expected": { "firepower": null, "returns": false }
    },
    {
      "id": "fire-hot-gun",
      "description": "setFire with hot gun does not fire",
      "method": "setFire",
      "setup": { "energy": 100.0, "gunHeat": 1.5 },
      "args": [1.0],
      "expected": { "firepower": null, "returns": false }
    },
    {
      "id": "fire-nan-throws",
      "description": "setFire with NaN throws IllegalArgumentException",
      "method": "setFire",
      "setup": { "energy": 100.0, "gunHeat": 0.0 },
      "args": ["NaN"],
      "expected": { "throws": "IllegalArgumentException" }
    },
    {
      "id": "turnrate-clamping-positive",
      "description": "setTurnRate above max is clamped to max",
      "method": "setTurnRate",
      "setup": { "maxTurnRate": 10.0 },
      "args": [999.0],
      "expected": { "turnRate": 10.0 }
    },
    {
      "id": "turnrate-clamping-negative",
      "description": "setTurnRate below -max is clamped to -max",
      "method": "setTurnRate",
      "setup": { "maxTurnRate": 10.0 },
      "args": [-999.0],
      "expected": { "turnRate": -10.0 }
    },
    {
      "id": "targetspeed-clamping",
      "description": "setTargetSpeed above max is clamped to max",
      "method": "setTargetSpeed",
      "setup": { "maxSpeed": 8.0 },
      "args": [100.0],
      "expected": { "targetSpeed": 8.0 }
    }
  ]
}
```

### Planned Test Suites

| File | What it tests |
|------|-------------|
| `intent-validation.json` | Fire, turn rates, target speed — validation and clamping |
| `movement-physics.json` | Optimal velocity algorithm, stopping distance calculation |
| `state-management.json` | Stop/resume, movement reset, saved state restoration |

### Per-Platform Runner

Each platform implements a parameterized test that:
1. Loads JSON test definition files from `bot-api/tests/shared/`
2. For each test case: creates an `IntentValidator`, sets up the declared state, calls the method, asserts expected intent fields
3. Handles special values: `"NaN"` → language NaN, `null` → field not set, `"throws"` → expects exception

Example runner structure (Java):
```java
@ParameterizedTest
@MethodSource("loadSharedTestCases")
void sharedValidationTest(SharedTestCase testCase) {
    var intent = new BotIntent();
    var setup = testCase.getSetup();
    
    if (testCase.getExpected().containsKey("throws")) {
        assertThrows(IllegalArgumentException.class, 
            () -> IntentValidator.invoke(testCase.getMethod(), intent, setup, testCase.getArgs()));
    } else {
        IntentValidator.invoke(testCase.getMethod(), intent, setup, testCase.getArgs());
        assertIntentFields(intent, testCase.getExpected());
    }
}
```

---

## Rationale

**Why JSON (not YAML, TOML, or code):**
- ✅ Every platform has built-in JSON parsing — zero new dependencies
- ✅ Machine-readable — easy to validate, generate, and diff
- ✅ Schema-driven project (ADR-0006) already uses JSON schemas
- ✅ No language-specific constructs that could bias toward one platform
- ❌ No comments — mitigated by `description` field in each test case

**Why shared files (not generated tests):**
- ✅ Human-readable single source of truth
- ✅ Adding a test case automatically tests all 4 platforms
- ✅ Diffs are easy to review
- ❌ Runners must handle cross-language differences (exception names, NaN syntax)

**Alternatives considered:**
1. **Each platform maintains its own equivalent tests** — Status quo. Rejected because it led to the drift and inconsistencies that caused our recent bugs.
2. **Code generation from a spec** — Rejected per ADR-0003. Generated test code is hard to debug.
3. **Single test binary that talks to each platform** — Rejected. Would require running each platform as a service just for unit testing.

---

## Consequences

### Positive

- Single source of truth for Bot API validation behavior
- Adding one JSON test case validates all 4 platforms automatically
- Enforces ADR-0003/0004 semantic equivalence mechanically, not by convention
- Test suites are trivially auditable — just read JSON
- New platform implementations can validate against existing tests immediately

### Negative

- Per-platform runner must be written and maintained (~50–100 lines each)
- Exception type names differ across languages — runners need a mapping layer
- JSON lacks expressiveness for complex setup (mitigated by keeping tests focused on pure functions)

---

## References

- [ADR-0003: Cross-Platform Bot API Strategy](./0003-cross-platform-bot-api-strategy.md)
- [ADR-0004: Java as Reference Implementation](./0004-java-reference-implementation.md)
- [ADR-0037: Functional Core Extraction](./0037-functional-core-bot-api-testability.md)
- [ADR-0006: Schema-Driven Protocol Contracts](./0006-schema-driven-protocol-contracts.md)
