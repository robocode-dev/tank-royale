---
id: P-003
type: plan
status: active
links: [G-001]
title: Tank Royale Rumble
provenance: inferred
reversal-cost: low
---

# P-003 — Tank Royale Rumble

Build the Tank Royale Rumble: an automated, serverless, community-driven tournament system in the RoboRumble/LiteRumble tradition, as designed in [docs/design/rumble/](../design/rumble/README.md). Bot authors submit source code via pull requests, community members run battles on their own machines, and CI aggregates results into rankings published on a static dashboard — all inside Git repositories, at zero infrastructure cost. Serves G-001 directly: the rumble is the competition half of "learning and competition".

The milestones mirror the design's Change Proposal Roadmap in order. Each milestone is delivered by its own change proposal that stops for maintainer approval before implementation, as the design mandates. Milestones M-006 through M-008 create artifacts outside this repository (the `rumble-bots` and `rumble-data` repositories and the client); their proposals and decision records still live here, in the system of record.

| ID | Milestone | Exit criterion | Status | Evidence |
|---|---|---|---|---|
| M-005 | Tank Royale prepared for rumble | `behaviorVersion` exists server-side and in the schema; SPDX `license` field in the booter bot config; Battle Runner exposes battle results for the client; rumble game presets defined; deterministic replay-regression hook in place | done | CH-009; server/runner/booter contracts and focused acceptance evidence |
| M-006 | `rumble-bots` repository live | Repository scaffolded under the community organization with validation CI, templates, and governance; a sample bot PR passes validation end to end | done | CH-010; `robocode-dev/rumble-bots#1` merged as `c735e6ff4`; 4 external focused tests and the validator smoke check pass |
| M-007 | `rumble-data` repository live | Result inbox drained by CI into immutable raw facts; aggregation produces leaderboard, pairings, and matches-needed projections; dashboard published on Pages | done | CH-011; [robocode-dev/rumble-data#4](https://github.com/robocode-dev/rumble-data/pull/4) merged on 2026-08-03, providing catalog synchronization and ranked advice for every V1 game type |
| M-008 | Rumble client runs ranked battles | After the official Tank Royale 1.1.0 release, the client pulls the bot catalog and matchmaking advice, runs a ranked battle, and its submitted result lands in `rumble-data` via issue-ops with no human in the loop | todo | |
| M-010 | GUI supports TwinDuel | The GUI game-type dialog can select and start the `TwinDuel` preset using the common game-type contract; it is shipped in Tank Royale 1.1.0 | done | CH-013; CAP-017/GTD-001 and GUI acceptance evidence |
| M-009 | Rumble documentation published | User guides live under `/web/docs/rumble/` with one quickstart per audience: bot author, battle contributor, moderator | todo | |

M-006 and M-007 both depend only on M-005 and may proceed in parallel; M-008 depends on the `rumble-bots` catalog, the `rumble-data` engine/matchmaking files, and the official Tank Royale 1.1.0 release; M-010 depends only on M-005's common game-type contract and is shipped in Tank Royale 1.1.0; M-009 follows the settled client and GUI interfaces.
