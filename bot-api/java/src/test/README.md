# Tank Royale Bot API — Java Tests (JUnit 5)

This folder contains Java tests for the Bot API. Java is the reference implementation — semantics, defaults, and timing must be mirrored 1‑1 by .NET and Python. See the shared matrix at `../../tests/TEST-MATRIX.md`.

## How to run
- Gradle: `./gradlew :bot-api:java:test`

## Parity policy
- Keep test IDs and names aligned with `.NET` and `Python` (see TEST-MATRIX.md).
- Prefer public-behavior testing; use reflection only for large internal methods where unavoidable.
- Embed the canonical ID/title in tests using `@DisplayName("TR-API-<ID> <short description>")` on classes or methods as appropriate.

## Concrete PR checklist (Java)
Use this checklist when adding or updating tests.

General
- [ ] Follow `TEST-MATRIX.md` IDs and naming.
- [ ] Use JUnit 5 (`@Test`, `@DisplayName` as needed).
- [ ] Keep assertions semantically identical to other languages.
- [ ] Update this checklist status in your PR description.

Scaffolding/utilities
- [ ] Ensure `test_utils/MockedServer` supports: handshake, schema-valid ticks, event injection, intent capture.
- [ ] Extend only as needed for BOT/CMD/EVT/TCK tests.

Value objects and constants (VAL)
- [ ] TR-API-VAL-001 BotInfo required fields
- [ ] TR-API-VAL-002 BotInfo validation
- [ ] TR-API-VAL-003 InitialPosition defaults
- [ ] TR-API-VAL-004 InitialPosition mapping round-trip
- [ ] TR-API-VAL-005 Constants integrity
- [ ] TR-API-VAL-006 Graphics Color
- [ ] TR-API-VAL-007 Graphics Point

Utilities (UTL)
- [ ] TR-API-UTL-001 ColorUtil conversions
- [ ] TR-API-UTL-002 JsonUtil serialization + schema compliance
- [ ] TR-API-UTL-003 CountryCode utility

Graphics (GFX)
- [ ] TR-API-GFX-001 SvgGraphics path basics
- [ ] TR-API-GFX-002 SvgGraphics styles
- [ ] TR-API-GFX-003 SvgGraphics text/images (if applicable)
- [ ] TR-API-GFX-004 IGraphics contract

Bot lifecycle/config (BOT)
- [ ] TR-API-BOT-001 BaseBot constructor (expand `BaseBotConstructorTest` for env default parity)
- [ ] TR-API-BOT-002 Connect/Disconnect (new: `lifecycle/ConnectionLifecycleTest.java`)
- [ ] TR-API-BOT-003 Start/Stop/Pause/Resume (new: `lifecycle/StartStopPauseResumeTest.java`)
- [ ] TR-API-BOT-004 Error handling (disconnect/protocol error)

Commands (CMD)
- [ ] TR-API-CMD-001 Movement commands (new: `commands/CommandsTest.java`)
- [ ] TR-API-CMD-002 Fire command
- [ ] TR-API-CMD-003 Radar/Scan commands
- [ ] TR-API-CMD-004 Graphics frame emission

Events (EVT)
- [ ] TR-API-EVT-001 GameStartedEvent
- [ ] TR-API-EVT-002 RoundStartedEvent
- [ ] TR-API-EVT-003 ScannedBotEvent
- [ ] TR-API-EVT-004 SkippedTurnEvent
- [ ] TR-API-EVT-005 Interruption semantics

Ticks and state (TCK)
- [ ] TR-API-TCK-001 Tick progression (immutability/read-only)
- [ ] TR-API-TCK-002 Intent apply/reset
- [ ] TR-API-TCK-003 Timeouts

Protocol and schema (PRT)
- [ ] TR-API-PRT-001 Outgoing messages validate against `/schema`
- [ ] TR-API-PRT-002 Incoming messages parse to DTOs and back to JSON losslessly
- [ ] TR-API-PRT-003 Backward compatibility for unknown fields

Internals (INT)
- [ ] TR-API-INT-001 Internal mapping helpers (e.g., `mapper/InitialPositionMapperTest.java`)
- [ ] TR-API-INT-002 Event dispatch loop (e.g., `internal/InternalDispatchTest.java`)

Notes
- Keep diffs minimal and focused.
- Avoid changing public API or protocol semantics in tests; align with Java reference behavior.
