---
id: CH-088-tasks
type: tasks
status: open
links: [CH-088]
title: Decide how the desktop program uses the Rumble code
---

- [ ] Settle with the maintainer: in-process, process or mixed, and which file carries the worker for the desktop program.
- [ ] Reserve identities and write the acceptance criteria for the decision's documents and the desktop build's dependency check, before any code, each with its test type.
- [ ] Write the decision record and add it to the decision index.
- [ ] Update the participation design (module table, diagram, dependency rules, the fetching paragraph) and the plan note.
- [ ] Add the desktop build's check that it does not depend on the command line or the worker, with a test that proves the check fails when it should.
- [ ] Regenerate the evidence manifest, scaffold the indexes and validate the corpus.
