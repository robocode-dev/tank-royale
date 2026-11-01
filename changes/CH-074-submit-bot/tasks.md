---
id: CH-074-tasks
type: tasks
status: open
links: [CH-074]
title: Send a staged bot to the Rumble as a pull request from the signed-in account's own copy
---

- [x] Settle the open questions with the maintainer: the pull request text and the branch rules.
- [ ] Reserve identities and write acceptance criteria for the readiness check, the commit with the executable launcher and replaced folder, the pull request and its text, the certification and safe names, the one-open-pull-request rule and the typed failures, in the bot catalog capability, before any code, each with its test type.
- [ ] Add the sign-in App page name per environment to the shared environments data file and read it in the library.
- [ ] Extend the small GitHub interface with the update call that changing a pull request's title and text needs, with a request-shape test.
- [ ] Implement the readiness check with its typed answers, with fixture tests for ready, no copy, App not installed and a repository that is not the Rumble's.
- [ ] Implement the send step: branch from the Rumble's head, one commit with modes and a replaced folder, comparison of file identifiers before anything is published, and the pull request, with fixture tests for a first version, an update that removes a file and a mismatch that stops it.
- [ ] Implement the one-open-pull-request rule, the certification refusal and the safe-name refusal, with tests for an unchanged and a changed open pull request, an interrupted attempt, a closed pull request, a missing certification and an unsafe name, and that private file names are not in the text.
- [ ] Extend the no-token-in-text tests and the source scan to the new code.
- [ ] Update the participation design's module table and the plan note, regenerate the evidence manifest and indexes and validate the corpus.
