### Protocol Schemas — Incremental Upgrade and Verification Plan

This plan is the single source of truth for auditing and aligning all protocol schemas with the actual server behavior. It is designed so any contributor (human or AI) can pick up a checklist item and continue the work deterministically.

#### Principles

- Server is the source of runtime truth. Schemas must reflect the server’s wire format exactly (naming, nullability, required/optional, enums, value ranges, and timing semantics).
- Java schema code is generated via jsonschema2pojo in `server`; .NET and Python bindings must be regenerated from the same canonical schemas.
- Changes must be additive where possible. For breaking changes, bump versions coherently, update codegen and consumers across repos.
- Keep a clean diff between current server serialization and schema assertions. When ambiguity exists, write a minimal reference test in server to assert the exact JSON payload.

#### How to execute each checklist item

1. Locate schema file under `schema/schemas/<name>.schema.yaml` and its generated model in each language.
2. Capture a live sample payload from the server for the specific message/event (unit/integration test or mocked-server trace).
3. Compare sample payload vs schema: field names, types, nullability, required lists, enums, default values, min/max ranges.
4. If schema mismatch:
   - Update YAML schema accordingly.
   - Regenerate language bindings (Java: server build; .NET/Python: respective codegen scripts/projects).
5. Add minimal wire test(s) to the server to lock the payload shape (serialization test or round‑trip test).
6. Update this document’s checklist item with status and notes; include references to commits/PRs.

Legend: [ ] Pending, [*] In progress, [✓] Done, [!] Blocked

#### Core handshake and lifecycle

- [✓] `server-handshake.schema.yaml`
  - Verified against `ClientWebSocketsHandler.addSocketAndSendServerHandshake(...)`.
  - Fields sent by server: `type`, `sessionId`, `name`, `variant`, `version`, `gameTypes`, `gameSetup`.
  - Required in schema: `sessionId`, `variant`, `version`, `gameTypes` — matches server behavior.
  - `gameTypes` is a set on server; schema enforces `uniqueItems: true` and `minItems: 1` — OK.
  - `gameSetup` is only present when a game is running; server omits it when null. Not required in schema — OK.
  - No explicit capability list is advertised by server at this time; version fields align (`variant`, `version`).
  - No changes needed; schema matches current server serialization. (Verified 2025-11-30)
- [✓] `bot-handshake.schema.yaml`
  - Verified against:
    - Server: `ClientWebSocketsHandler.handleBotHandshake(...)` (parses `BotHandshake`, validates `sessionId` and optional `secret`).
    - Bot API (Java reference): `bot-api/java/.../internal/BotHandshakeFactory.java` (sets all fields populated by bots).
    - Generated model: `server/build/generated-sources/schema/.../BotHandshake.java` (confirms required fields from schema).
  - Fields sent by bots (from Bot API factory): `type`, `sessionId`, `name`, `version`, `authors`, `description`, `homepage`, `countryCodes`, `gameTypes`, `platform`, `programmingLang`, `initialPosition`, `teamId`, `teamName`, `teamVersion`, `isDroid`, `secret`.
  - Schema required: `sessionId`, `name`, `version`, `authors` — matches the Java reference and generated model expectations.
  - `secret` is optional in schema; server only enforces it when a bot-secret list is configured, which aligns with handler logic.
  - Team fields and `initialPosition` are optional in schema; bots may include them via env/config — OK.
  - No changes needed; schema matches current client (Bot API) payload and server expectations. (Verified 2025-11-30)
- [✓] `game-started-event-for-bot.schema.yaml`
  - Verified against server emission in `GameServer.createGameStartedEventForBot(...)` and consumers in Bot APIs.
  - Server sets: `type = GameStartedEventForBot`, `myId` (required), `gameSetup` (required), `teammateIds` (optional, may be empty), and optionally `startX`, `startY`, `startDirection` when initial model data exists. Schema marks `myId` and `gameSetup` as required and others optional — matches.
  - `gameSetup` is mapped via `GameSetupMapper.map(...)` and references `game-setup.schema.yaml` — structure aligns with mapper usage.
  - Java generated model (`server/build/generated-sources/schema/.../GameStartedEventForBot.java`) reflects the same fields and required list.
  - No changes needed; schema matches current server serialization and Bot API expectations. (Verified 2025-11-30)
