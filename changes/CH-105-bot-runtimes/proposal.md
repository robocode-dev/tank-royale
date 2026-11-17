---
id: CH-105
type: change
status: open
links: [P-007, CAP-016, CAP-024, ADR-056, ADR-052, ADR-043]
title: Run ranked Python bots in contribution sessions, each battle with its own copy of the program's Python Bot API made fresh for the session
---

# Run ranked Python bots in contribution sessions, each battle with its own copy of the program's Python Bot API made fresh for the session

A contribution session can now run Java catalog bots end to end, but no Python bot starts: the worker keeps the contributor's own Python packages out on purpose, and nothing gives the bots a Python Bot API. Both public catalogs hold Java and Python bots. The [decision that each ranked battle with a Python bot gets its own copy of the program's Python Bot API, made fresh for the session](../../docs/decisions/ADR-056-ranked-python-battles-get-their-own-bot-api-copy.md) has the host install the program's own Bot API into a folder of its own at the start of a session, keep its hash in memory, and give every Python battle its own checked copy in that battle's run folder.

This slice works in the shared library and the command line; the desktop screen for sessions is a later slice.

## Why

A Rumble that ranks only the Java half of its catalog is not the Rumble. The launchers already say how they want their Python, so keeping that promise from the host is what is missing, and doing it with nothing that one battle's bots can write and a later battle's bots use is what keeps a change by one bot out of other battles.

## What changes

- **The decision is recorded** in the decision record above, and the client capability's design, the command line capability and the participation design say what it changes.
- **The command line carries its own Python Bot API**, the wheel built with the engine, the way the desktop program already does for the Download bots screen.
- **At the start of a session that may run a Python bot**, the session checks the Python on the path against the version the Rumble supports and installs the program's own Bot API, with what it needs, into a new folder for that session, keeping its content hash in memory. It reports that it is setting up Python and how that ended. Python that is missing, too old or cannot install leaves the Python bots and the teams holding them out with a reason and the version needed. The folder is removed when the session ends.
- **Every battle with a Python bot** checks the session's folder against the hash in memory, copies it into the battle's own run folder, and gives the worker the copy on Python's module path, with Python told not to write compiled files nor read the user's own packages, and `RUMBLE_PYTHON` naming the contributor's Python. A battle without a Python bot gets none of it.
- **Java, C# and TypeScript bots are unchanged.**

## Challenge

- **Assumption most likely to be wrong:** that the catalog's Python bots run with the program's own Bot API (1.4.0) although their `requirements.txt` pins 1.3.1, and that the Bot API runs from a plain folder on the module path. The old client did the first; if a real Python catalog bot fails to start or play this way, the slice stops there.
- **Credible alternative:** one shared environment kept between sessions and checked against a stored hash. Rejected after review: a bot can rewrite a stored hash as well as the environment, and a shared folder carries one battle's change into the next.
- **Cheapest test:** the install, the check and the copy with a fake tool runner and real folders; a battle's worker environment with and without a Python bot; that a change to a battle's copy never reaches the next battle; then one real session in the test Rumble with a Python catalog bot against a Java one, kept, sent and accepted.
- **Result that revises the work:** a real Python battle that does not complete this way, or a change made in one battle's copy that reaches another battle, stops the slice.
- **How it could pass every criterion and still fail a contributor:** the first battle waits for an install with no word about it. The session therefore reports setting up Python and how it ended, and the time of the install and of a copy is measured and written down.

## Out of scope

Making the bots of one battle safe from each other or the contributor's computer safe from a bot, which the trust boundary leaves to the container and to moderation; caches for C#, TypeScript and Java; per-bot Python installs; the desktop session screen; installing a Python runtime for the contributor.

## Acceptance criteria

These will be written as criteria of the Rumble client capability and the command line's capability, each with its identity, when the work starts. Test class Unit unless stated.

- **The session's Python Bot API is installed fresh with the supported Python and the program's own Bot API, its hash kept in memory**, and an unusable Python leaves the Python bots out with a reason.
- **A battle with a Python bot gets its own checked copy through the worker's environment, and one without gets nothing of it.**
- **A change to one battle's copy, or to the session's folder, never reaches another battle.**
- **The command line carries its own Python Bot API and reports setting up Python.**
- **One real session in the test Rumble runs a Python catalog bot and its result is accepted.** (Integration, needs the saved sign-in and the supported Python; skipped without them.)

## Identity allocation

The change and the decision were allocated with the repository's coordinated allocation and are made live when the proposal is committed. Criterion identities are allocated when the criteria are written.

## Human evidence

One real `rumble contribute` session in the test Rumble with a Python bot, run by the maintainer, if the integration test cannot run on the maintainer's computer.
