---
id: CH-091-open-questions
type: open-questions
status: open
links: [CH-091]
title: The slice, the real trial and the way back to a sent bot
---

## Should this change build all three steps, or only the copy check and the sending?

Steps four and five are what turns the wizard into something that publishes. Step six only reads where the pull request stands. It is smaller and carries no risk of publishing, but a wizard that can send and cannot say what happened afterwards leaves the person at a link in the browser.

Recommendation: build all three together, because step six is read-only and thin over the library, and a send that cannot be followed is an unfinished screen.

## How is the real trial done?

The behavior is proved against a fake GitHub. The real trial is the only proof that the buttons, the waiting and the sending work with GitHub, and a send is public and cannot be taken back, though the pull request can be closed.

Recommendation: after the build, the maintainer tries the wizard on the test environment with a test bot, with my help, and the pull request is closed afterwards. I send nothing to GitHub without the maintainer's word for that bot.

## Should the wizard remember a sent bot?

If the person closes the dialog after sending, the way back to the pull request is the browser or the command line. Opening Submit or update bot for the same bot could instead find its open pull request through the library and show where it stands, with nothing saved on the computer, because GitHub already holds the answer.

Recommendation: find it again through GitHub when the wizard reaches the point of sending for a bot that already has an open pull request, and show where it stands there; save nothing new.
