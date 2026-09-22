---
id: CH-047
type: change
status: open
links: []
title: Trial bounded team-message throughput across Tank Royale
---

# CH-047 — Trial bounded team-message throughput

## What and why

Classic teams can send more messages in a turn than Tank Royale currently forwards. The Bot APIs, server, and schema disagree about both count and size, so a client can accept a message that the server silently drops. Trial one policy for every Tank Royale bot: at most 128 messages and 256 KiB of compact, UTF-8-encoded `teamMessages` array data per bot per turn, and at most 48 KiB for each encoded message. Bound inbound WebSocket text to 1 MiB before JSON parsing. Reject an invalid intent in full rather than delivering a prefix.

The user supplied this trial plan. It is plan-less in the Tank Royale corpus; CAP-018 will own the accepted messaging criteria. The trial uses local builds only. The policy becomes publishable only if boundary, malformed-client, CombatTeam, and five-bot 30 TPS stress evidence pass.

## Scope

Align the server, schema, Java, Python, .NET, and TypeScript Bot APIs; add focused cross-platform and server evidence; document count, byte budgets, failure behavior, and next-turn delivery. After the trial gate passes, add bridge and book evidence in their own repositories. Preserve the bridge's frozen classic API and its read-only robot jars. No release or push to an integration branch is part of this change.
