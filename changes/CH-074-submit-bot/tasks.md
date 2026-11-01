---
id: CH-074-tasks
type: tasks
status: open
links: [CH-074]
title: Send a staged bot to the Rumble as a pull request from the signed-in account's own copy
---

- [ ] Settle the open questions with the maintainer: the pull request text and the branch rules.
- [ ] Reserve identities and write acceptance criteria for the readiness check, the commit with the executable launcher and replaced folder, the opened pull request and its text, the certification, the resume without duplicates and the typed failures, in the bot catalog capability, before any code, each with its test type.
- [ ] Add the sign-in App page name per environment to the shared environments data file and read it in the library.
- [ ] Extend the small GitHub interface to the calls needed (reading repositories and installations, creating blobs, trees, commits and branches, opening a pull request), with request-shape tests and a check that the token goes only to GitHub's API address.
- [ ] Implement the readiness check with its typed answers, with fixture tests for ready, no copy, App not installed and a repository that is not the Rumble's.
- [ ] Implement the send step: branch from the Rumble's head, one commit with modes and a replaced folder, comparison of file identifiers before the pull request is opened, and the pull request, with fixture tests for a first version, an update that removes a file and a mismatch that stops it.
- [ ] Implement the resume rules and the certification refusal, with tests for an existing branch with the same files, an open pull request, a branch with other content and a missing certification.
- [ ] Extend the no-token-in-text tests and the source scan to the new code.
- [ ] Update the participation design's module table, regenerate the evidence manifest and indexes and validate the corpus.
