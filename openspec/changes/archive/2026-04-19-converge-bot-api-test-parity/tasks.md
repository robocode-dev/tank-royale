# Tasks: Converge Bot API Test Parity Across Platforms

## Phase 0 — Registry, LEGACY tagging, ADR update

- [x] 0.1 Update `bot-api/tests/TEST-REGISTRY.md`:
    - Add TR-API-EVT-001 thru TR-API-EVT-009 rows (mark ❌ for Java/C#/Python, ✅ TypeScript)
    - Add TR-API-MDL-001 thru TR-API-MDL-004 rows (mark ❌ for Java/C#/Python, ✅ TypeScript)
    - Add TR-API-BOT-002 thru TR-API-BOT-008 rows (mark ❌ for Java/C#/Python, ✅ TypeScript)
- [x] 0.2 Tag the following TypeScript test files as LEGACY by adding a banner comment at the top
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
- [x] 0.3 Update `docs-internal/architecture/adr/0038-shared-cross-platform-test-definitions.md`:
    - Add TR-API-EVT and TR-API-MDL to the planned categories table
    - Add TR-API-BOT-002 thru 008 to the BOT category
    - Add note about TypeScript-specific tests (Mappers/RuntimeAdapter/WebSocketHandler) as
      intentional language-layer exceptions
    - Add note about Python EnvVars gap and its planned resolution (this change)
    - Keep status as Proposed (will move to Accepted after implementation completes in Phase 5)

## Phase 1 — New shared JSON test definitions (Tier 1)

- [x] 1.1 Create `bot-api/tests/shared/event-priorities.json` covering:
    - TR-API-EVT-002: critical events (BotDeathEvent, WonRoundEvent, SkippedTurnEvent) return
      `isCritical = true`
    - TR-API-EVT-003: non-critical events return `isCritical = false` (BotHitBotEvent,
      BotHitWallEvent, BulletFiredEvent, BulletHitBotEvent, BulletHitBulletEvent,
      BulletHitWallEvent, HitByBulletEvent, ScannedBotEvent, CustomEvent, TeamMessageEvent)
    - TR-API-EVT-004: default event priorities match the constants in DefaultEventPriority
      (verify each of the 15 priority values)
- [x] 1.2 Create `bot-api/tests/shared/bullet-state.json` covering:
    - TR-API-MDL-001: BulletState `speed = 20 − 3 × power` for firepower values 0.1, 0.5, 1.0,
      2.0, 3.0, and the edge case power = 0.0 (speed = 20.0)
- [x] 1.3 Create `bot-api/tests/shared/bot-math.json` covering:
    - TR-API-BOT-002: `calcBearing(sourceDirection, targetDirection)` — use at least 4 representative
      angle pairs (same direction, 90°, 180°, −90°)
    - TR-API-BOT-003: `normalizeAbsoluteAngle` clamps to [0, 360) and
      `normalizeRelativeAngle` clamps to (−180, 180] — at least 6 boundary cases each
    - TR-API-BOT-004: `calcBulletSpeed = 20 − 3 × firepower` (same cases as MDL-001)
    - TR-API-BOT-005: `calcGunHeat = 1 + firepower / 5` for firepower 0.1, 1.0, 2.0, 3.0
    - TR-API-BOT-006: `calcMaxTurnRate` at speed 0, 5, 8, 10 (decreases as speed increases)
- [x] 1.4 Extend `bot-api/tests/shared/constants.json` with GameType constants:
    - `CLASSIC`, `MELEE`, `ONE_VS_ONE` string values

## Phase 2 — Fill Python gaps

- [x] 2.1 Create `bot-api/python/tests/bot_api/test_env_vars.py`:
    - Mirror the coverage of `EnvVars.test.ts` (and Java/C# equivalents):
        - Default values when env vars are not set
        - SERVER_URL parsing
        - BOT_NAME, BOT_VERSION, BOT_AUTHORS parsing
        - BOT_TEAM_ID parsing
        - BOT_INITIAL_POSITION parsing (valid and invalid formats)
        - BOT_SECRET parsing
    - Tag: `TR-API-ENV-001` (or whichever existing ENV IDs apply; add new ones if needed)
- [x] 2.2 Fill `bot-api/python/tests/bot_api/events/test_events.py`:
    - TR-API-EVT-001: All event class constructors store correct fields (test each event type)
    - TR-API-EVT-005: EventQueue dispatches events in priority order (higher first)
    - TR-API-EVT-006: EventQueue removes non-critical events older than MAX_EVENT_AGE turns
    - TR-API-EVT-007: EventQueue size does not exceed MAX_QUEUE_SIZE (256)
    - TR-API-EVT-008: `Condition.test()` is callable and subclass can override return value
    - TR-API-EVT-009: `CustomEvent` fires when its `Condition.test()` returns true

## Phase 3 — EventSystem tests for Java and C#

- [x] 3.1 Create `bot-api/java/src/test/java/.../EventSystemTest.java`:
    - TR-API-EVT-001: All event constructors store correct fields
    - TR-API-EVT-005: EventQueue priority ordering
    - TR-API-EVT-006: EventQueue age culling (non-critical events removed after MAX_EVENT_AGE)
    - TR-API-EVT-007: EventQueue size cap (MAX_QUEUE_SIZE = 256)
    - TR-API-EVT-008: Condition.test() callable; anonymous subclass override works
    - TR-API-EVT-009: CustomEvent dispatches when Condition.test() returns true
- [x] 3.2 Create `bot-api/dotnet/src/test/csharp/.../EventSystemTest.cs` with the same coverage

## Phase 4 — BaseBot math + data model tests for Java, C#, and Python

- [x] 4.1 Java: Add BaseBot math tests covering TR-API-BOT-002 thru BOT-006 (driven from
  `bot-math.json` where applicable, platform-native assertions otherwise)
- [x] 4.2 Java: Add data model tests covering TR-API-MDL-002 (BotState), MDL-003 (BotResults),
  MDL-004 (GameSetup) — assert all fields stored correctly via constructors
- [x] 4.3 C#: Same as 4.1 and 4.2 for C#
- [x] 4.4 Python: Same as 4.1 and 4.2 for Python (TR-API-BOT-002–006, MDL-002–004)
- [x] 4.5 All platforms: Add TR-API-BOT-007 (BaseBot state accessor defaults — getMyId=0,
  getEnergy=0, etc. when constructed without state)
- [x] 4.6 All platforms: Add TR-API-BOT-008 (adjustment flags default false —
  adjustGunForBodyTurn, adjustRadarForBodyTurn, adjustRadarForGunTurn)

## Phase 5 — Validate, update registry, delete LEGACY tests

- [x] 5.1 Run full test suite on all four platforms and verify all new TR-API IDs pass
- [x] 5.2 Update platform JSON runners to load new shared JSON definition files
  (`event-priorities.json`, `bullet-state.json`, `bot-math.json`)
- [x] 5.3 Flip all new TR-API IDs to ✅ in `TEST-REGISTRY.md` for the platforms now covered
- [x] 5.4 **Delete** all LEGACY-tagged TypeScript test files (confirmed ✅ on all platforms):
    - `bot-api/typescript/test/EventSystem.test.ts`
    - `bot-api/typescript/test/DefaultEventPriority.test.ts`
    - `bot-api/typescript/test/BotState.test.ts`
    - `bot-api/typescript/test/BotResults.test.ts`
    - `bot-api/typescript/test/BulletState.test.ts`
    - `bot-api/typescript/test/GameSetup.test.ts`
    - `bot-api/typescript/test/GameType.test.ts`
    - `bot-api/typescript/test/BaseBotInterfaces.test.ts`
- [x] 5.5 Update ADR-0038 status from Proposed → Accepted
- [x] 5.6 Run full test suite one final time to confirm no regressions

## Phase 6 — Promote EventQueue behaviors to Tier 1

EVT-005, 006, and 007 are currently Tier 2 (each platform wrote its own tests in its own
style). Making them Tier 1 enforces that the EventQueue produces *identical* outputs for
identical inputs on every platform, not just "something that looks correct."

Because EventQueue is stateful (events are added over time and culled relative to the
current turn), the JSON format must be extended with a **scenario** model: each test case
describes an ordered sequence of operations and an expected outcome, rather than a single
function call. All platform runners must implement a lightweight harness that drives these
scenarios against the native EventQueue type.

- [x] 6.1 Extend `bot-api/tests/shared/test-definition.schema.json` with a `scenario` test
  type for stateful step-sequences (add `steps` array and `expectAfter` assertion block)
- [x] 6.2 Create `bot-api/tests/shared/event-queue.json` covering:
    - TR-API-EVT-005: add several events with differing priorities at the same turn; expect
      the queue to return them ordered highest-priority-first
    - TR-API-EVT-006: add a mix of critical and non-critical events with turn numbers older
      than MAX_EVENT_AGE; expect non-critical ones removed, critical ones retained
    - TR-API-EVT-007: add MAX_QUEUE_SIZE + 1 (257) non-critical events in one turn; expect
      the queue size capped at exactly 256
- [x] 6.3 Tag the existing Tier 2 EVT-005/006/007 test methods on all four platforms as
  LEGACY — they will be deleted once the JSON runner is confirmed ✅:
    - Java `EventSystemTest.java` — add `@Tag("LEGACY")` to the three methods
    - C# `EventSystemTest.cs` — add `[Category("LEGACY")]` to the three methods
    - Python `test_events.py` — add `@pytest.mark.LEGACY` to the three functions
    - TypeScript `Tier2_EventSystem.test.ts` — add `// LEGACY:` banner comment to the three
      `describe` blocks
- [x] 6.4 Update all four platform shared-test runners (Java, C#, Python, TypeScript) to
  load and execute `event-queue.json` scenarios against their native EventQueue
- [x] 6.5 Update `TEST-REGISTRY.md`: flip TR-API-EVT-005, EVT-006, EVT-007 from Tier 2 → Tier 1
- [x] 6.6 Run full test suite and confirm all platforms pass the new JSON-driven scenarios
- [x] 6.7 **Delete** the LEGACY-tagged EVT-005/006/007 test methods on all four platforms
  (confirmed ✅ via JSON runner):
    - Java `EventSystemTest.java`
    - C# `EventSystemTest.cs`
    - Python `test_events.py`
    - TypeScript `Tier2_EventSystem.test.ts`
- [x] 6.8 Run full test suite and confirm no regressions

## Phase 7 — Promote BaseBot defaults to Tier 1

BOT-007 and BOT-008 are currently Tier 2. Defining the expected defaults in shared JSON
prevents silent drift where one platform quietly changes which accessors throw and which
return a zero/empty safe value.

The JSON must distinguish three accessor behaviors:

- **throws** — accessor throws when no server state is available
- **returns** — accessor returns a specific safe default (e.g., `0`, `false`, `""`)
- **returnsEmpty** — accessor returns an empty collection

- [x] 7.1 Create `bot-api/tests/shared/basebot-defaults.json` covering:
    - TR-API-BOT-007: classify every public state accessor as `throws`, `returns: <value>`,
      or `returnsEmpty` for the not-yet-connected state (e.g., `getMyId` → throws,
      `getSpeed` → returns 0, `getBulletStates` → returnsEmpty)
    - TR-API-BOT-008: all three adjustment flags (`adjustGunForBodyTurn`,
      `adjustRadarForBodyTurn`, `adjustRadarForGunTurn`) → `returns: false`
- [x] 7.2 Update all four platform shared-test runners to consume `basebot-defaults.json`
- [x] 7.3 Update `TEST-REGISTRY.md`: flip TR-API-BOT-007, BOT-008 from Tier 2 → Tier 1
- [x] 7.4 Run full test suite and confirm no regressions

## Phase 8 — Protocol and internal-layer parity audit

TypeScript's `WebSocketHandler.test.ts` (protocol message routing) and `Mappers.test.ts`
(JSON → domain object mapping) together cover protocol conformance scenarios that are
implicitly required on all platforms but have never been added to the registry. The goal
is to extract those cross-platform requirements, register them as TR-API-TCK IDs, and
close the coverage gap on Java, C#, and Python.

`RuntimeAdapter.test.ts` (Node-vs-browser detection) is TypeScript-architecture-specific
and needs no counterpart on other platforms.

- [x] 8.1 Catalogue `WebSocketHandler.test.ts` — classify each `describe` block as one of:
    - **cross-platform requirement** (e.g., "ServerHandshake triggers BotHandshake reply
      containing the bot's name and secret", "GameStarted triggers onGameStarted callback")
    - **TypeScript-architecture-specific** (e.g., createWebSocket injection, ws.close proxy)
- [x] 8.2 For each cross-platform requirement identified in 8.1, add a new TR-API-TCK ID
  to `TEST-REGISTRY.md` (marked ❌ for every platform that lacks equivalent coverage)
- [x] 8.3 Catalogue `Mappers.test.ts` — classify each mapper scenario as:
    - **cross-platform requirement** (the correctness of deserializing protocol JSON into a
      domain object is required everywhere, even if done via Gson/Jackson/Pydantic)
    - **TypeScript-architecture-specific** (the Mapper class itself has no equivalent)
    - For cross-platform requirements, determine whether they are already covered under
      existing TR-API-MDL or TR-API-TCK IDs; add new IDs only for genuine gaps
- [x] 8.4 Tag all TypeScript test blocks confirmed as cross-platform requirements in 8.1 and
  8.3 as LEGACY — they will be deleted once Java/C#/Python implement the new TR-API-TCK IDs:
    - `WebSocketHandler.test.ts` — add `// LEGACY: Superseded by TR-API-TCK-xxx. Delete once
    all platforms are ✅.` banner comment to each qualifying `describe` block
    - `Mappers.test.ts` — same banner on each qualifying `describe` block
- [x] 8.5 Implement all newly-added ❌ TR-API-TCK IDs on Java, C#, and Python
- [x] 8.6 Document `RuntimeAdapter.test.ts` as a TypeScript-architecture-specific exception
  in ADR-0038 (alongside the existing Mappers/WebSocketHandler note)
- [x] 8.7 Run full test suite and confirm all platforms pass the new TR-API-TCK tests
- [x] 8.8 **Delete** all LEGACY-tagged `describe` blocks in `WebSocketHandler.test.ts` and
  `Mappers.test.ts` (confirmed ✅ on all platforms)
- [x] 8.9 Run full test suite and confirm no regressions
