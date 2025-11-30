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

- [ ] `server-handshake.schema.yaml`
  - Verify all advertised capabilities and server version fields.
  - Confirm optional vs required. Ensure alignment with server handshake sender.
- [ ] `bot-handshake.schema.yaml`
  - Check bot metadata fields (e.g., `name`, `version`, `authors`, `countryCode`, `gameTypes`).
  - Validate enums, arrays, and optional fields (e.g., `teamName`, `private` flags if applicable).
- [ ] `game-started-event-for-bot.schema.yaml`
  - Ensure embedded `gameSetup` structure matches server (dimensions, rates, timeouts, etc.).
- [ ] `round-started-event.schema.yaml`
  - Confirm minimal fields and turn numbering.
- [ ] `tick-event-for-bot.schema.yaml`
  - Validate `roundNumber`, `turnNumber`, `botState`, optional `events`, `bulletStates`.
  - Ensure `events` can be empty list; validate polymorphic event union discriminator.
  - Confirm numeric ranges (e.g., directions in degrees, speeds, energy, gunHeat).

#### Bot state and intent

- [ ] `bot-state.schema.yaml`
  - Confirm presence and types of: `isDroid`, `energy`, `x`, `y`, `direction`, `gunDirection`, `radarDirection`, `radarSweep`, `speed`, `turnRate`, `gunTurnRate`, `radarTurnRate`, `gunHeat`, `enemyCount`, debug/colors if serialized by server.
  - Clarify optional vs required for cosmetic fields (colors, debugging flags) based on actual server output.
- [ ] `bot-intent.schema.yaml`
  - Validate optionality of `turnRate`, `gunTurnRate`, `radarTurnRate`, `targetSpeed`, `firepower`.
  - Confirm `firepower` is omitted (null) unless firing is allowed. Enforce min/max for movement and firepower.

#### Events (polymorphic)

- [ ] `scanned-bot-event.schema.yaml`
  - Confirm all numeric fields and IDs; check `turnNumber` semantics.
- [ ] `bot-hit-bot-event.schema.yaml`
  - Verify actor/subject IDs and positions; check damage fields.
- [ ] `bot-hit-wall-event.schema.yaml`
  - Validate position and impact fields.
- [ ] `bot-death-event.schema.yaml`
  - Confirm cause/source fields.
- [ ] `bullet-fired-event.schema.yaml`
  - Ensure alignment with projectile state creation timing.
- [ ] `bullet-hit-bot-event.schema.yaml`
  - Verify hit data and energy transfer.
- [ ] `bullet-hit-wall-event.schema.yaml`
  - Validate impact coordinates and bullet state changes.

#### Collections and updates

- [ ] `bot-list-update.schema.yaml`
  - Verify add/remove payloads and minimal fields.
- [ ] `message-event.schema.yaml` / `team-message.schema.yaml`
  - Validate message size, sender/recipient IDs, per-turn limits.

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

#### Execution tracking

Maintain progress directly in this file. For each checked item, add short notes and link to commit/PR.
