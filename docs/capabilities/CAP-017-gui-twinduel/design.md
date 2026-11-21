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

## Counting participants and the local server

New Battle counts every chosen bot as a participant, also a team's members, the way the server counts the bots that join, so two teams of two bots fill TwinDuel's four places; `BattleParticipants` holds that count. The desktop program starts its local server with every game type New Battle offers (`GameType.serverGamesArgument()`), so the server does not refuse a TwinDuel or custom battle started from New Battle.
