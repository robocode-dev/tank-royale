---
id: CH-064-open-questions
type: open-questions
status: open
links: [CH-064]
title: Should bot submission keep the fork and pull request path
---

## Is the fork path simple enough, or should the author submit source as an issue?

The experiment showed that an outside account can submit a bot through a fork and a pull request, but the API cannot create the fork for them and the App must be installed on the fork. That makes three one-time browser steps: sign in with a code, press Fork on the bot repository page, and install the App on the fork. The alternative is to let the author submit the bot source as an issue that a workflow in the bot repository turns into the pull request. That needs no fork and no second installation, but it changes how the bot repository handles submissions and moderation, and it limits how large a bot can be.

Recommendation: keep the fork and pull request path for this milestone, because the bot repository's moderation, validation and ownership rules already work on pull requests, and the three steps happen once per author. Revisit if the maintainer finds the steps too heavy after reading the mockups. This question blocks the mockups for the submit screen only.
