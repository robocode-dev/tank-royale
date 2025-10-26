# Tank Royale Bot API — Python Tests (pytest)

This folder contains Python tests for the Bot API. Tests must maintain strict 1‑1 parity with Java (reference) and .NET. See the shared matrix at `../../tests/TEST-MATRIX.md`.

## How to run
- Gradle: `./gradlew :bot-api:python:test` (runs `pytest`)
- Direct: `pytest -q` (from this folder)

## Parity policy
- Keep test IDs and names aligned with Java and .NET (see TEST-MATRIX.md).
- Prefer public-behavior testing. Import underscore-prefixed helpers only when large internals are otherwise untestable.

## Concrete PR checklist (Python)
Use this checklist when adding or updating tests.

General
- [ ] Follow `TEST-MATRIX.md` IDs and naming.
- [ ] Use pytest style; keep tests pure and deterministic.
- [ ] Keep assertions semantically identical to other languages.
- [ ] Update this checklist status in your PR description.

Scaffolding/utilities
- [ ] Provide a simple mocked server/harness mirroring Java/.NET `MockedServer` (handshake, schema-valid ticks, event injection, intent capture).
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
- [ ] TR-API-BOT-001 BaseBot constructor
- [ ] TR-API-BOT-002 Connect/Disconnect (new: `bot_api/lifecycle/test_connection.py`)
- [ ] TR-API-BOT-003 Start/Stop/Pause/Resume (new: `bot_api/lifecycle/test_start_stop_pause_resume.py`)
- [ ] TR-API-BOT-004 Error handling (disconnect/protocol error)

Commands (CMD)
- [ ] TR-API-CMD-001 Movement commands (new: `bot_api/commands/test_commands.py`)
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
- [ ] TR-API-INT-001 Internal mapping helpers (e.g., `mapper/test_initial_position_mapper.py`)
- [ ] TR-API-INT-002 Event dispatch loop (e.g., `internal/test_internal_dispatch.py`)

Notes
- Keep diffs minimal and focused.
- Avoid changing public API or protocol semantics in tests; align with Java reference behavior.
