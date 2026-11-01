---
id: CH-072-tasks
type: tasks
status: open
links: [CH-072]
title: Register the signed-in account with the Rumble from the shared library
---

- [ ] Settle the open questions with the maintainer: where the client ID is kept, how long to wait for the workflow, and how the result repository's file is read.
- [ ] Reserve identities and write acceptance criteria for finding an existing registration, opening one registration issue, waiting for the file, the refusals and the timeout, the sign-in-again answer, and the saved registration record, in the Rumble client capability, before any code, each with its test type.
- [ ] Implement the small GitHub interface and its real implementation on Java's HTTP client, with request-shape tests and a check that the token goes only to GitHub's address.
- [ ] Implement the registration step with its typed answers, with fixture tests for every answer and for reusing an earlier issue.
- [ ] Implement the per-environment registration record, with tests for another account, another environment, damage and removal on sign-out.
- [ ] Extend the no-token-in-text test and the source scan to the new code.
- [ ] Add an opt-in live check against the test result repository that only reads, and run it once.
- [ ] Update the participation design's module table, regenerate the evidence manifest and indexes and validate the corpus.
