---
id: CH-066-tasks
type: tasks
status: open
links: [CH-066]
title: Start the shared Rumble library with environments and a dependency rule check
---

- [ ] Settle the open questions with the maintainer: where the criteria live and how the environment names are kept in one place.
- [ ] Reserve identities and write draft acceptance criteria for the environment rules and the dependency rule in the Rumble client capability, before any code, each with its test type.
- [ ] Add the `lib:rumble` module to the build with no dependency on the desktop program, runner or Swing.
- [ ] Implement the environment type with exact-match repository identity, the public Beta default and the development warning flag, with tests for acceptance and for refusing lookalike and foreign names.
- [ ] Implement per-environment state roots and the queue's environment binding, with tests that one environment's state is never handed to another.
- [ ] Remove the duplicate list of environment names according to the answer to the open question, and test that the Kotlin and Python sides agree.
- [ ] Add the dependency rule check to the build and prove it fails on a deliberate violation.
- [ ] Update the participation design's module table and the criteria status, regenerate indexes and validate the corpus.
