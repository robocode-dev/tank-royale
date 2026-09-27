---
id: CH-047
type: change
status: open
links: [CAP-006]
title: Batch team messages across Tank Royale
---

# CH-047 — Batch team messages across Tank Royale

## What and why

Classic teams can send more messages in a turn than Tank Royale currently forwards. This change adds an ordered batch API that carries multiple logical payloads in one existing team-message packet and event. The trial limits are 64 packets, 128 logical payloads, 48 KiB per encoded packet, and 256 KiB for the compact UTF-8 `teamMessages` array per bot per turn. The server keeps the 1 MiB pre-parse WebSocket bound and rejects invalid intents in full.

The user selected batching after the two count-only trials failed. Java is the semantic reference, and Python, .NET, and TypeScript match its behavior. Every recipient must advertise batch protocol version 1. Batches preserve entry order, use the existing next-turn delivery, and have one destination mode per packet. Compression is omitted because batching reduces per-message processing and event fanout without adding compression cost. Uniform boundary tests, malformed-client checks, and bridge conformance pass. For this PR, the stress pass criteria are complete ordered delivery, zero protocol or content errors, and zero skipped turns after the ten-turn warm-up with the server configured at 30 TPS. The corrected 64-payload batch test and the 1,000-turn 32-payload batch test pass these criteria. Measured dispatch and observer TPS remain reported diagnostics; an exact 30.000 TPS floor is not a pass criterion because host scheduling can move the long-run average slightly below nominal even for the no-message control. The 25% CPU-capped skipped-turn results remain recorded and do not establish reliability on slower machines. See OQ-002 for the measurements and limits of this evidence.

## Scope

This change is plan-less; it implements the user-selected change plan in the task discussion and does not serve an existing campaign plan item. Align the server, schema, Java, Python, .NET, and TypeScript Bot APIs; add uniform cross-platform batch tests and focused server evidence; document count, byte budgets, failure behavior, batch compatibility, and next-turn delivery. Add bridge and book evidence in their own repositories. Preserve the bridge's frozen classic API and its read-only robot jars. No release or push to an integration branch is part of this change.
