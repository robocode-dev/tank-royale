---
id: CH-085-tasks
type: tasks
status: open
links: [CH-085]
title: Check, send and follow a bot from the Rumble command line
---

- [ ] Settle with the maintainer: the command names and the rule that the license confirmation is an explicit option an agent passes only when told to.
- [ ] Reserve identities and write the acceptance criteria for `bot check`, `bot send`, `bot status`, the license confirmation, setup that only the person can do and the answer contract, before any code, each with its test type.
- [ ] Add `bot check` with tests that use real bot folders, including the sample bots.
- [ ] Add `bot send` with `--dry-run` and `--certify-license`, staging into the state folder, the advisory check and the readiness check, with in-process tests using a fake GitHub.
- [ ] Add `bot status`, with tests for every progress state.
- [ ] Run the real jar as a real process for `bot check` on a sample bot and for a `bot send` that needs a sign-in.
- [ ] Add the new commands to the fixture check's command line case and its tests, and rerun the command.
- [ ] Update the participation design, the command line capability, the plan note and the indexes, regenerate the evidence manifest and validate the corpus.
