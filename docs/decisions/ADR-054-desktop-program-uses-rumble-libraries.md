---
id: ADR-054
type: decision
status: verified
links: [ADR-052, ADR-053, P-007, ARCH-027, ARCH-028, CAP-016, CAP-024]
title: The desktop program uses the Rumble libraries in its own process
author: agent
accepted-by: Flemming N. Larsen (2026-11-05, Claude Code conversation)
---

# The desktop program uses the Rumble libraries in its own process

## Context

The Rumble menu needs sign-in, registration, checking and sending a bot, following it, the catalog, downloads and, later, contributing battles. The Rumble command line already does the first group, with a JSON contract that a screen could also read. A screen can therefore either call the shared libraries inside the desktop program, or run `rumble` as a separate program and read its answers. The libraries are small (the Rumble library about 0.5 MB, the bot project library 0.07 MB, the message library 0.06 MB, against a 22 MB desktop jar); what is large is the battle worker, 14 MB with its own runner, server and booter, which the desktop program cannot link.

## Decision

The desktop program calls the shared Rumble libraries in its own process for everything except battles. The command line is a second front end over the same libraries, for people without the desktop program and for agents. The behavior lives in the libraries; the command line is a thin layer that turns it into text, JSON and exit codes.

The only thing the desktop program fetches is the battle worker, because only contributing battles need it. It takes the worker out of `rumble.jar`, which the desktop program fetches from the release of its own version at the first use that needs it, the way the command line takes the worker out of itself. Signing in, registering, checking, sending and following a bot need no fetch.

The desktop program never depends on the command line or on the worker, and the libraries never depend on the desktop program. Both rules are checked by the build.

## Consequences

The desktop program holds the sign-in token in its own memory, which a separate command line process would avoid. This is accepted: the desktop program does not run bots (the booter starts them as separate programs), the saved sign-in file is readable by anything running as the same user whichever route is used, as [the account decision](ADR-053-agent-uses-participants-account.md) states, and the battle worker, which does run bots, stays a separate program with no access to it.

Two front ends can drift. The command line's contract is tested through the real jar, and the screens call the libraries directly, so a mistake in a screen is not caught by the command line's tests; the libraries' own tests carry the behavior. Contribution sessions report progress as events from the library, with no streaming form of the command line contract needed for the desktop program.

The fetching of `rumble.jar` stays as built. If the worker alone is ever wanted, a release file of its own is a later change. The [participation design](../design/rumble/participation.md) holds the module table and the rules.
