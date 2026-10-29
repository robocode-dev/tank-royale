---
id: CH-064-open-questions
type: open-questions
status: resolved
links: [CH-064]
title: Bot submission keeps the fork and pull request path
---

## Is the fork path simple enough, or should the author submit source as an issue?

The experiment showed that an outside account can submit a bot through a fork and a pull request, but the API cannot create the fork for them and the App must be installed on the fork. That makes three one-time browser steps: sign in with a code, press Fork on the bot repository page, and install the App on the fork. The alternative was to let the author submit the bot source as an issue that a workflow in the bot repository turns into the pull request.

Resolved by the maintainer on 2026-10-29: keep the fork and pull request path. The reason is ownership: a fork is a copy in the author's own account that only they control, while an issue lives in a repository where an administrator can delete it, possibly by accident. The mockups therefore guide the author through the fork and the App installation instead of avoiding them.
