---
id: CH-086-open-questions
type: open-questions
status: resolved
links: [CH-086]
title: Where the launcher comparison runs, and whether a difference blocks
---

## Should `bot check` compare the launchers too?

`bot check` is offline, writes nothing and is the command an agent runs often while writing. Comparing launchers needs the starter launchers, which are in the Rumble's bot repository. Reading them needs the network. Keeping a copy in the program avoids that, but the copy can go out of date and then give wrong advice.

Settled by the maintainer: yes to the recommendation. Compare in the dry run and the send, which already use the network, and keep `bot check` offline. An author who wants the answer early runs the dry run, which changes nothing. The alternative is an option on `bot check` that reads the template, which keeps the default offline.

## Should a launcher difference stop the send?

A difference means a moderator must look at the bot; it does not make the bot wrong. A person may want a custom launcher.

Settled by the maintainer: yes to the recommendation. Report it, do not block. The answer says what it means and how to avoid it, and the person or the agent decides. Blocking would make it impossible to send a bot that a moderator would gladly accept.
