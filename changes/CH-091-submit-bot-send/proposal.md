---
id: CH-091
type: change
status: open
links: [P-007, M-029, M-038, ARCH-028, ADR-053, ADR-054, CAP-016, CAP-024]
title: Add the copy check, sending and following to Submit or update bot in the desktop program
---

# Add the copy check, sending and following to Submit or update bot in the desktop program

The first three steps of the Submit or update bot wizard are in the desktop program: choose the folder, check it, review what will be published and confirm. This change builds the last three: make sure the account has its own copy of the bot repository with the sign-in App installed, send the bot as a pull request, and follow the pull request until the bot is in the Rumble. It is the first screen of the desktop program that publishes something.

## Why

Without these steps the wizard ends at a message that sending comes later, so a person still has to leave the program and use the command line to send a bot. The command line and the shared library already do all three things and are proved against a fake GitHub and, for one bot, against the real test environment. This change puts them behind the wizard the maintainer reviewed, so an author can go from a folder to a pull request without leaving the desktop program, which is the point of the participation plan.

## What changes

- **Step 4, your copy on GitHub.** After the confirmations, the wizard asks the library whether the signed-in account has a copy of the bot repository with the Rumble sign-in App installed on it. When it is ready, the wizard goes straight on. When the copy is missing, it opens GitHub's own page for making a copy, and when the App is missing it opens the page that installs it on that copy, each with a button and a plain sentence from the resources. GitHub does not let the program make the copy, so the person presses the buttons in the browser; the wizard checks again at a calm pace by itself and goes on when both are found. A repository of the same name that is not a copy of the Rumble's bot repository, a sign-in that GitHub no longer accepts and any other failure each have their own plain text. The checking stops when the person goes back or closes the dialog.
- **Step 5, send.** The program sends the staged copy that was reviewed, never the original folder, as one pull request, through the same library the command line uses. The author's license confirmation is passed to it from the second box the person ticked at the review, never from a default. The screen shows progress in plain steps and then the result: a new pull request, one resumed from an earlier attempt, the same files already sent, or an open pull request that got one new commit, each with its own text, and a button that opens the pull request in the browser. The launchers are compared with the starter launchers first and a difference is shown as a note that does not stop the send, as the command line does. If the sending is interrupted, nothing is sent twice: the library already resumes from what is on GitHub.
- **Step 6, follow.** The screen reads where the pull request stands and shows it in one plain state (waiting for the checks, being checked, sent back with the reason, waiting for a moderator, accepted, merged, in the catalog, closed), with a button that checks now and a calm automatic check while the screen is open. Checks, review, publication and visibility in the rankings stay separate states, as the participation design requires.
- **A way back to a sent bot, and recognising what is already in the Rumble.** Opening Submit or update bot for a bot the account has already sent finds its open pull request through GitHub, by the account and the bot's name as the library does, and shows where it stands, so closing the dialog does not lose the way back, and nothing about the sent bot is saved on the computer. The checksum of the staged copy, the same hash of the whole source tree that the Rumble's catalog records for every bot, tells the wizard when exactly this version is already in the Rumble, so nothing is offered to send, and when a version with the same name has other content, so the person is told to take a new version.
- **The behavior stays separate from Swing, as for the earlier screens.** The steps, their transitions and the words live in the plain wizard class in the shared library, which gets a port for the readiness check, the send and the reading of progress. Every call to GitHub or the disk runs away from the screen's event thread, results arrive in order, and a result that arrives after going back, closing or signing out is dropped. The dialog only shows the state. All texts are resources, in every language file in English until translated, every text wraps at the window's edge, and success and failure use the green and red the maintainer chose for the first steps.
- **Two earlier scenarios change meaning.** The review used to end in a message that sending comes later; now going on leads to the check of the person's copy. The scenario for the confirmations and the manual trial of the first steps are reworded for that, and the first trial's recorded observation stays what it was.
- **Documents.** The participation design and the mockups' status note record what is built and what is proved, and the plan gets a progress note. New acceptance criteria are written before the code.

## Challenge

**This is the first thing in the desktop program that publishes under the author's name.** The notice and both confirmations already exist and are required before the person can go on; this change adds that the license confirmation sent along is exactly the second box, that nothing is sent when it is missing, and that the program never sends the original folder or a copy other than the one reviewed. Tests show each of these with a fake GitHub, and a test shows that the staged copy that was reviewed is the one that is sent.

**Real GitHub is the maintainer's to allow.** All behavior is proved against a fake GitHub and against the library's own tests. A real send from the desktop program goes only to the test environment, only with a test bot, and only when the maintainer says so for that bot. The hand trial is a send of a test bot to the test environment, closed afterwards by the maintainer, as the earlier real trial was. Nothing in the tests or in the build ever contacts the real Rumble.

**The program holds the sign-in token in its own memory.** This was accepted in the decision that the desktop program uses the libraries in its own process. The screen never shows the token, never writes it to a log and never puts it in a text; the existing test that finds a token in any text is extended to the new texts.

**Waiting must never freeze the window or spend GitHub's limits.** The waits in steps 4 and 6 are a slow repeated check on a background thread, with the pace the library already uses, and they stop the moment the person leaves the screen. A test shows that no check runs after closing and that a late result is dropped.

**Things the proof cannot reach.** Building the branch from the Rumble's head when the person's copy is behind is still unproven against GitHub, as is any operating system other than Windows and the public environment, which has no sign-in App yet. The proposal does not claim them; the participation design keeps saying so.

## Not in this change

Making a new bot from a starter, the advisory check against the catalog beyond what the library already does, the other Rumble menu items (My bots, Contribute battles, Results and queue, Rankings, Get started, Help), fetching the command line's worker, and the public sign-in App. The Rumble menu stays behind its developer switch until it is whole.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any criterion, code or document is changed.
