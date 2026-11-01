---
id: CH-070-tasks
type: tasks
status: open
links: [CH-070]
title: Add GitHub sign-in with a code, and token renewal, to the shared Rumble library
---

- [x] Settle the open questions with the maintainer: the public sign-in App, and how a long wait for approval should be run.
- [x] Reserve identities and write acceptance criteria for the sign-in steps, the renewal, the refusals and the no-leak rule in the Rumble client capability, before any code, each with its test type.
- [x] Add the sign-in App identifier per environment to the shared environments data file and read it in the library, with the public Beta empty and refused.
- [x] Implement the typed sign-in (ask for a code, check at GitHub's interval, signed in, waiting, slow down, expired, denied, failed) behind a small network interface, with fixture tests for each answer.
- [x] Implement renewal with the refresh token and no secret, with fixture tests for success and for a revoked or expired refresh token.
- [x] Implement the token holder that never shows the token, and tests that a recognisable token appears in no text, message or log path.
- [x] Implement the real network client on Java's HTTP client, with address and request-shape tests and an opt-in live check that needs a human with a browser.
- [x] Update the participation design's module table, regenerate the evidence manifest and indexes and validate the corpus.
