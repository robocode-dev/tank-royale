# ADR-0038: Cross-Platform Test Parity and Shared Test Definitions

**Status:** Proposed  
**Date:** 2026-04-14

---

## Context

ADR-0003 requires "1:1 semantic equivalence" across all Bot API platforms (Java, C#, Python, TypeScript). ADR-0004 makes Java the authoritative reference. However, there is **no mechanism to enforce test parity** — each platform writes its own tests independently, leading to:

- **Coverage gaps:** TypeScript has 0 fire/movement/radar command tests. Python has 4 BotInfo tests vs. 51 in C#. Python has 0 EnvVar tests.
- **Silent drift:** Validation rules can diverge between platforms. A bug fixed in Java may not be caught in Python or C#.
- **No cross-platform traceability:** TR-API-xxx acceptance IDs are used in Java and C#, partially in Python, and not at all in TypeScript.

A deep audit (April 2026) of ~900 test cases across all platforms confirmed identical validation rules (NaN checks, clamping, energy/gunHeat guards, optimal velocity), but equivalence was verified only by manual inspection.

**Prerequisite:** ADR-0037 (Functional Core Extraction) provides a pure, I/O-free validation layer that can be tested without WebSocket or threads.

**References:**
- [ADR-0003: Cross-Platform Bot API Strategy](./0003-cross-platform-bot-api-strategy.md)
- [ADR-0004: Java as Reference Implementation](./0004-java-reference-implementation.md)
- [ADR-0037: Functional Core Extraction](./0037-functional-core-bot-api-testability.md)

---

## Decision

### 1. Every acceptance test must exist on all 4 platforms

Each test identified by a `TR-API-xxx` acceptance ID must have an implementation on every platform. The **test registry** (`bot-api/tests/TEST-REGISTRY.md`) is the single source of truth for what must be tested and where coverage stands.

### 2. Every acceptance ID must have positive and negative tests

Each `TR-API-xxx` ID must include both:

- **Positive tests** — verify correct behavior with valid inputs and preconditions.
- **Negative tests** — verify correct rejection/handling of invalid inputs, insufficient preconditions, and edge cases.

A test ID is only considered complete (✅) when both positive and negative cases are covered on all platforms. This applies equally to Tier 1 (shared JSON) and Tier 2 (platform-specific) tests.

### 3. Test tagging via native framework mechanisms

All tests must be tagged using each platform's native tagging mechanism:

| Platform | Mechanism | Category tag | Test ID tag |
|----------|-----------|-------------|-------------|
| Java | `@Tag("CMD")` `@Tag("TR-API-CMD-001")` | JUnit 5 `@Tag` | JUnit 5 `@Tag` |
| C# | `[Category("CMD")]` `[Category("TR-API-CMD-001")]` | NUnit `[Category]` | NUnit `[Category]` |
| Python | `@pytest.mark.CMD` | pytest marker | Test function name contains `TR_API_CMD_001` |
| TypeScript | `describe("TR-API-CMD-001: ...")` | File-level grouping | describe block name prefix |

**Category tags** (for filtering by area): `VAL`, `CMD`, `TCK`, `BOT`, `UTL`, `GFX`, `Reliability`

**Lifecycle tags** (for migration):
- `LEGACY` — old test that will be replaced by a new shared-definition test. Keep running until the replacement is green on all platforms, then delete.

### 4. Two-tier test architecture

Tests fall into two tiers based on what they exercise:

**Tier 1 — Shared JSON definitions** (pure functions, no I/O):
- Stored in `bot-api/tests/shared/*.json`
- Each platform provides a thin parameterized runner (~50–100 lines)
- One JSON test case = one assertion on all 4 platforms automatically
- Covers: validation, clamping, physics, state management

**Tier 2 — Platform-specific with parity tracking** (integration, I/O):
- Written natively per platform (JUnit, NUnit, pytest, Vitest)
- Must have matching TR-API-xxx ID across all platforms
- Test registry tracks parity status
- Covers: MockedServer interactions, WebSocket, events, constructors

### 5. Test registry as living document

`bot-api/tests/TEST-REGISTRY.md` lists every TR-API-xxx acceptance criterion with:
- Description of the expected behavior
- Tier (shared JSON or platform-specific)
- Per-platform implementation status (✅ / ❌)
- Category tag

The registry is reviewed when adding or modifying tests. CI may optionally validate that every registered ID has a corresponding test on each platform.

### Shared JSON Test Definition Format (Tier 1)

```json
{
  "suite": "intent-validation",
  "description": "Validates intent-building logic across all platforms",
  "tests": [
    {
      "id": "TR-API-CMD-002-fire-valid",
      "description": "setFire with sufficient energy and cold gun sets firepower",
      "method": "setFire",
      "setup": { "energy": 100.0, "gunHeat": 0.0 },
      "args": [1.5],
      "expected": { "firepower": 1.5, "returns": true }
    },
    {
      "id": "TR-API-CMD-002-fire-nan-throws",
      "description": "setFire with NaN throws IllegalArgumentException",
      "method": "setFire",
      "setup": { "energy": 100.0, "gunHeat": 0.0 },
      "args": ["NaN"],
      "expected": { "throws": "IllegalArgumentException" }
    },
    {
      "id": "TR-API-CMD-001-turnrate-clamping",
      "description": "setTurnRate above max is clamped to max",
      "method": "setTurnRate",
      "setup": { "maxTurnRate": 10.0 },
      "args": [999.0],
      "expected": { "turnRate": 10.0 }
    }
  ]
}
```

### Planned Shared Test Suites (Tier 1)

| File | Category | What it tests |
|------|----------|-------------|
| `intent-validation.json` | CMD | Fire, turn rates, target speed — validation and clamping |
| `movement-physics.json` | CMD | Optimal velocity algorithm, stopping distance |
| `botinfo-validation.json` | VAL | Required fields, field length limits, invalid inputs |
| `color-values.json` | GFX | RGBA construction, hex conversion, named constants |
| `constants.json` | VAL | API constant values match across platforms |

### Per-Platform Runner (Tier 1)

Each platform implements a parameterized test that:
1. Loads JSON test definition files from `bot-api/tests/shared/`
2. For each test case: sets up declared state, calls the method, asserts expected fields
3. Handles special values: `"NaN"` → language NaN, `null` → field not set, `"throws"` → expects exception

### Migration strategy

1. Tag all existing tests with `LEGACY` across all platforms
2. For each test category, create the shared JSON definition (Tier 1) or parity-tracked platform test (Tier 2)
3. When a new test is green on all platforms, delete the corresponding `LEGACY` test
4. Continue until no `LEGACY` tests remain

---

## Rationale

**Why 1-1 parity is mandatory (not aspirational):**
The April 2026 audit found that Python had 0 EnvVar tests, TypeScript had 0 command tests, and BotInfo coverage ranged from 4 to 51 tests depending on platform. Every gap is a place where semantic drift can hide. The only way to enforce ADR-0003 is to mechanically require the same acceptance tests everywhere.

**Why two tiers (not all JSON):**
Pure function tests (validation, clamping, physics) are trivially expressed as input/output JSON. Integration tests (MockedServer, WebSocket, constructors) require platform-specific setup that JSON cannot express. Forcing everything into JSON would make integration tests awkward and brittle.

**Why JSON for Tier 1 (not YAML, TOML, or code):**
- ✅ Every platform has built-in JSON parsing — zero new dependencies
- ✅ Machine-readable — easy to validate, generate, and diff
- ✅ Schema-driven project (ADR-0006) already uses JSON schemas
- ✅ No language-specific constructs that could bias toward one platform

**Why a test registry (not just tags):**
Tags alone don't answer "which tests are missing on which platform?" The registry provides a single view of cross-platform coverage, making gaps visible during review.

**Why LEGACY tagging:**
Deleting old tests before new ones are verified is risky. Running both in parallel with clear tags allows safe, incremental migration. The `LEGACY` tag also makes it easy to exclude old tests once replacements are confirmed.

**Alternatives considered:**
1. **Each platform maintains its own equivalent tests** — Status quo. Rejected because it led to drift.
2. **Code generation from a spec** — Rejected per ADR-0003. Generated test code is hard to debug.
3. **Single test binary that talks to each platform** — Rejected. Would require running each platform as a service.

---

## Consequences

### Positive

- Single source of truth for Bot API acceptance behavior (registry + shared JSON)
- Adding one JSON test case validates all 4 platforms automatically (Tier 1)
- Coverage gaps are visible at a glance in the test registry
- Enforces ADR-0003/0004 semantic equivalence mechanically, not by convention
- Safe migration path via LEGACY tagging — no tests deleted prematurely
- New platform implementations can validate against existing tests immediately

### Negative

- Per-platform runner must be written and maintained (~50–100 lines each)
- Exception type names differ across languages — runners need a mapping layer
- Test registry requires manual maintenance (mitigated by CI validation)
- Tier 2 parity is enforced by convention and review, not mechanically

---

## References

- [ADR-0003: Cross-Platform Bot API Strategy](./0003-cross-platform-bot-api-strategy.md)
- [ADR-0004: Java as Reference Implementation](./0004-java-reference-implementation.md)
- [ADR-0037: Functional Core Extraction](./0037-functional-core-bot-api-testability.md)
- [ADR-0006: Schema-Driven Protocol Contracts](./0006-schema-driven-protocol-contracts.md)
- [Test Registry](../../../bot-api/tests/TEST-REGISTRY.md)
