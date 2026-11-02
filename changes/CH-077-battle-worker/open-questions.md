---
id: CH-077-open-questions
type: open-questions
status: resolved
links: [CH-077]
title: Where the messages live and which capability holds the criteria
---

## Where should the host and worker messages live?

The participation design said the messages live in the common library. That library is published to Bot API users, has no serialization dependency, and is used by the server, the booter and the clients. The host library must read the messages but must not depend on the runner, and the worker must read them without the host library, so the messages need a module both can use.

Resolved by the maintainer on 2026-11-02: a new module for Rumble, built the way Tank Royale normally builds its modules. It is `lib:rumble-protocol`, unpublished, shaped like the other Rumble modules, and the design is corrected to say so.

## Which capability should hold the criteria?

The criteria so far lived in the bot catalog capability, which fits submission and download but not running battles.

Resolved by the maintainer on 2026-11-02: a new file for battles, so things are not mixed. The criteria go in a new capability for running contributed battles, and later slices add to it.
