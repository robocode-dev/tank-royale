---
id: CH-028-tasks
type: tasks
status: open
links: [CH-028]
title: Task breakdown for CH-028
---

# CH-028 — Tasks

- [x] Capture the CH-028 proposal and open questions
- [x] Spec-first pause: the maintainer reviewed the proposal and answered the open questions
- [x] Write `docs/decisions/0045-official-bot-api-language-set.md` with the closed set, the four reopening criteria, and the community Bot API tier
- [x] Register ADR-0045 in `.clue/id-ledger.yaml` and add it to `docs/decisions/README.md`
- [x] Expand `CONTRIBUTING.md` "Regarding Bot APIs" into "Contributing a Bot API" covering both tiers and the reopening criteria
- [x] Split `web/docs/api/apis.md` into official and community sections (open question 1, resolved: list community APIs, clearly separated)
- [x] State the official/community split in the README Bot API listing
- [x] Relabel [issue 198](https://github.com/robocode-dev/tank-royale/issues/198): create the `community project` label, apply it, remove `help wanted` and `huge effort` (open question 3, resolved: keep open, reframed)
- [-] Add instance rows to `docs/decisions/log.md` — not done: open question 2 resolved against it. A decision record is timeless and a hand-maintained instance list goes stale; the dated public record lives on the issue and the discussion.
- [-] Comment on [issue 198](https://github.com/robocode-dev/tank-royale/issues/198) — cannot be done before merge, because the comment cites ADR-0045 and that record does not exist until this change is accepted. Carried to the pull request as a post-merge action so it survives the workspace deletion.
- [x] Resolve open question 4 (CHANGELOG entry) against `.agents/instructions/changelog.md` — resolved: no entry
- [x] Run `clue validate` — clean; the only remaining findings are pre-existing "folder has no README.md" warnings on generated `docs/api` output
- [x] Resolve open question 5 (agentic review loop vs the session instruction against spawning subagents) — the maintainer waived the review loop for this change; disclosed in the acceptance brief
- [x] Digest: delete the change workspace
