---
id: ADR-056
type: decision
status: verified
links: [ADR-052, ADR-043, P-007, CAP-016, CAP-021, CAP-024]
title: Each ranked battle with a Python bot gets its own copy of the program's Python Bot API, made fresh for the session and never written back
author: agent
accepted-by: Flemming N. Larsen (2026-11-17, Claude Code conversation)
---

# ADR-056: Each ranked battle with a Python bot gets its own copy of the program's Python Bot API, made fresh for the session and never written back

## Context

The battle worker starts with an environment built from an allowlist and with its own empty home and temporary folders, so that no token, home folder or container socket is handed to the bots; the [decision to share desktop and headless Rumble logic while isolating untrusted execution](ADR-052-rumble-desktop-and-test-environments.md) asks for that. Under the [Rumble client trust boundary](ADR-043-rumble-client-trust-boundary.md), running bots directly on the contributor's computer, as the desktop program and the command line do, is the fallback to the container, with a weaker isolation boundary whose residual risk the contributor accepts: the bots run as the contributor's own user, so a bot can still open a file it knows the path of, change files the user can change, or interfere with the other bot of its battle.

The bot repository's launchers expect the one who runs them to provide what they need. A Python bot runs `python`, or the Python that `RUMBLE_PYTHON` names, with the Bot API already importable; Java, C# and TypeScript launchers fetch their Bot API themselves into a cache folder that defaults to the temporary folder. The old Rumble client provided the Python Bot API in its container image, built from the engine. The first real contribution sessions on 2026-11-16 ran Java catalog bots, which fetch their Bot API into the worker's own temporary folder at every battle, but no Python bot could start: the contributor's own Python packages live in their user folder, which the worker keeps out. Both public catalogs hold only Java and Python bots today.

A folder that bots of one battle can write to and bots of a later battle use would let one bot change the code another bot runs in another battle, and a check against a hash kept where bots can write would not stop it.

## Decision

At the start of a session that may run a Python bot, the host installs this program's own Python Bot API, the one built with the engine that judges the battles, and what it needs into a new folder of its own for that session, with the Python the launchers call: the program found on the contributor's path, at the version the Rumble supports. The host keeps the content hash of that folder in memory only. The folder is never given to a bot and is removed when the session ends; no environment is reused between sessions.

For every battle that holds a Python bot, the host checks the session's folder against the hash in memory and copies it into that battle's own run folder, which the worker gets new and empty and which is removed after the battle. The worker's environment then names the copy on Python's module path and tells Python not to write compiled files nor to read the user's own packages; `RUMBLE_PYTHON` names the same Python the launchers would find. A copy is used by one battle only, so nothing a bot writes reaches a later battle.

When Python is missing, older than the Rumble supports, or the install fails, the session leaves its Python bots, and the teams that hold them, out with a reason that says what to install, and runs the others. Java, C# and TypeScript bots keep fetching their Bot API into the worker's own temporary folder at every battle.

This decision does not make the bots of one battle safe from each other, nor the contributor's computer safe from a bot: on the computer itself they run as the contributor's user, as the trust boundary already accepts, and moderation of the bot repository remains what keeps harmful bots out.

## Rationale

The launchers already define how they are given a Python and its packages, so the host only has to keep that promise, and installing the program's own Bot API means the bots run the API built with the engine that judges them, as the old image did. A folder made fresh for each session, with its hash held only in memory, cannot carry a change from an earlier session or be matched by a hash a bot rewrote; a copy for each battle, in a folder that exists only for that battle, cannot carry a change to a later battle. Installing into a plain folder for Python's module path, rather than making a virtual environment, keeps the copy small and free of the absolute paths a virtual environment writes into its scripts. Per-bot installs from each bot's `requirements.txt` would multiply the install time for no gain, because the catalog allows the official Bot API as a Python bot's only dependency.

## Consequences

- Python bots can take part in contribution sessions on a computer with the supported Python; the session says when it cannot use them and why.
- Each session spends the time of one install at its start, which needs the network for the Bot API's own dependencies; each Python battle spends the time of one check and one copy of a small folder, measured when this is built.
- Python bots run this program's Bot API, not necessarily the version their `requirements.txt` pins, as with the old client.
- Java, C# and TypeScript bots fetch their Bot API at every battle, which costs a download per battle; a checked cache for them is a later decision if that cost matters.
- The bots learn the path of the contributor's Python through `RUMBLE_PYTHON`, as they already can through the path; no folder of the Rumble's state is named to them.

## References

- [ADR-052: Share desktop and headless Rumble logic while isolating development and untrusted execution](ADR-052-rumble-desktop-and-test-environments.md)
- [ADR-043: Rumble client trust boundary](ADR-043-rumble-client-trust-boundary.md)
- [Participation design](../design/rumble/participation.md)
- [Rumble client capability](../capabilities/CAP-016-rumble-client/README.md)
- [Rumble command line capability](../capabilities/CAP-024-rumble-command-line/README.md)
