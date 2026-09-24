---
id: CH-047
type: change
status: open
links: [CAP-006]
title: Batch team messages across Tank Royale
---

# CH-047 — Batch team messages across Tank Royale

## What and why

Classic teams can send more messages in a turn than Tank Royale currently forwards. At the normal 30 ms deadline, the 128-message and 64-message count-only trials both missed the delivery and skipped-turn criteria. This change adds an ordered batch API that carries multiple logical payloads in one existing team-message packet and event. The draft trial limits are 64 packets, 128 logical payloads, 48 KiB per encoded packet, and 256 KiB for the compact UTF-8 `teamMessages` array per bot per turn. The server keeps the 1 MiB pre-parse WebSocket bound and rejects invalid intents in full.

The user selected batching after the two count-only trials failed. Java is the semantic reference, and Python, .NET, and TypeScript match its behavior. Every recipient must advertise batch protocol version 1. Batches preserve entry order, use the existing next-turn delivery, and have one destination mode per packet. Compression is omitted because batching reduces per-message processing and event fanout without adding compression cost. Uniform boundary tests, malformed-client checks, and bridge conformance pass. The initial stress run passed, but repeatability testing found missed turns and batches, and the matched no-message control retained substantially more turn budget. The latest instrumented 128-entry sequence had one failure in eight fresh paired runs; a separate 64-entry batch trial passed ten fresh pairs on the user's fast local PC, with minimum remaining budget as low as 8.581 ms. The stress gate remains unresolved for slower hardware, so the draft trial values are not accepted policy. See OQ-002 for the measurements.

## Scope

This change is plan-less; it implements the user-selected change plan in the task discussion and does not serve an existing campaign plan item. Align the server, schema, Java, Python, .NET, and TypeScript Bot APIs; add uniform cross-platform batch tests and focused server evidence; document count, byte budgets, failure behavior, batch compatibility, and next-turn delivery. Add bridge and book evidence in their own repositories. Preserve the bridge's frozen classic API and its read-only robot jars. No release or push to an integration branch is part of this change.
