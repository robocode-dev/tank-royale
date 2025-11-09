### Tank Royale Bot API — Wasm Parity Checklist (Java reference)

Purpose
- Single place to track Kotlin/Wasm API parity against the Java Bot API (source of truth).
- Used by contributors/LLM agents during Milestone 1 and updated continuously.

How to use
- For each Java public type, add a row with mapping to the Kotlin/Wasm FQN and current status.
- Keep status in {TODO, Partial, Done}. Use notes to capture any intentional, documented idiomatic differences.
- Link matrix IDs from bot-api/tests/TEST-MATRIX.md that cover the type/behavior.

Legend
- Area: VAL, UTL, GFX, BOT, CMD, TCK, EVT, PRT, INT
- Status: TODO (not started), Partial (compiles, missing parts/tests), Done (implemented + tests passing)

Checklist table (start with top-level packages; expand as you enumerate Java API)

| Area | Java FQN | Wasm/Kotlin FQN | Status | Covered by Test IDs | Notes |
|------|----------|------------------|--------|---------------------|-------|
| VAL | dev.robocode.tankroyale.api.BotInfo | dev.robocode.tankroyale.api.BotInfo | TODO | TR-API-VAL-001, TR-API-VAL-002 | |
| VAL | dev.robocode.tankroyale.api.InitialPosition | dev.robocode.tankroyale.api.InitialPosition | TODO | TR-API-VAL-003, TR-API-VAL-004 | |
| VAL | dev.robocode.tankroyale.api.Color | dev.robocode.tankroyale.api.Color | TODO | TR-API-VAL-006 | |
| VAL | dev.robocode.tankroyale.api.Point | dev.robocode.tankroyale.api.Point | TODO | TR-API-VAL-007 | |
| UTL | dev.robocode.tankroyale.api.util.ColorUtil | dev.robocode.tankroyale.api.util.ColorUtil | TODO | TR-API-UTL-001 | |
| UTL | dev.robocode.tankroyale.api.util.JsonUtil | dev.robocode.tankroyale.api.util.JsonUtil | TODO | TR-API-UTL-002 | |
| UTL | dev.robocode.tankroyale.api.util.CountryCode | dev.robocode.tankroyale.api.util.CountryCode | TODO | TR-API-UTL-003 | |
| GFX | dev.robocode.tankroyale.api.IGraphics | dev.robocode.tankroyale.api.gfx.IGraphics | TODO | TR-API-GFX-004 | |
| GFX | dev.robocode.tankroyale.api.SvgGraphics | dev.robocode.tankroyale.api.gfx.SvgGraphics | TODO | TR-API-GFX-001..003 | |
| PRT | dev.robocode.tankroyale.api.schema.* | dev.robocode.tankroyale.api.proto.* | TODO | TR-API-PRT-001..003 | Align fields with /schema |
| BOT | dev.robocode.tankroyale.api.BaseBot | dev.robocode.tankroyale.api.BaseBot | TODO | TR-API-BOT-001a..e, 002..004 | Wasm: no Java System props |
| CMD | dev.robocode.tankroyale.api.Command* | dev.robocode.tankroyale.api.Command* | TODO | TR-API-CMD-001..004 | |
| TCK | dev.robocode.tankroyale.api.Tick* | dev.robocode.tankroyale.api.Tick* | TODO | TR-API-TCK-001..003 | |
| EVT | dev.robocode.tankroyale.api.events.* | dev.robocode.tankroyale.api.events.* | TODO | TR-API-EVT-001..005 | |
| INT | internal mapping/dispatch | dev.robocode.tankroyale.api.internal.* | TODO | TR-API-INT-001..002 | Not public API |

Actions for Milestone 1
- Enumerate Java public API surface (via code search in the Java module).
- Populate all rows in this table; do not leave placeholders once enumeration is done.
- Commit with message: "wasm: add initial Java↔Wasm parity checklist (Milestone 1)."