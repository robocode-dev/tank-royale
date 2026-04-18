# Bot API Test Registry

The single source of truth for **what must be tested** across all Bot API platforms (Java, C#, Python, TypeScript). Each row is an acceptance criterion identified by a `TR-API-xxx` ID. If a platform cell is ❌, that test is missing and must be written.

Use this registry to:
- See which tests exist and where gaps are
- Find the acceptance ID to tag a new test with
- Track migration from LEGACY tests to shared definitions

Governed by [ADR-0038](../../docs-internal/architecture/adr/0038-shared-cross-platform-test-definitions.md).

---

## Rules

Every `TR-API-xxx` test listed here **must** have an implementation on all 4 platforms.
Status: ✅ = implemented, ❌ = missing, 🔶 = partial.

**Tier 1** = shared JSON definition in `bot-api/tests/shared/` (pure functions, no I/O)
**Tier 2** = platform-specific implementation with parity tracking (integration, I/O)

### Positive and negative tests required

Every acceptance criterion must include **both** positive (happy-path) and negative (rejection/edge) test cases under the same TR-API ID. A test ID is only ✅ when both sides are covered.

| Type | Verifies | Example |
|------|----------|---------|
| **Positive** | Correct behavior with valid input | `setFire(1.5)` with sufficient energy → firepower set to 1.5 |
| **Negative** | Correct rejection of invalid input | `setFire(1.5)` with insufficient energy → returns false, no firepower set |

This applies to all tiers and all categories (VAL, CMD, TCK, BOT, UTL, GFX).

---

## VAL — Validation

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-VAL-001 | BotInfo required fields validated | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-VAL-002 | BotInfo invalid fields rejected | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-VAL-003 | InitialPosition defaults | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-VAL-004 | InitialPosition mapping round-trip | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-VAL-005 | API constants integrity | 1 | ✅ | ✅ | ✅ | ✅ |

## CMD — Commands

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-CMD-001 | Movement commands clamped correctly | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-CMD-002 | Fire commands validated (energy, gunHeat, NaN, boundaries) | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-CMD-003 | Radar commands (rescan, adjust) | 2 | ✅ | ✅ | ✅ | ✅ |

## TCK — Protocol Conformance

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-TCK-004 | Bot sees first tick state and sends initial intent | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-TCK-005 | WonRoundEvent delivery | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-TCK-006 | Team message delivery | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-TCK-007 | BotHandshake contains correct sessionId, name, version, authors, isDroid | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-TCK-008 | Bot sends BotReady after GameStarted | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-TCK-009 | onRoundStarted fires with roundNumber==1 | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-TCK-010 | onRoundEnded fires with roundNumber==1, turnNumber==5 | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-TCK-011 | onGameEnded fires with numberOfRounds==10 | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-TCK-012 | onSkippedTurn fires with turnNumber==7 | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-TCK-013 | Unknown server message type triggers onConnectionError with descriptive message | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-TCK-014 | BotDeathEvent(victimId==myId) triggers onDeath (isCritical=true) | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-TCK-015 | BotDeathEvent(victimId!=myId) triggers onBotDeath | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-TCK-016 | BulletHitBotEvent(victimId==myId) triggers onHitByBullet | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-TCK-017 | BulletHitBotEvent(victimId!=myId) triggers onBulletHit | 2 | ✅ | ✅ | ✅ | ✅ |

## EVT — Events

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-EVT-001 | Event constructors store fields correctly | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-EVT-002 | Critical events have isCritical = true | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-EVT-003 | Non-critical events have isCritical = false | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-EVT-004 | Default event priorities match constants | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-EVT-005 | EventQueue priority ordering | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-EVT-006 | EventQueue age culling (non-critical) | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-EVT-007 | EventQueue size cap (MAX_QUEUE_SIZE = 256) | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-EVT-008 | Condition.test() callable and overridable | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-EVT-009 | CustomEvent dispatches when Condition.test() is true | 2 | ✅ | ✅ | ✅ | ✅ |

## MDL — Data Models

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-MDL-001 | BulletState speed calculation | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-MDL-002 | BotState constructor | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-MDL-003 | BotResults constructor | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-MDL-004 | GameSetup constructor | 2 | ✅ | ✅ | ✅ | ✅ |

## BOT — Constructor & Lifecycle

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-BOT-001a | Constructor reads env vars and applies defaults | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-BOT-001b | Missing required env defers validation to handshake | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-BOT-001c | Explicit args take precedence over env vars | 2 | ✅ | ✅ | ✅ | ✅ |
| TR-API-BOT-001d | Bot type string parsing and normalization | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-BOT-002 | Math: calcBearing calculation | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-BOT-003 | Math: normalizeAbsoluteAngle and normalizeRelativeAngle | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-BOT-004 | Math: calcBulletSpeed calculation | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-BOT-005 | Math: calcGunHeat calculation | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-BOT-006 | Math: calcMaxTurnRate at various speeds | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-BOT-007 | BaseBot state accessor defaults | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-BOT-008 | BaseBot adjustment flags default false | 1 | ✅ | ✅ | ✅ | ✅ |

## UTL — Utilities

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-UTL-001 | ColorUtil hex round-trip and string parsing | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-UTL-002 | JSON converter serialization/deserialization | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-UTL-003 | Country code validation and local detection | 1 | ✅ | ✅ | ✅ | ✅ |

## GFX — Graphics

| ID | Description | Tier | Java | C# | Python | TypeScript |
|----|-------------|------|------|----|--------|------------|
| TR-API-GFX-001 | Color RGBA construction and named constants | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-GFX-002 | Alpha applied to stroke and fill in SVG | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-GFX-003 | Text is escaped in SVG output | 1 | ✅ | ✅ | ✅ | ✅ |
| TR-API-GFX-004 | Identical draw sequences produce identical SVG | 1 | ✅ | ✅ | ✅ | ✅ |

---

## Coverage Summary

| Category | Total IDs | Java | C# | Python | TypeScript |
|----------|-----------|------|----|--------|------------|
| VAL | 5 | 5 | 5 | 5 | 5 |
| CMD | 3 | 3 | 3 | 3 | 3 |
| TCK | 14 | 14 | 14 | 14 | 14 |
| EVT | 9 | 9 | 9 | 9 | 9 |
| MDL | 4 | 4 | 4 | 4 | 4 |
| BOT | 11 | 11 | 11 | 11 | 11 |
| UTL | 3 | 3 | 3 | 3 | 3 |
| GFX | 4 | 4 | 4 | 4 | 4 |
| **Total** | **53** | **53** | **53** | **53** | **53** |

---

## How to add a new acceptance test

1. Choose a category (`VAL`, `CMD`, `TCK`, `EVT`, `MDL`, `BOT`, `UTL`, `GFX`)
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
