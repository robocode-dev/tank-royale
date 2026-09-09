---
id: CH-038-tasks
type: tasks
status: open
links: [CH-038]
title: Task breakdown for CH-038
---

# CH-038 — Tasks

- [x] Reserve CH-038, branch from the accepted tip of `main`, and capture the full-route proposal
- [x] Add RCL-012 for a tested, reviewable runtime refresh workflow before implementing the new behavior
- [ ] Commit and push the proposal, then open the required draft PR before implementation
- [ ] Update the Rumble image runtime source of truth, Dockerfile base/runtime packages, and Python virtual-environment setup (RCL-008)
- [ ] Make native runtime preflight commands follow the configured Python lane and update its positive and negative unit evidence (RCL-008)
- [ ] Add the release-metadata updater with fixture tests and no-op/failure-closed behavior (RCL-012)
- [ ] Add the scheduled GitHub Actions refresh workflow that opens a reviewable PR only after the image and updater checks pass (RCL-012)
- [ ] Extend Docker CI to build and execute Java, C#, Python, and TypeScript one-round sample-bot battles and assert real `BattleResults` output (RCL-008)
- [ ] Update Rumble and Tank Royale documentation and durable runtime policy/design records (RCL-008, RCL-012)
- [ ] Run the full Rumble client tests, container smoke tests, Tank Royale checks, documentation build, and `clue validate`
- [ ] Run the automatic review loop, complete the acceptance brief, and leave the draft PR ready for human merge
