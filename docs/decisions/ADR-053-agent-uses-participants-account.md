---
id: ADR-053
type: decision
status: inferred
links: [ADR-052, P-007, ARCH-027, CAP-024]
title: An AI agent works with the participant's own GitHub account
author: agent
accepted-by: []
---

# An AI agent works with the participant's own GitHub account

## Context

The Rumble command line is meant to work for AI agents as well as for people. The commands that sign in, register and send bots raise whose identity an agent acts under. An agent that runs as the participant's operating system user can read the participant's saved sign-in whichever account it uses, so a separate account would not protect the saved sign-in by itself. The registration, the ownership of a bot, the one open pull request per bot and the rankings all assume that an account belongs to a person.

## Decision

An agent acts for one participant, with that participant's own GitHub account and saved sign-in. Registrations, pull requests and results carry the participant's name. An agent with a GitHub account of its own is not supported; it can be added by a later decision that also gives rules for registration, ownership of a bot and the rankings, and names a person who answers for it.

The sign-in commands follow these rules. The person starts and approves the sign-in at GitHub with the device code; an agent never approves it in a browser and never creates or registers an account. A command that needs a sign-in and finds none, or one that cannot be used, ends with exit code 3, says so in its JSON answer and asks nothing. No answer shows a token. A command works on one environment at a time and never mixes the public and the test environment.

## Consequences

The saved sign-in is treated as the participant's and shown to nobody. It is not protected from an agent that the participant runs it under, as it is not protected from a bot running as the same user; the battle worker is what keeps bots away from it. The [participation design](../design/rumble/participation.md) holds the contract and the [architecture overview](../architecture/README.md) the boundaries it keeps.
