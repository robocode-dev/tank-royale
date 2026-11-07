---
id: CH-091-tasks
type: tasks
status: open
links: [CH-091]
title: Add the copy check, sending and following to Submit or update bot
---

- [x] Settle with the maintainer: the slice (all three steps or two), how the real trial is done, and whether a sent bot is found again through GitHub.
- [x] Reserve identities and write the acceptance criteria for the copy check, the license confirmation that is sent, the reviewed copy that is sent, the progress states, the thread and waiting rules and the texts, before any code, each with its test type.
- [x] Extend the wizard class with the copy check and its waiting, with a port for the readiness check, and prove it against a fake that answers each readiness case.
- [x] Extend it with the sending, the launcher advice and the result texts, and prove that nothing is sent without the second confirmation and that the reviewed copy is the one sent.
- [x] Extend it with the following of the pull request and the finding of an open pull request for a bot already sent, and prove that no check runs after closing and that late results are dropped.
- [x] Extend the dialog with steps four to six, all texts in the resource files in every language file with every text wrapping and success and failure in colour, and extend the test that every code has a text and that no token appears in a text.
- [x] Update the participation design, the mockups' status note and the plan note, regenerate the evidence manifest, add the new test classes to the fixture check's cases and validate the corpus.
- [x] Hand the maintainer the manual check on the test environment with a test bot, only when the maintainer says so, and close the pull request afterwards. Done on 2026-11-07 by the maintainer with RumbleOnboardingScout 1.0.1: the bot was sent from the desktop program to the test environment and opened a pull request with its four files, the screen showed the opened pull request and where it stood, both checks on the pull request ended green, and the pull request was closed afterwards without being merged. Not observed in this run: the copy and the App being missing and then present, sending the same files again, and the moderator's step. These are recorded as not observed in the brief.