- [✓] `round-started-event.schema.yaml`
  - Verified against server emission in `GameServer.broadcastRoundStartedToAll(...)`.
  - Server broadcasts `RoundStartedEvent` to all (participants, observers, controllers) with fields: `type = RoundStartedEvent` and `roundNumber` only. Emitted on the first turn of each round via `onNextTick(...): if (turnNumber == 1)`.
  - Schema requires `roundNumber` and has no other properties — matches server serialization.
  - Clarified docs: Added explicit note that numbering is 1-based for `roundNumber`/`turnNumber` (no protocol change).
  - Generated model confirms the same shape: `server/build/generated-sources/schema/.../RoundStartedEvent.java`.
  - No changes needed; schema matches current server behavior. (Verified 2025-11-30)
- [✓] `tick-event-for-bot.schema.yaml`
  - Verified against server mapping `TurnToTickEventForBotMapper.map(...)` and `EventsMapper`.
  - Server sets: `type = TickEventForBot`, `roundNumber`, `turnNumber` (from base `event.schema.yaml`), `botState`, `bulletStates`, and `events`.
  - Arrays present every tick: mapper always assigns `bulletStates` (may be empty) and `events` = mapped set (may be empty). No `minItems` in schema — OK.
  - Schema requires `roundNumber`, `botState`, `bulletStates`, `events`, and inherits required `turnNumber` from `event.schema.yaml` — matches server payload.
  - Polymorphic `events` use per-item `type` discriminator (e.g., `ScannedBotEvent`, `BulletHitBotEvent`, etc.); schema references `event.schema.yaml` and concrete event schemas handle shapes — OK.
  - 1-based numbering documentation already clarified for `roundNumber`/`turnNumber` — matches server semantics.
  - Numeric ranges for values reside in `bot-state.schema.yaml` / `bullet-state.schema.yaml` and will be validated in their dedicated tasks.
  - No changes needed; schema matches current server serialization. (Verified 2025-11-30)

#### Bot state and intent

- [✓] `bot-state.schema.yaml`
  - Verified against server mapper `server/src/main/kotlin/dev/robocode/tankroyale/server/mapper/BotToBotStateMapper.kt` and generated model `server/build/generated-sources/schema/.../BotState.java`.
  - Fields set by server each tick for alive bots: `isDroid`, `energy`, `x`, `y`, `speed`, `turnRate`, `gunTurnRate`, `radarTurnRate`, `direction`, `gunDirection`, `radarDirection`, `radarSweep`, `gunHeat`, `enemyCount`, optional colors (`bodyColor`, `turretColor`, `radarColor`, `bulletColor`, `scanColor`, `tracksColor`, `gunColor`), and `isDebuggingEnabled`.
  - Schema required list matches what server always sets: `energy`, `x`, `y`, `direction`, `gunDirection`, `radarDirection`, `radarSweep`, `speed`, `turnRate`, `gunTurnRate`, `radarTurnRate`, `gunHeat`, `enemyCount`. Cosmetic fields and `isDebuggingEnabled` are optional — matches server (nulls omitted by Gson).
  - Documentation clarified (non‑breaking): angles for `direction`, `gunDirection`, `radarDirection` are normalized to [0, 360); `radarSweep` is in [0, 360). No protocol/shape changes.
  - No further schema changes needed; server enforces value ranges at runtime. (Verified 2025-11-30)
