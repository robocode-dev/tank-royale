---
id: CAP-015
type: capability
goal: G-001
status: draft
links: [G-001, P-003]
title: Rumble result data
provenance: inferred
reversal-cost: low
---

# CAP-015 — Rumble result data

Provide an auditable, machine-written record of ranked Tank Royale Rumble battles, so contributors can submit results without write access and everyone can reproduce the leaderboard and match advice from immutable facts.

The capability is implemented in the external `robocode-dev/rumble-data` repository. Its portable Python scripts validate issue-ops batches, append accepted facts, derive cumulative active-version projections, publish meaningful ranking freshness, and preserve immutable month-end dashboard snapshots. Criteria remain draft until their external automated evidence is registered in this corpus.
