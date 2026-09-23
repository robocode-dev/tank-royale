---
id: TASKS-002
type: tasks
status: open
links: [CH-047]
title: CH-047 implementation tasks
---

# Tasks

- [x] Record the user-selected batch trial, Java reference semantics, compatibility handshake, and no-compression first implementation (TML-001 through TML-004).
- [x] Align schema and server batch validation, including atomic rejection, recipient capability checks, and the pre-parse WebSocket text bound (PRO-006, PRO-007, PRO-008).
- [x] Align Java, Python, .NET, and TypeScript batch APIs and enqueue validation, with shared `TR-API-TCK-022` cases and matching public documentation (PRO-006, PRO-007).
- [x] Run uniform boundary, malformed-client, directed/broadcast, order, and next-turn tests against matched local builds (PRO-006, PRO-007, PRO-008).
- [x] Run CombatTeam and the five-bot 30 TPS stress workload; record processing time, skipped turns, delivery, and encoded payload traffic, then apply the acceptance gate (PRO-006).
- [x] Address branch review: stable same-turn team-message order in the .NET queue, stale team messages dropped on add in all four queues (`TR-API-EVT-010`, `TR-API-EVT-011`), receivers checked against the game-start roster so a disconnected teammate no longer gets the sender closed, and Python batches accept one-shot iterables (PRO-006, PRO-007, PRO-009).
- [x] After the gate passed, finish the Tank Royale article, bridge collection conformance, Book pages, CHANGELOG, and version roll, then run relevant checks (PRO-006, PRO-007).