- [✓] `bot-intent.schema.yaml`
  - Verified against server mapper and execution flow:
    - Mapping: `server/src/main/kotlin/dev/robocode/tankroyale/server/mapper/BotIntentMapper.kt` (nulls → defaults; fields optional and interpreted as "no change").
    - Execution: `server/src/main/kotlin/dev/robocode/tankroyale/server/core/ModelUpdater.kt`
      - Movement clamping in `updateBotTurnRatesAndDirections(...)` via `limitTurnRate`, `limitGunTurnRate`, `limitRadarTurnRate`.
      - Speed/accel handled in rules: `server/src/main/kotlin/dev/robocode/tankroyale/server/rules/math.kt` (`calcNewBotSpeed`, `clampSpeed`, etc.).
      - Firing gated in `coolDownAndFireGuns()`/`checkIfGunMustFire()` (requires `gunHeat == 0` and `energy > firepower`), and power clamped in `fireBullet(...)`.
  - Schema updates (documentation-only plus non-breaking relaxation):
    - Clarified that movement rates/speed are clamped server-side; omitting fields means no change.
    - Documented firing rules and clamping; kept `minimum: 0.0`, `maximum: 3.0` and removed `exclusiveMinimum` so `0.0` is allowed to represent "no fire" (matches server behavior where firing only occurs when `firepower >= MIN_FIREPOWER`).
    - Added defaults notes for `rescan` (false) and `fireAssist` (true) to reflect server intent defaults.
  - No protocol shape change; fields and required list unchanged. Relaxing the lower bound is backward-compatible for clients. (Verified 2025-11-30)

#### Events (polymorphic)

- [✓] `scanned-bot-event.schema.yaml`
  - Verified against server mapping in `server/src/main/kotlin/dev/robocode/tankroyale/server/mapper/EventsMapper.kt` (ScannedBotEvent): sets `turnNumber`, `scannedByBotId`, `scannedBotId`, `energy`, `x`, `y`, `direction`, `speed`.
  - Event creation: produced from `ModelUpdater.checkAndHandleScans()/handleScannedBot()/createAndAddScannedBotEventToTurn(...)` with values taken from the scanned bot at the moment of detection; `direction` comes from bot model which is normalized to [0, 360) during updates.
  - Schema extends `event.schema.yaml` and inherits required `turnNumber` (1‑based). Required list includes all event-specific fields — matches server payload.
  - Documentation clarified: `direction` now explicitly states angles are normalized to the [0, 360) range (non‑breaking clarification).
  - No other changes needed; schema matches current server serialization. (Verified 2025-11-30)
- [✓] `bot-hit-bot-event.schema.yaml`
  - Verified against server mapping in `server/src/main/kotlin/dev/robocode/tankroyale/server/mapper/EventsMapper.kt` and creation in `server/src/main/kotlin/dev/robocode/tankroyale/server/core/ModelUpdater.kt#handleBotHitBot(...)`.
  - Server sets: `type = BotHitBotEvent`, `turnNumber` (inherited from base `event.schema.yaml`), `botId` (attacker), `victimId`, `energy` (victim remaining after collision), `x`, `y` (victim position), and `rammed` (true if attacker rammed victim).
  - Schema fix: corrected `$schema` key typo (was `$schema"`) and clarified descriptions for `energy`, `x`, `y`, and `rammed`. Required fields unchanged: `botId`, `victimId`, `energy`, `x`, `y`, `rammed` — matches server payload.
  - No protocol changes; documentation-only clarification plus YAML syntax fix. (Verified 2025-11-30)
- [✓] `bot-hit-wall-event.schema.yaml`
  - Verified against server mapping in `server/src/main/kotlin/dev/robocode/tankroyale/server/mapper/EventsMapper.kt` (BotHitWallEvent) and creation in `server/src/main/kotlin/dev/robocode/tankroyale/server/core/ModelUpdater.kt#checkAndHandleBotWallCollisions()`.
  - Server creates `BotHitWallEvent(turnNumber, bot.id)` on wall collision and throttles duplicates within consecutive turns; mapper sets `turnNumber` (inherited from base `event.schema.yaml`) and `victimId`.
  - Schema extends `event.schema.yaml` and requires `victimId` only — matches server payload. `turnNumber` is required via the base schema and documented as 1‑based.
  - Cross‑checked generated model `server/build/generated-sources/schema/.../BotHitWallEvent.java` — fields match.
  - No changes needed; schema matches current server serialization. (Verified 2025-11-30)
- [✓] `bot-death-event.schema.yaml`
  - Verified against server creation and mapping.
  - Server creation: `server/src/main/kotlin/dev/robocode/tankroyale/server/core/ModelUpdater.kt#checkAndHandleDefeatedBots()` creates `BotDeathEvent(turn.turnNumber, botId)` for each defeated bot and adds it to both bot and observer event streams.
  - Mapping: `server/src/main/kotlin/dev/robocode/tankroyale/server/mapper/EventsMapper.kt#map(BotDeathEvent)` sets `type`, inherits `turnNumber` from base `event.schema.yaml`, and sets `victimId`.
  - Schema shape matches: `schema/schemas/bot-death-event.schema.yaml` defines `victimId` only and extends `event.schema.yaml` (which provides required 1‑based `turnNumber`). Required list aligns with server payload.
  - No changes needed; documentation-only tracking updated. (Verified 2025-11-30)
