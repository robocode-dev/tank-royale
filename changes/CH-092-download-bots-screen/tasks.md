---
id: CH-092-tasks
type: tasks
status: open
links: [CH-092]
title: Add the Download bots screen to the Rumble menu
---

- [ ] Settle with the maintainer: the slice (the screen only, or also New Battle), whether the license levels come now, and how the screen knows earlier downloads.
- [ ] Reserve identities and write the acceptance criteria for the choose step, the states of the bots, the download and its result, the rule that nothing is overwritten or run, the thread and result-order rules and the texts, before any code, each with its test type.
- [ ] Check what the library already does for earlier downloads and retired bots, and add only what is missing, with tests against a fake catalog and archive.
- [ ] Build the plain wizard class with a port for the catalog and the fetch, and prove every state, the one-failure-does-not-stop-the-rest rule, the dropped late result and that nothing is run.
- [ ] Build the dialog and the menu item with every text in the resource files in every language file, every text wrapping and success and failure in colour, and extend the test that every code has a text.
- [ ] Update the participation design, the mockups' status note and the plan note, regenerate the evidence manifest and validate the corpus.
- [ ] Hand the maintainer the manual check on the test environment, with the downloaded folder removed afterwards, only when the maintainer says so.
