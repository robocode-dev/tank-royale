---
id: CH-074-open-questions
type: open-questions
status: resolved
links: [CH-074]
title: The pull request text and the branch rules
---

## What should the pull request text say?

The bot repository has a pull request template, a checklist of statements the author makes, including that they ran the repository's validator with the smoke check. The program cannot truthfully tick that. Reading the repository's moderation showed that it does not read the pull request text at all, only the author and the changed files, so the text is for people.

Resolved by the maintainer on 2026-11-01, approving the recommendation: a fixed text with the facts of the submission, what the program checked and what it did not, and the author's certification of the license. It does not list the files left out, because those names come from the author's private folder and the pull request is public. Name and version are limited to safe characters and written as code.

## What should happen when the branch name is already used?

The first recommendation was a numbered branch whenever the content differed. Reading the moderation showed that to be wrong in the common case: moderation runs again when an open pull request gets a new commit, and the repository's contributing guide asks authors to push a fix for exactly that reason, so a numbered branch would put a second pull request beside the first.

Resolved by the maintainer on 2026-11-01, approving the corrected recommendation: one open pull request per bot per account. An open pull request with the same files is returned; with other files it gets one new commit and an updated title and text. With no open pull request, a branch holding exactly the staged files is an interrupted attempt and is used; otherwise a numbered branch keeps a closed or merged pull request as history. Nothing is forced, overwritten or deleted.
