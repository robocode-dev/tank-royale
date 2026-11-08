---
id: CH-092-tasks
type: tasks
status: open
links: [CH-092]
title: Add the Download bots screen to the Rumble menu
---

- [x] Settle with the maintainer: the screen only, links to the official licenses and no levels, and an empty marker file per downloaded bot folder to know earlier downloads.
- [x] Reserve identities and write the acceptance criteria for the choose step, the states of the bots, the download and its result, the rule that nothing is overwritten or run, the thread and result-order rules and the texts, before any code, each with its test type.
- [x] Add what the library lacks, found by reading it: it fetches, checks the checksum and places new bots, and leaves an existing folder alone, but it has no marker file, no state of earlier downloads, no replacing of an unchanged earlier download, no reading of a bot's license from its file on the disk and no table of the allowed licenses with their links. Add only that, with tests against a fake catalog and archive.
- [x] Build the plain wizard class with a port for the catalog and the fetch, and prove every state, the one-failure-does-not-stop-the-rest rule, the dropped late result and that nothing is run.
- [x] Build the dialog and the menu item with every text in the resource files in every language file, every text wrapping and success and failure in colour, and extend the test that every code has a text.
- [x] Write the short explanation of each license for the maintainer to read, and correct what the use case, the mockups and the participation design say about seeing the license before the download. Update the participation design, the mockups' status note and the plan note, regenerate the evidence manifest and validate the corpus.
- [ ] Hand the maintainer the manual check on the test environment, with the downloaded folder removed afterwards, only when the maintainer says so.