- [✓] `bullet-fired-event.schema.yaml`
  - Verified against server creation and mapping:
    - Creation: `server/src/main/kotlin/dev/robocode/tankroyale/server/core/ModelUpdater.kt#fireBullet(...)` constructs `BulletFiredEvent(turn.turnNumber, bullet.copy())` right after spawning the bullet and before energy deduction.
    - Mapping: `server/src/main/kotlin/dev/robocode/tankroyale/server/mapper/EventsMapper.kt#map(BulletFiredEvent)` sets `type`, `turnNumber` (from base `event`), and `bullet = BulletToBulletStateMapper.map(bullet)`.
    - Bullet snapshot mapping: `server/src/main/kotlin/dev/robocode/tankroyale/server/mapper/BulletToBulletStateMapper.kt` assigns `ownerId`, `bulletId`, normalized `direction`, `power`, `x`, `y`, and `color`.
  - Documentation clarified in schema (non‑breaking): the `bullet` description now states it’s a snapshot at fire time; direction normalized to [0, 360); firepower clamped to [0.1, 3.0] by server; color equals bot’s bullet color at fire time and does not change afterwards.
  - No protocol shape changes; required fields unchanged. (Verified 2025-11-30)
- [✓] `bullet-hit-bot-event.schema.yaml`
  - Verified against server creation and mapping:
    - Creation: `server/src/main/kotlin/dev/robocode/tankroyale/server/core/ModelUpdater.kt#handleBulletHittingBot(...)` creates `BulletHitBotEvent(turn.turnNumber, bullet, victimId, damage, bot.energy)` when a bullet hits a bot.
    - Mapping: `server/src/main/kotlin/dev/robocode/tankroyale/server/mapper/EventsMapper.kt#map(BulletHitBotEvent)` sets `type`, `turnNumber` (from base `event.schema.yaml`), `bullet = BulletToBulletStateMapper.map(bullet)`, `victimId`, `damage` (calculated via `calcBulletDamage(bullet.power)`), and `energy` (victim's remaining energy after damage).
  - Schema shape matches: extends `event.schema.yaml` (provides required `turnNumber`), requires `victimId`, `bullet`, `damage`, and `energy` — all fields present in server payload.
  - The `bullet` field is a snapshot of the bullet state at the moment of impact (mapped via `BulletToBulletStateMapper`).
  - Damage calculation: `damage = calcBulletDamage(bullet.power)` as per server rules; `energy` is the victim's energy after `bot.addDamage(damage)`.
  - Generated model `server/build/generated-sources/schema/.../BulletHitBotEvent.java` confirms the same structure.
  - No changes needed; schema matches current server serialization. (Verified 2025-11-30)
- [✓] `bullet-hit-wall-event.schema.yaml`
  - Verified against server mapping in `server/src/main/kotlin/dev/robocode/tankroyale/server/mapper/EventsMapper.kt#map(BulletHitWallEvent)` which sets `type`, inherits `turnNumber` from base `event.schema.yaml`, and maps `bullet` via `BulletToBulletStateMapper.map(bullet)`.
  - Schema doc clarification (non‑breaking): described `bullet` as a snapshot at impact time; direction normalized to [0, 360); coordinates (`x`, `y`) represent the impact location on that turn; color equals bullet color at impact; bullet ceases to exist after collision.
  - Required fields unchanged: `bullet` (plus inherited `turnNumber`). No protocol shape changes needed; matches server serialization and generated models.
  - Changes in commit: updated `schema/schemas/bullet-hit-wall-event.schema.yaml` description only. (Verified 2025-11-30)

#### Collections and updates

- [✓] `bot-list-update.schema.yaml`
  - Verified against server emission in `GameServer`:
    - Maintained snapshot `botListUpdateMessage` updated via `updateBotListUpdateMessage()`; broadcast on join/leave via `sendBotListUpdateToObserversAndControllers()` and sent to a newly connected client via `sendBotListUpdate(conn)`.
    - Cloned message sets `type = BotListUpdate` and copies `bots` list to avoid races (`cloneBotListUpdate(...)`).
    - Bots list is built from current connections using `BotHandshakeToBotInfoMapper.map(...)`, which maps `BotHandshake` fields and adds `host`/`port`.
  - Schema doc clarification (non‑breaking): `description` now states it is a full replacement snapshot, may be empty, order unspecified; item references `bot-info.schema.yaml` which already requires `host` and `port` in addition to handshake fields.
  - Required fields unchanged: inherits required `type` from `message.schema.yaml`; keeps `bots` required. No protocol/shape change. (Verified 2025-11-30)
- [✓] `team-message-event.schema.yaml`
  - Verified against server creation and mapping:
    - Creation: `server/src/main/kotlin/dev/robocode/tankroyale/server/core/ModelUpdater.kt#processTeamMessages(...)` emits
      `TeamMessageEvent(turn.turnNumber, message, messageType, bot.id)` either to a specific `receiverId` or all teammates when broadcasting.
    - Limits enforced by server (source of truth):
      - `MAX_TEAM_MESSAGE_SIZE = 4096` (`server/src/main/kotlin/dev/robocode/tankroyale/server/rules/rules.kt#L94`). If a message exceeds this size, that message and any following messages for the same turn are ignored (see guard in `processTeamMessages`).
      - `MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN = 5` (`rules.kt#L97`), enforced via `coerceAtMost(...)` over the intent list.
    - Mapping: `server/src/main/kotlin/dev/robocode/tankroyale/server/mapper/EventsMapper.kt#map(TeamMessageEvent)` sets `type`, inherits `turnNumber` from base `event.schema.yaml`, and sets `message`, `messageType`, and `senderId`.
  - Schema updates:
    - `schema/schemas/team-message-event.schema.yaml`: Reduced `message.maxLength` from 32768 to 4096 to match server runtime limit. Expanded description to clarify 1‑based `turnNumber` inheritance, delivery semantics (private/broadcast to teammates), and server-side limits/ignore behavior when size exceeded. No field additions/removals.
  - Compatibility: Wire shape unchanged; aligning maxLength with actual server constraint prevents oversized messages in generated clients. (Verified 2025-11-30)
  - Note: There is no `message-event.schema.yaml` in the repository; prior checklist item name was a typo. The correct schema is `team-message-event.schema.yaml`.

#### Server control/utility

- [ ] `pause-game.schema.yaml` / `resume-game.schema.yaml`
  - Confirm presence and any metadata.
- [ ] `abort-game.schema.yaml`
  - Validate reason codes and required fields.

#### Cross-language binding regeneration tasks

- [ ] Java (server): `jsonschema2pojo` generates models from `schema/schemas`.
  - Action: run `./gradlew :server:clean :server:build`. Verify generated classes match updated YAML.
- [ ] .NET: Update codegen (if any) or schema-derived models.
  - Action: run `.\gradlew :bot-api:dotnet:clean :bot-api:dotnet:test` after regeneration.
- [ ] Python: Use `bot-api/python/scripts/schema_to_python.py` to regenerate.
  - Action: run `./gradlew :bot-api:python:setupVenv` then Python unit tests.

#### Known risks and investigation hooks

- Polymorphic `events` on Tick: some language bindings need explicit type discriminator handling. Consider simplifying or adding a stable discriminator field in schemas if missing.
- Timing semantics: if the server occasionally omits fields (e.g., colors/debug flags), mark as optional in schema and update consumers accordingly.
- Fire command gating: confirm `firepower` omission when gun on cooldown or insufficient energy. Add a server-side serialization test to lock behavior.

#### Documentation clarifications (non-breaking)

- Numbering base: `roundNumber` and `turnNumber` are 1-based (first round/turn is 1). Schema descriptions updated across relevant files (`event`, `round-started`, `round-ended-*`, `tick-event-*`).

#### Execution tracking

Maintain progress directly in this file. For each checked item, add short notes and link to commit/PR.
