# Change: Converge Bot API Test Parity Across Platforms

## Why

After archiving `testable-bot-api-architecture`, the TEST-REGISTRY.md reports all 22 TR-API IDs as
✅ on all four platforms (Java, C#, Python, TypeScript). This is misleading. TypeScript has roughly
ten test files that cover areas no other platform tests at all:

- **EventSystem** — EventQueue priority dispatch, age/size limits, Condition, CustomEvent (580 lines
  in TypeScript; nothing on Java/C#/Python).
- **DefaultEventPriority** — all 15 default priority constants (TypeScript only).
- **Data models** — BotState (22 fields), BotResults (11 fields), BulletState (speed formula),
  GameSetup (8 fields), GameType constants (TypeScript only).
- **BaseBot math utilities** — calcBearing, normalizeAbsoluteAngle/RelativeAngle, distanceTo,
  calcBulletSpeed, calcGunHeat, calcMaxTurnRate (TypeScript only).
- **EnvVars** — full environment variable parsing tested on Java, C#, and TypeScript but
  **entirely absent on Python** (critical gap).

Python also has an empty `test_events.py` file that was never filled.

The result is that correctness guarantees for the event system, data models, and BaseBot math
functions exist only for the TypeScript implementation. Regressions on Java, C#, or Python in
these areas would go undetected.

## What Changes

### TEST-REGISTRY.md

Add three new TR-API categories with 20 new test IDs. Mark them ❌ for platforms where they do
not yet have tests, ✅ only where they do.

| Category               | IDs     | Description                                                                                         |
|------------------------|---------|-----------------------------------------------------------------------------------------------------|
| TR-API-EVT             | 001–009 | Event system — constructors, critical flags, default priorities, EventQueue, Condition, CustomEvent |
| TR-API-MDL             | 001–004 | Data models — BulletState speed formula, BotState/BotResults/GameSetup field storage                |
| TR-API-BOT (additions) | 002–008 | BaseBot math utilities, state accessor defaults, adjustment flag defaults                           |

### TypeScript tests marked LEGACY → deleted

The following TypeScript-only test files will be tagged `LEGACY` and **deleted once all platforms
are green** on the new TR-API-EVT/MDL/BOT IDs:

- `EventSystem.test.ts`
- `DefaultEventPriority.test.ts`
- `BotState.test.ts`, `BotResults.test.ts`, `BulletState.test.ts`, `GameSetup.test.ts`,
  `GameType.test.ts`
- `BaseBotInterfaces.test.ts`

TypeScript-specific tests (`Mappers.test.ts`, `RuntimeAdapter.test.ts`,
`WebSocketHandler.test.ts`) are **not** LEGACY — they test the TypeScript protocol layer, which
has no equivalent on other platforms. They stay.

### New shared JSON test definitions (Tier 1)

| File                                           | TR-API IDs                | Content                                                                 |
|------------------------------------------------|---------------------------|-------------------------------------------------------------------------|
| `bot-api/tests/shared/event-priorities.json`   | EVT-002, EVT-003, EVT-004 | isCritical flags for all event types + 15 default priority values       |
| `bot-api/tests/shared/bullet-state.json`       | MDL-001                   | BulletState speed = 20 − 3×power (positive cases + edge cases)          |
| `bot-api/tests/shared/bot-math.json`           | BOT-002 thru BOT-006      | calcBearing, angle normalization, bullet speed, gun heat, max turn rate |
| `bot-api/tests/shared/constants.json` (extend) | —                         | GameType enum constants (CLASSIC, MELEE, ONE_VS_ONE)                    |

### New platform tests (Tier 2)

| Platform           | New test files                                          | TR-API IDs                                 |
|--------------------|---------------------------------------------------------|--------------------------------------------|
| Python             | `tests/bot_api/test_env_vars.py` (new)                  | TR-API-ENV-001–N (existing, now on Python) |
| Python             | `tests/bot_api/events/test_events.py` (fill empty file) | TR-API-EVT-001, 005–009                    |
| Java               | `EventSystemTest.java`                                  | TR-API-EVT-001, 005–009                    |
| C#                 | `EventSystemTest.cs`                                    | TR-API-EVT-001, 005–009                    |
| Java + C# + Python | BaseBot math test (new)                                 | TR-API-BOT-002–008                         |
| Java + C# + Python | Data model test (new)                                   | TR-API-MDL-002–004                         |

### ADR-0038 updated

- Add TR-API-EVT and TR-API-MDL categories to the spec.
- Document TypeScript-specific test layer (Mappers/RuntimeAdapter/WebSocketHandler) as an
  intentional exception — these files have no cross-platform equivalent.
- Document the Python EnvVars gap and its resolution.
- Change status from Proposed → Accepted after implementation completes.

## Out of Scope

- Python bot-internals / basebot-internals API alignment — that is tracked in a separate future
  ADR.
- ADR-0039 (server testability) — no changes needed.

## Impact

- All four Bot API implementations (Java, C#, Python, TypeScript)
- `bot-api/tests/shared/` (new JSON files)
- `bot-api/tests/TEST-REGISTRY.md`
- `docs-internal/architecture/adr/0038-shared-cross-platform-test-definitions.md`
- TypeScript: 8 LEGACY test files deleted after phase 5
