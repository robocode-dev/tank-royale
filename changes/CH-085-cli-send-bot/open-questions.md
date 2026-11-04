---
id: CH-085-open-questions
type: open-questions
status: open
links: [CH-085]
title: Command names, and who gives the license confirmation
---

## Are `bot check`, `bot send` and `bot status` the right names?

The existing commands are single words (`login`, `register`, `status`). Sending a bot is about a thing other than the account, and `status` is already taken for the account, so the bot commands are grouped under `bot`. The alternatives are `check-bot`, `send-bot` and `bot-status`, which keep one level but read worse, or reusing `status` with a bot name, which mixes two answers.

Recommendation: the grouped names.

## May an agent give the author's license confirmation?

The Rumble's rules say the author certifies the right to publish the bot. An agent acts for its person (the account decision), but the certification is a statement of fact and of rights that only the author can give. The program cannot tell who typed the option.

Recommendation: an explicit option, never a default and never saved, written into the answer and the pull request text, with the documentation saying that an agent passes it only when its person has told it to for that bot. The alternative is a second command that only a person can run, such as one that needs a code shown on screen, which would make agents unable to send at all and would not stop an agent that read the code to its person's chat.
