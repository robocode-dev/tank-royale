---
id: TASKS-004
type: tasks
status: open
links: [CH-050]
title: Tasks for CH-050
---

# Tasks

- [x] Add CAP-019 scoring criteria SCR-001 and SCR-002 before changing scoring implementation.
- [ ] Implement SCR-001 survival awards per newly dead bot, to every still-living bot on another team, once per death including simultaneous deaths; exclude teammates and repeated dead states.
- [ ] Implement SCR-002 last-survivor bonus for every living member of the sole remaining team, using the count of opposing bots; award it once and give no bonus to a draw.
- [ ] Add deterministic ScoreTracker tests for SCR-001 and SCR-002 and a results-view check that team totals sum member scores.
- [ ] Bump CURRENT_BEHAVIOR_VERSION from 1 to 2 under ADR-042 and update the deterministic replay fixture/version assertion.
- [ ] Update the public scoring and team-strategy articles and add a user-visible server bug fix under the existing 1.4.0 changelog entry; leave VERSION unchanged.
- [ ] Build and test the server, then run the official 1200×1200, 10-round CombatTeam comparison five times with matched local Tank Royale artifacts and capture all score components and skipped-turn rounds.
- [ ] Digest the verified scoring contract into docs, run the required full build and Cliewen verification, and prepare the PR for human acceptance.
