# Bot API Test Registry

Cross-platform acceptance test registry per [ADR-0038](../../docs-internal/architecture/adr/0038-shared-cross-platform-test-definitions.md).

Every `TR-API-xxx` test listed here **must** have an implementation on all 4 platforms.
Status: ✅ = implemented, ❌ = missing, 🔶 = partial.

**Tier 1** = shared JSON definition in `bot-api/tests/shared/` (pure functions, no I/O)
**Tier 2** = platform-specific implementation with parity tracking (integration, I/O)

---

## VAL — Validation

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-VAL-001 | BotInfo required fields validated | 1 | ✅ | ✅ | ✅ | ❌ |
| TR-API-VAL-002 | BotInfo invalid fields rejected | 1 | ✅ | ✅ | ✅ | ❌ |
| TR-API-VAL-003 | InitialPosition defaults | 1 | ❌ | ❌ | ✅ | ❌ |
| TR-API-VAL-004 | InitialPosition mapping round-trip | 1 | ✅ | ✅ | ✅ | ❌ |
| TR-API-VAL-005 | API constants integrity | 1 | ❌ | ❌ | ✅ | ✅ |

## CMD — Commands

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-CMD-001 | Movement commands clamped correctly | 1 | ✅ | ✅ | ✅ | ❌ |
| TR-API-CMD-002 | Fire commands validated (energy, gunHeat, NaN, boundaries) | 1 | ✅ | ✅ | ✅ | ❌ |
| TR-API-CMD-003 | Radar commands (rescan, adjust) | 2 | ✅ | ✅ | ✅ | ❌ |

## TCK — Protocol Conformance

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-TCK-004 | Bot sees first tick state and sends initial intent | 2 | ✅ | ✅ | ✅ | ❌ |

## BOT — Constructor & Lifecycle

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-BOT-001a | Constructor reads env vars and applies defaults | 2 | ✅ | ✅ | ✅ | ❌ |
| TR-API-BOT-001b | Missing required env defers validation to handshake | 2 | ✅ | ✅ | ✅ | ❌ |
| TR-API-BOT-001c | Explicit args take precedence over env vars | 2 | ✅ | ✅ | ✅ | ❌ |
| TR-API-BOT-001d | Bot type string parsing and normalization | 1 | ✅ | ✅ | ✅ | ❌ |

## UTL — Utilities

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-UTL-001 | ColorUtil hex round-trip and string parsing | 1 | ❌ | ✅ | ✅ | ✅ |
| TR-API-UTL-002 | JSON converter serialization/deserialization | 1 | ✅ | ✅ | ❌ | ✅ |
| TR-API-UTL-003 | Country code validation and local detection | 1 | ✅ | ❌ | ✅ | ❌ |

## GFX — Graphics

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-GFX-001 | Color RGBA construction and named constants | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-GFX-002 | Alpha applied to stroke and fill in SVG | 1 | ❌ | ❌ | ✅ | ❌ |
| TR-API-GFX-003 | Text is escaped in SVG output | 1 | ❌ | ❌ | ✅ | ❌ |
| TR-API-GFX-004 | Identical draw sequences produce identical SVG | 1 | ❌ | ❌ | ✅ | ❌ |

---

## Coverage Summary

| Category | Total IDs | Java | C# | Python | TypeScript |
|----------|-----------|------|----|--------|------------|
| VAL | 5 | 3 | 3 | 5 | 1 |
| CMD | 3 | 3 | 3 | 3 | 0 |
| TCK | 1 | 1 | 1 | 1 | 0 |
| BOT | 4 | 4 | 4 | 4 | 0 |
| UTL | 3 | 2 | 2 | 2 | 2 |
| GFX | 4 | 1 | 1 | 4 | 1 |
| **Total** | **20** | **14** | **14** | **19** | **4** |

---

## How to add a new acceptance test

1. Choose a category (`VAL`, `CMD`, `TCK`, `BOT`, `UTL`, `GFX`)
2. Assign the next sequential ID: `TR-API-{CAT}-{NNN}`
3. Add a row to the table above with ❌ for all platforms
4. For Tier 1: create the JSON test case in `bot-api/tests/shared/`
5. For Tier 2: write the platform-specific test with the TR-API ID in the tag/name
6. Update this table as each platform implementation lands (❌ → ✅)

## How to tag tests

| Platform | Category tag | Test ID | Legacy tag |
|----------|-------------|---------|------------|
| Java | `@Tag("CMD")` | `@Tag("TR-API-CMD-001")` | `@Tag("LEGACY")` |
| C# | `[Category("CMD")]` | `[Category("TR-API-CMD-001")]` | `[Category("LEGACY")]` |
| Python | `@pytest.mark.CMD` | ID in function name: `test_TR_API_CMD_001_*` | `@pytest.mark.LEGACY` |
| TypeScript | File grouping | `describe("TR-API-CMD-001: ...")` | `describe.skip("LEGACY: ...")` |
