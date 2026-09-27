---
id: ADR-048
type: decision
author: agent
status: inferred
links: [CAP-006]
title: Keep full team identity separate from the display name
accepted-by: []
---

# ADR-048 — Keep full team identity separate from the display name

## Context

Tank Royale limits a bot's display name to 30 characters, while classic team bots may address one another by a fully qualified robot name. Reusing or widening the display name would change lobby presentation and its existing validation contract. The server also needs to assign unique battle names consistently while preserving numeric bot IDs for transport.

## Decision

Each Bot API may send an optional, unbounded `teamMemberName` in `bot-handshake`, sourced from `BotInfo`. The server uses that value when nonblank and otherwise falls back to the display name, appends the advertised version, and adds battle-wide ` (n)` suffixes to repeated full names in the selected roster order. `game-started-event-for-bot` carries the receiving bot's name and its teammates' names; it never includes opponent names. Bot APIs expose this map through `getBotName(botId)` with `null`/`None` for ids absent from the map.

This additive protocol preserves older clients that omit `teamMemberName` and older servers that omit `botNames`. The choice extends the [WebSocket protocol capability](../capabilities/CAP-006-protocol/README.md) and the [architecture overview](../architecture/README.md).

## Consequences

Bot API implementations must preserve the full team name unchanged across platforms, and bridges that require classic names must populate `teamMemberName`. The server owns fallback, version formatting, duplicate ordering, and teammate-only visibility so all clients receive one canonical name map.
