---
id: CH-087-tasks
type: tasks
status: open
links: [CH-087]
title: Let the desktop program fetch the Rumble command line from its own release
---

- [x] Settle with the maintainer: the file name and place, when the downloaded file is asked for its version, and what happens with releases that lack the file.
- [x] Reserve identities and write the acceptance criteria for the release carrying the file, finding the program, planning a fetch, fetching with every check, every failure answer and the developer setting, before any code, each with its test type.
- [ ] Add the command line jar to the release assets, its smoke test and the release instructions.
- [ ] Add the library part that locates the program, plans a fetch and fetches it, with tests against a fake GitHub and a fake download for every outcome, and one test that uses the real built jar as the downloaded file.
- [ ] Add the developer setting and its tests.
- [ ] Update the participation design, the command line capability, the plan note and the indexes, regenerate the evidence manifest, add the new test classes to the fixture check's cases and validate the corpus.
