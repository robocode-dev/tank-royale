---
id: CH-090-tasks
type: tasks
status: open
links: [CH-090]
title: Add the first steps of Submit or update bot to the desktop program
---

- [ ] Settle with the maintainer: the slice (steps one to three now), the two separate confirmations, and how the folder is chosen.
- [ ] Reserve identities and write the acceptance criteria for the steps' behavior, the problem texts, the confirmations, the untouched original and the thread rules, before any code, each with its test type.
- [ ] Add the desktop program's dependency on the bot project library, within the rules the build already checks.
- [ ] Write the wizard's behavior class with its steps and words, with tests against real bot folders (including the sample bots) and a library that blocks.
- [ ] Add the menu item and the dialog, with all texts in the resource files in every language file and every text wrapping, and the test that every problem code has a text.
- [ ] Update the participation design, the mockups' status note and the plan note, regenerate the evidence manifest, add the new test classes to the fixture check's cases and validate the corpus.
- [ ] Hand the maintainer the manual check: open the wizard, choose a bot, fix a problem, review and see that Next stays disabled until both confirmations are ticked.
