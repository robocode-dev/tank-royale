---
id: CAP-016
type: capability
goal: G-001
status: draft
links: [G-001, P-003]
title: Rumble client
provenance: inferred
reversal-cost: low
---

# CAP-016 — Rumble client

Let a registered community contributor run reproducible ranked Tank Royale battles locally and submit every completed result to the Rumble without receiving repository-content write access.

The capability is implemented in the external `robocode-dev/rumble-client` repository. It consumes the reviewed bot catalog and result-data contracts, wraps Battle Runner for execution, and keeps practice activity separate from ranked evidence. Criteria remain draft until their external automated evidence is tagged and registered under P-001/M-002.
