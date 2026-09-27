---
id: CH-049
type: change
status: open
links: [CAP-006]
title: Expose battle-wide bot names to team bots
---

# CH-049 — Expose battle-wide bot names to team bots

**Plan:** This change is plan-less; no active plan item covers classic-compatible team identity.

## What and why

Tank Royale assigns numeric IDs to bots and teammate IDs, but a bot cannot resolve those IDs to names. The Robocode API bridge needs the name for the current bot, teammates, directed team messages, and message senders to reproduce the frozen classic `robocode.*` team API.

Add an optional `teamMemberName` to BotInfo and `bot-handshake`, and an optional `botNames` mapping to `game-started-event-for-bot`. `teamMemberName` carries the full name used for team addressing; it has no 30-character display-name limit. The server falls back to the advertised display name when this field is absent or blank, appends the advertised version, assigns ` (n)` suffixes to repeated full names in the `start-game.botAddresses` order across the complete battle roster, and sends each bot only its own name and its teammates' names. Existing numeric IDs remain unchanged. Older servers may omit the `botNames` field, and older clients may omit `teamMemberName`.

Add `getBotName(botId)` to the Java Bot API and port its semantics unchanged to .NET, Python, and TypeScript. It returns the server-provided canonical name for the bot itself or a teammate and `null`/`None` when that ID is absent from the bot's name map. This gives the bridge an authoritative lookup without exposing opponent names.

Update the protocol schema, shared models, all four Bot APIs and API references, the public team messaging documentation, and the Unreleased changelog. Add uniform positive and negative API evidence for `teamMemberName` propagation and bot-name lookup, plus server coverage for long names, version formatting, ordered duplicate suffix assignment, and opponent-name filtering.

## Commitment challenge

The central assumption is that the ordered `botAddresses` list is the authoritative battle roster order for classic duplicate-name suffixes, and that bots can advertise their full team-addressing name separately from the 30-character display name. A credible alternative is to add explicit identity and order fields supplied by the bridge runner instead of deriving names from BotInfo metadata. The cheapest useful test is a server integration case that sends a team-member name longer than 30 characters, with repeated names separated by another bot, distinct versions, and randomized server IDs; the output must preserve the long name and follow the input address order while exposing names only to teammates. Stop and revise the protocol if that test truncates the name, changes the roster order, or the bridge cannot provide the exact classic name and version.

An implementation could satisfy every API test and still fail Flemming if the bridge continues sending simple/display names, or if names differ from the exact strings classic `TeamRobot` returns. The bridge integration must therefore prove the full names and version formatting against classic before the identity capability is considered complete.

## Scope

The change updates CAP-006's protocol criteria and implements the same optional identity metadata and name lookup behavior in each official Bot API. It does not alter bot IDs, team routing, message delivery, or expose opponent names to bots.
