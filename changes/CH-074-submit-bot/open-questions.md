---
id: CH-074-open-questions
type: open-questions
status: open
links: [CH-074]
title: The pull request text and the branch rules
---

## What should the pull request text say?

The bot repository has a pull request template, a checklist of statements the author makes, including that they ran the repository's validator with the smoke check and that they can publish the source under the declared license. The program cannot truthfully tick the smoke check, because it has not run it on the author's computer. The recommendation is a fixed text that states the facts of the submission and what the program checked, and states the author's certification of the license (which the wizard asked for), without imitating the checklist. A moderator then sees what was and was not done. The alternative is to fill the repository's checklist with the boxes the program can honestly tick and leave the rest empty, which keeps the repository's own form but looks half finished.

## What should happen when the branch name is already used?

The branch name is fixed from the bot and version so an interrupted submission can be resumed. The recommendation is to reuse the branch when it holds exactly the staged files, return the open pull request if there is one, and otherwise use a numbered branch (`submit/MyBot-1.0.3-2`), never overwriting or deleting anything. The alternative is to refuse and ask the person to remove the old branch on GitHub, which is simpler but leaves a half-done submission stuck.
