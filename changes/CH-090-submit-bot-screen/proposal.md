---
id: CH-090
type: change
status: open
links: [P-007, M-029, M-038, ARCH-028, ADR-053, ADR-054, CAP-016, CAP-024]
title: Add the first steps of Submit or update bot to the desktop program
---

# Add the first steps of Submit or update bot to the desktop program

The Rumble menu has one screen, the GitHub account. The next screen in the reviewed mockups is Submit or update bot, a wizard of six steps: choose the project, check it, review what will be published, make sure the account has its own copy of the bot repository with the sign-in App installed, send, and follow the pull request. This change builds the first three steps, which need no account and send nothing, and leaves the last three for the next change.

## Why

The six steps are too much for one reviewable change, and the first three stand on their own: they give an author a way to check a bot from the desktop program and see exactly what would be published, which is useful before any of the sending exists. They also carry the one part of the flow that must be right before anything is sent: the notice that the source becomes public and the author's confirmations. Splitting there lets the maintainer try the first half in the real program before the half that publishes something is built.

## What changes

- **A menu item and a wizard dialog.** The Rumble menu (still behind its developer switch) gets Submit or update bot. The dialog shows steps one to three of the mockup: choose the project folder with a folder chooser, check it, and review what will be published.
- **Step 1, choose.** The person picks a bot or team folder. The program finds the bot's name, version and language from its own configuration and shows them. A folder that is no bot says so in plain words.
- **Step 2, check.** The existing offline checks of the bot project library run on the folder and every problem is listed at once, each with a text from the resources that says what is wrong, and the library's own sentence with the exact change shown beneath it as a labelled detail. There are about thirty-five problem codes, and every one gets a resource text; a test fails when a code has none. A button checks again after the person fixes the folder.
- **Step 3, review.** The folder is staged as a separate copy in the environment's state folder, never touching the original, and the screen lists what will be published: name and version, language, the license the bot declares, and every file. It shows the notice that everything in the files becomes public the moment the bot is sent, that anyone can read and copy it, and that it stays in the history even if the pull request is closed, and it asks for two separate confirmations before the person can go on: that they understand it becomes public, and that they are the author and have the right to publish the bot under the license in its files. Next stays disabled until both are ticked. These confirmations are given by the person at the screen and are never remembered, preset or ticked by the program.
- **The next step is not built yet.** Behind the switch, Next after the confirmations says in plain words that sending comes in a later version, and nothing is sent. The wizard can be closed at any step, and nothing is written anywhere but the staged copy.
- **The behavior is separate from Swing, as for the account screen.** The steps, their transitions and the words live in a plain class in the shared library, with every call to the disk away from the screen's event thread and results in order through the screen's queue; the dialog only shows the state. All texts are resources, in every language file in English until translated, and every text wraps at the window's edge.
- **A way to get a GitHub account.** The account screen, in its signed-out state, gets a button that opens GitHub's own sign-up page in the browser, with a short text for people who have no account. The program only opens the page; it never creates an account and never fills anything in, as the account decision says. This is added here because the person who reaches this wizard without an account is the one who needs it, and it is the same small screen change.
- **Documents.** The participation design and the mockups' status note record what is built, and the plan gets a progress note. New acceptance criteria are written before the code.

## Challenge

**The confirmations are the heart of this change.** Sending a bot publishes its source under the author's name and cannot be taken back, and the notice and the confirmations are what the maintainer decided every way of sending must show. They are built here, before the sending, so they can be seen and tried alone. A test shows that the person cannot continue with either box unticked, that nothing ticks them for the person, and that going back to an earlier step clears them.

**Showing a lower layer's sentences.** The bot project library speaks English. Under the rule that no text is hard-coded, each problem code gets its own resource text, and the library's sentence appears only as a labelled detail. If the library adds a code, the test that compares the codes in its sources with the resource keys fails until the screen has a text for it.

**The staged copy is not the original.** The original folder is never changed. The staged copy is rebuilt whenever the person checks again, and it is the only thing a later step may send. A test shows that checking and staging leave the original byte for byte as it was.

**The hand trial is the maintainer's.** The behavior is proved against real bot folders and the real library, but the dialog opening, the folder chooser and the way the texts wrap are tried by hand, as for the account screen.

## Not in this change

Step 4 (the copy and the App on GitHub), step 5 (sending), step 6 (following the pull request), the comparison of launchers with the starter ones, the advisory check against the catalog, and making a new bot from a starter. No sending of any kind, and no account is needed. The other Rumble menu items are later screens.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any criterion, code or document is changed.
