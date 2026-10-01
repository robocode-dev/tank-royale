---
id: TASKS-004
type: tasks
status: open
links: [CH-050]
title: Tasks for CH-050
---

# Tasks

- [x] Add CAP-019 scoring criteria SCR-001 and SCR-002 before changing scoring implementation.
- [x] Implement SCR-001 survival awards per newly dead bot, to every still-living bot on another team, once per death including simultaneous deaths; exclude teammates and repeated dead states.
- [x] Implement SCR-002 last-survivor bonus for every living member of the sole remaining team, using the count of opposing bots; award it once and give no bonus to a draw.
- [x] Add deterministic ScoreTracker tests for SCR-001 and SCR-002 and a results-view check that team totals sum member scores.
- [x] Bump CURRENT_BEHAVIOR_VERSION from 1 to 2 under ADR-042 and update the deterministic replay fixture/version assertion.
- [x] Update the public scoring and team-strategy articles and add a user-visible server bug fix under the existing 1.4.0 changelog entry; leave VERSION unchanged.
- [x] Add positive and negative SCR-001 and SCR-002 scoring tests as JUnit methods with stable identities recognized by the Clue 0.26.0 CI validator, and export their executable references for Clue 0.27 validation.
- [x] Run the server test suite and full clean build.
- [x] Run the official 1200×1200, 10-round CombatTeam comparison five times with matched local Tank Royale artifacts and capture all score components and skipped-turn rounds; two component-capture runs were error-free and three recorded bridge-side CombatTeam exceptions (see `combatteam-retest.json`).
- [ ] Run the required full build and Cliewen verification, then prepare the PR for human acceptance.
