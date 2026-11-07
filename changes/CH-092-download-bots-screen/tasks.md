---
id: CH-092-tasks
type: tasks
status: open
links: [CH-092]
title: Add the Download bots screen to the Rumble menu
---

- [x] Settle with the maintainer: the screen only, links to the official licenses and no levels, and an empty marker file per downloaded bot folder to know earlier downloads.
- [x] Reserve identities and write the acceptance criteria for the choose step, the states of the bots, the download and its result, the rule that nothing is overwritten or run, the thread and result-order rules and the texts, before any code, each with its test type.
- [ ] Add what the library lacks, found by reading it: it fetches, checks the checksum and places new bots, and leaves an existing folder alone, but it has no marker file, no state of earlier downloads, no replacing of an unchanged earlier download and no license reading. Add only that, with tests against a fake catalog and archive.
- [ ] Build the plain wizard class with a port for the catalog and the fetch, and prove every state, the one-failure-does-not-stop-the-rest rule, the dropped late result and that nothing is run.
- [ ] Build the dialog and the menu item with every text in the resource files in every language file, every text wrapping and success and failure in colour, and extend the test that every code has a text.
- [ ] Update the participation design, the mockups' status note and the plan note, regenerate the evidence manifest and validate the corpus.
- [ ] Hand the maintainer the manual check on the test environment, with the downloaded folder removed afterwards, only when the maintainer says so.
