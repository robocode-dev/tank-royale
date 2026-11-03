---
id: CH-084-tasks
type: tasks
status: open
links: [CH-084]
title: Sign in, sign out and register from the Rumble command line
---

- [ ] Settle the open question with the maintainer: the two-call sign-in flow for agents and the single blocking call for a person.
- [ ] Reserve identities and write the acceptance criteria for the three commands, exit code 3, the waiting code and the answer rules, before any code, each with its test type.
- [ ] Add a protected waiting-code file to the library with save, load, remove and expiry, with tests.
- [ ] Add `login`, `login --finish`, `logout` and `register` to the command line with injectable GitHub transports, with in-process tests using fakes.
- [ ] Give exit code 3 its meaning in the answer contract, with the reasons and next command in the JSON answer.
- [ ] Run the real jar as a real process for one sign-in-needed case with closed input.
- [ ] Add the new commands to the fixture check's command line case and its tests, and rerun the command.
- [ ] Update the participation design, the command line capability, the plan note and the indexes, regenerate the evidence manifest and validate the corpus.
