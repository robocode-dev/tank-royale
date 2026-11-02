---
id: CH-077-open-questions
type: open-questions
status: open
links: [CH-077]
title: Where the messages live and which capability holds the criteria
---

## Where should the host and worker messages live?

The participation design says the messages live in the common library. That library is published to Bot API users, has no serialization dependency, and is used by the server, the booter and the clients. The host library must read the messages but must not depend on the runner, and the worker must read them without the host library, so the messages need a module both can use.

Recommendation: a new small unpublished module for the messages, with the serialization dependency, and an update of the design to say so. The alternative is to add serialization to the common library, which changes a published module for an unpublished feature.

## Which capability should hold the criteria?

The criteria so far live in the bot catalog capability, which fits submission and download but not running battles. The client-battles design describes a client that runs ranked battles and uploads results, and no capability covers it yet.

Recommendation: write these criteria in a new capability for running contributed battles, with its own prefix, so later slices (the host, the controller, the fixture command) add to one place. The alternative is to keep adding to the bot catalog capability, which grows it past its subject.
