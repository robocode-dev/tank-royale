---
id: CAP-014
type: capability
goal: G-001
status: draft
links: [G-001, P-003]
title: Rumble bot catalog
provenance: inferred
reversal-cost: low
---

# CAP-014 — Rumble bot catalog

Provide a community-owned, source-only catalog of eligible Tank Royale Rumble bots, so authors can submit bots through reviewable pull requests and battle contributors can retrieve precisely the reviewed source that they run.

The capability is implemented in the external `robocode-dev/rumble-bots` repository. It adopts the Tank Royale booter convention, validates submissions in portable Python, and publishes a generated versioned catalog after a bot pull request is merged. `rumble-data` checks that catalog hourly, publishing a new bot or version while skipping ranking work for identical content. Criteria remain draft until their external automated evidence is registered in this corpus.
