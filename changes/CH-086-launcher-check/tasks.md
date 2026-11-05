---
id: CH-086-tasks
type: tasks
status: open
links: [CH-086]
title: Tell a bot's author before sending that its launchers need a moderator, and record the first real send
---

- [x] Settle with the maintainer: where the launcher comparison runs and that a difference reports but does not block.
- [x] Reserve identities and write the acceptance criteria for the launcher finding (difference found, no difference, template unreadable, answer in both forms, no block) before any code, each with its test type.
- [ ] Read the starter launchers from the Rumble's bot repository head and compare as the moderator does, with tests using a fake GitHub for every outcome.
- [ ] Show the finding in the dry run and send answers, text and JSON, as `same`, `differs` or `not-checked`, with the file, the reason and the fix, and cover the moderator's own cases (identical, Windows line endings, renamed, edited) in the tests.
- [ ] Add the fixed note to the `bot check` answer that the launchers are compared at the dry run, and update the criteria and tests that describe its exact answer.
- [ ] Update the participation design with the first real send, what it proved and what it did not, the command line capability, the plan note and the indexes, regenerate the evidence manifest and validate the corpus.
