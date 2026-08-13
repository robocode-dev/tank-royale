---
id: CAP-017-design
type: design
status: active
links: [CAP-017]
title: Design notes for CAP-017 (gui-twinduel)
provenance: inferred
reversal-cost: low
---

# CAP-017 design

The GUI-specific `GameType` enum and game-type dropdown expose the same `twinduel` identifier as `lib:common`. `GamesSettings` derives every built-in setup from `GAME_TYPE_PRESETS`, and `LiveBattlePlayer` starts the setup selected in `ConfigSettings`; no GUI-local TwinDuel rule values are duplicated.
