---
id: CH-071-tasks
type: tasks
status: open
links: [CH-071]
title: Save the GitHub sign-in between runs in one Java-only file
---

- [ ] Settle the open questions with the maintainer: one account per environment, and the lock between programs.
- [ ] Retire the criterion that said the library writes no token to disk and mint its replacements, then write acceptance criteria for saving, loading, the owner-only check, the refusals, forgetting, switching saving off and renewal under a lock, before any code, each with its test type.
- [ ] Move the tests that carried the retired criterion to the new identities and keep the no-leak and source-scan tests working.
- [ ] Implement the file protection (owner-only creation with read-back, key derivation, encryption, atomic write) with tests that mirror the experiment's checks.
- [ ] Implement save, load and forget with the single ending for every problem, with tests for damaged, other user, other home folder, other environment, truncated and empty files, and for rights that cannot be restricted.
- [ ] Implement renewal that saves the new refresh token before use and holds a lock on the file, and test it with two simulated programs sharing one file.
- [ ] Implement the switch that turns saving off.
- [ ] Replace the design text about the Windows credential store, update the module table, regenerate the evidence manifest and indexes and validate the corpus.
