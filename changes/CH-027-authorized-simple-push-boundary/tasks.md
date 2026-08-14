---
id: CH-027-tasks
type: tasks
status: open
links: [CH-027]
title: Task breakdown for CH-027
---

# CH-027 — Tasks

- [x] Repair the `CH` counter in `.clue/id-ledger.yaml` and reserve CH-027, after `clue id next CH` offered CH-002, an identity the corpus already binds to the TypeScript npm publishing change
- [x] Capture the CH-027 proposal and open the draft pull request
- [x] Spec-first pause: report the proposal and wait for the maintainer's go-ahead before implementation (repository review gate)
- [x] Amend `docs/constraints/C-002-review-boundary.md`: title, body, promotion trigger, residual
- [x] Update the constraint's index row in `docs/constraints/README.md`
- [x] Update the live carriers of the old contract
  - [x] `AGENTS.md`
  - [x] `DEVELOPMENT.md`
  - [x] `.agents/instructions/core-principles.md`
  - [x] `.github/pull_request_template.md`
- [x] Record the amendment as a dated row in `docs/decisions/log.md`
- [x] Confirm that no changelog entry is warranted (no bot-developer-visible effect) and that no product code, test, or build surface changed
- [ ] Run `clue validate` and the `clue-verify` agentic review loop, then digest and delete this workspace
