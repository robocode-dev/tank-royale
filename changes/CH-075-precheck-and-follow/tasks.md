---
id: CH-075-tasks
type: tasks
status: open
links: [CH-075]
title: Check a staged bot against the Rumble's rules before sending it, and follow its pull request afterwards
---

- [ ] Settle the open question with the maintainer: what the pre-check does with a lower version than the active one.
- [ ] Reserve identities and write acceptance criteria for the pre-check findings, its advisory nature, the progress states and the unknown-wording fallback, in the bot catalog capability, before any code, each with its test type.
- [ ] Read the owners and the bans of a Rumble's bot repository beside its catalog, with typed errors when a file is missing or invalid.
- [ ] Implement the pre-check with the validator's lookalike rule, with fixture tests for each finding, a new name, an update by the owner and the identical version.
- [ ] Implement the progress reader with fixture tests for each state, taken from the statuses a real pull request in the test environment produced.
- [ ] Add the live check over every active public bot, skipped unless asked for.
- [ ] Extend the no-token-in-text tests and the source scan to the new code.
- [ ] Update the participation design's module table and the plan note, regenerate the evidence manifest and indexes and validate the corpus.
