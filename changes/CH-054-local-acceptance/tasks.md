---
id: TASKS-008
type: tasks
status: open
links: [CH-054]
title: Task breakdown for CH-054
---

# CH-054 — Tasks

- [x] Capture the proposal for local acceptance
- [ ] Pause for maintainer approval of this proposal before implementation
- [ ] Record the policy switch and the rejected alternative as a decision
- [ ] Rewrite the review-boundary constraint for the local acceptance boundary and the push authorization after acceptance
- [ ] Update AGENTS.md and the Copilot instructions to describe the new flow
- [ ] Review CONTRIBUTING.md and the pull request template for wording that presumes the maintainer's own flow
- [ ] Delete `.clue/acceptance.yaml` so the effective policy is local on `main`
- [ ] Confirm that the existing validation workflow still judges every PR and every push to `main`
