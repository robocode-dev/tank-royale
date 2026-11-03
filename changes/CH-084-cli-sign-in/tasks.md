---
id: CH-084-tasks
type: tasks
status: open
links: [CH-084]
title: Sign in, sign out and register from the Rumble command line, and make it pleasant to use
---

- [x] Settle with the maintainer: an agent may start the sign-in and register the approved account, and the sign-in behaves the same with and without JSON.
- [x] Settle with the maintainer: how the program reaches people (the desktop program fetches it from the matching release at first use) and the default environment (public).
- [ ] Reserve identities, retire the three criteria that become false and write the new criteria, before any code, each with its test type.
- [ ] Revise the decision on an agent's account and mark it verified by the maintainer.
- [ ] Add to the library, with tests: a protected waiting-code store holding the pace and next check, a check bounded by a deadline, restoring a waiting code, the saved account name, a removal that reports failure and creates nothing, a renewal that says whether the new sign-in was kept, and registration that reads an earlier refusal and can retry.
- [ ] Change the answers of the command line: outcome, environment and next step in every answer, exit code 4, bad input in the form asked for, values cleaned with the account name kept, and answers as text naming the environment.
- [ ] Add `login`, `login --finish`, `login --wait`, `logout` and `register`, and update `status`, with injectable GitHub transports, with in-process tests using a fake GitHub and a fake clock.
- [ ] Add `--version`, `-e`, `RUMBLE_ENVIRONMENT` and the better help, and remove the Java warning by trying the core of the command line library, or with a manifest entry if that does not hold.
- [ ] Run the real jar as a real process for closed input, an empty standard error and the exit code of a case needing a sign-in.
- [ ] Add the new commands to the fixture check's command line case and its tests, and rerun the command.
- [ ] Update the participation design, the command line capability, the plan note and the indexes, including the direction that the desktop program fetches the program at first use and that it is not published separately, regenerate the evidence manifest and validate the corpus.
