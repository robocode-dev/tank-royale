# Tasks: Converge Bot API Test Parity Across Platforms

## Phase 0 — Registry, LEGACY tagging, ADR update

- [ ] 0.1 Update `bot-api/tests/TEST-REGISTRY.md`:
  - Add TR-API-EVT-001 thru TR-API-EVT-009 rows (mark ❌ for Java/C#/Python, ✅ TypeScript)
  - Add TR-API-MDL-001 thru TR-API-MDL-004 rows (mark ❌ for Java/C#/Python, ✅ TypeScript)
  - Add TR-API-BOT-002 thru TR-API-BOT-008 rows (mark ❌ for Java/C#/Python, ✅ TypeScript)
- [ ] 0.2 Tag the following TypeScript test files as LEGACY by adding a banner comment at the top
  of each file:
  > `// LEGACY: Superseded by cross-platform TR-API-EVT/MDL/BOT tests. Delete once all platforms are ✅.`
  - `bot-api/typescript/test/EventSystem.test.ts`
  - `bot-api/typescript/test/DefaultEventPriority.test.ts`
  - `bot-api/typescript/test/BotState.test.ts`
  - `bot-api/typescript/test/BotResults.test.ts`
  - `bot-api/typescript/test/BulletState.test.ts`
  - `bot-api/typescript/test/GameSetup.test.ts`
  - `bot-api/typescript/test/GameType.test.ts`
  - `bot-api/typescript/test/BaseBotInterfaces.test.ts`
- [ ] 0.3 Update `docs-internal/architecture/adr/0038-shared-cross-platform-test-definitions.md`:
  - Add TR-API-EVT and TR-API-MDL to the planned categories table
  - Add TR-API-BOT-002 thru 008 to the BOT category
  - Add note about TypeScript-specific tests (Mappers/RuntimeAdapter/WebSocketHandler) as
    intentional language-layer exceptions
  - Add note about Python EnvVars gap and its planned resolution (this change)
  - Keep status as Proposed (will move to Accepted after implementation completes in Phase 5)

## Phase 1 — New shared JSON test definitions (Tier 1)

- [ ] 1.1 Create `bot-api/tests/shared/event-priorities.json` covering:
  - TR-API-EVT-002: critical events (BotDeathEvent, WonRoundEvent, SkippedTurnEvent) return
    `isCritical = true`
  - TR-API-EVT-003: non-critical events return `isCritical = false` (BotHitBotEvent,
    BotHitWallEvent, BulletFiredEvent, BulletHitBotEvent, BulletHitBulletEvent,
    BulletHitWallEvent, HitByBulletEvent, ScannedBotEvent, CustomEvent, TeamMessageEvent)
  - TR-API-EVT-004: default event priorities match the constants in DefaultEventPriority
    (verify each of the 15 priority values)
- [ ] 1.2 Create `bot-api/tests/shared/bullet-state.json` covering:
  - TR-API-MDL-001: BulletState `speed = 20 − 3 × power` for firepower values 0.1, 0.5, 1.0,
    2.0, 3.0, and the edge case power = 0.0 (speed = 20.0)
- [ ] 1.3 Create `bot-api/tests/shared/bot-math.json` covering:
  - TR-API-BOT-002: `calcBearing(sourceDirection, targetDirection)` — use at least 4 representative
    angle pairs (same direction, 90°, 180°, −90°)
  - TR-API-BOT-003: `normalizeAbsoluteAngle` clamps to [0, 360) and
    `normalizeRelativeAngle` clamps to (−180, 180] — at least 6 boundary cases each
  - TR-API-BOT-004: `calcBulletSpeed = 20 − 3 × firepower` (same cases as MDL-001)
  - TR-API-BOT-005: `calcGunHeat = 1 + firepower / 5` for firepower 0.1, 1.0, 2.0, 3.0
  - TR-API-BOT-006: `calcMaxTurnRate` at speed 0, 5, 8, 10 (decreases as speed increases)
- [ ] 1.4 Extend `bot-api/tests/shared/constants.json` with GameType constants:
  - `CLASSIC`, `MELEE`, `ONE_VS_ONE` string values

## Phase 2 — Fill Python gaps

- [ ] 2.1 Create `bot-api/python/tests/bot_api/test_env_vars.py`:
  - Mirror the coverage of `EnvVars.test.ts` (and Java/C# equivalents):
    - Default values when env vars are not set
    - SERVER_URL parsing
    - BOT_NAME, BOT_VERSION, BOT_AUTHORS parsing
    - BOT_TEAM_ID parsing
    - BOT_INITIAL_POSITION parsing (valid and invalid formats)
    - BOT_SECRET parsing
  - Tag: `TR-API-ENV-001` (or whichever existing ENV IDs apply; add new ones if needed)
- [ ] 2.2 Fill `bot-api/python/tests/bot_api/events/test_events.py`:
  - TR-API-EVT-001: All event class constructors store correct fields (test each event type)
  - TR-API-EVT-005: EventQueue dispatches events in priority order (higher first)
  - TR-API-EVT-006: EventQueue removes non-critical events older than MAX_EVENT_AGE turns
  - TR-API-EVT-007: EventQueue size does not exceed MAX_QUEUE_SIZE (256)
  - TR-API-EVT-008: `Condition.test()` is callable and subclass can override return value
  - TR-API-EVT-009: `CustomEvent` fires when its `Condition.test()` returns true

## Phase 3 — EventSystem tests for Java and C#

- [ ] 3.1 Create `bot-api/java/src/test/java/.../EventSystemTest.java`:
  - TR-API-EVT-001: All event constructors store correct fields
  - TR-API-EVT-005: EventQueue priority ordering
  - TR-API-EVT-006: EventQueue age culling (non-critical events removed after MAX_EVENT_AGE)
  - TR-API-EVT-007: EventQueue size cap (MAX_QUEUE_SIZE = 256)
  - TR-API-EVT-008: Condition.test() callable; anonymous subclass override works
  - TR-API-EVT-009: CustomEvent dispatches when Condition.test() returns true
- [ ] 3.2 Create `bot-api/dotnet/src/test/csharp/.../EventSystemTest.cs` with the same coverage

## Phase 4 — BaseBot math + data model tests for Java, C#, and Python

- [ ] 4.1 Java: Add BaseBot math tests covering TR-API-BOT-002 thru BOT-006 (driven from
  `bot-math.json` where applicable, platform-native assertions otherwise)
- [ ] 4.2 Java: Add data model tests covering TR-API-MDL-002 (BotState), MDL-003 (BotResults),
  MDL-004 (GameSetup) — assert all fields stored correctly via constructors
- [ ] 4.3 C#: Same as 4.1 and 4.2 for C#
- [ ] 4.4 Python: Same as 4.1 and 4.2 for Python (TR-API-BOT-002–006, MDL-002–004)
- [ ] 4.5 All platforms: Add TR-API-BOT-007 (BaseBot state accessor defaults — getMyId=0,
  getEnergy=0, etc. when constructed without state)
- [ ] 4.6 All platforms: Add TR-API-BOT-008 (adjustment flags default false —
  adjustGunForBodyTurn, adjustRadarForBodyTurn, adjustRadarForGunTurn)

## Phase 5 — Validate, update registry, delete LEGACY tests

- [ ] 5.1 Run full test suite on all four platforms and verify all new TR-API IDs pass
- [ ] 5.2 Update platform JSON runners to load new shared JSON definition files
  (`event-priorities.json`, `bullet-state.json`, `bot-math.json`)
- [ ] 5.3 Flip all new TR-API IDs to ✅ in `TEST-REGISTRY.md` for the platforms now covered
- [ ] 5.4 **Delete** all LEGACY-tagged TypeScript test files (confirmed ✅ on all platforms):
  - `bot-api/typescript/test/EventSystem.test.ts`
  - `bot-api/typescript/test/DefaultEventPriority.test.ts`
  - `bot-api/typescript/test/BotState.test.ts`
  - `bot-api/typescript/test/BotResults.test.ts`
  - `bot-api/typescript/test/BulletState.test.ts`
  - `bot-api/typescript/test/GameSetup.test.ts`
  - `bot-api/typescript/test/GameType.test.ts`
  - `bot-api/typescript/test/BaseBotInterfaces.test.ts`
- [ ] 5.5 Update ADR-0038 status from Proposed → Accepted
- [ ] 5.6 Run full test suite one final time to confirm no regressions
