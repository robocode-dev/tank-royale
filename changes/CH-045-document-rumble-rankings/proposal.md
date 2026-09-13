---
id: CH-045
type: change
status: open
links: [G-001, CAP-014, CAP-015]
title: Document current Rumble rankings on robocode.dev
---

# CH-045 — Document current Rumble rankings on robocode.dev

## Why

Merged [`robocode-dev/rumble-data#12`](https://github.com/robocode-dev/rumble-data/pull/12) changed the public ranking contract: catalog polling is change-aware, live rankings use only current active-version matchups, publication exposes meaningful freshness, and immutable cumulative month-end snapshots are available. The dashboard already links to `/rumble/rankings`, but that canonical public explanation does not exist, and the current Tank Royale Rumble design still presents several unimplemented LiteRumble-inspired metrics as current behavior.

## What

Add a dedicated `robocode.dev/rumble/rankings` guide explaining the exact APS calculation, distinct pairing weights, behavior epochs, active bot versions, update cadence, publication freshness, and cumulative monthly snapshots. Link it from the Rumble overview, audience guides, dashboard, and VitePress sidebar. Align the internal Rumble design and the two external-repository capability carriers with the merged behavior, and record the user-visible documentation change in the changelog.

This is a plan-less change serving `G-001`, `CAP-014`, and `CAP-015`. It does not reopen completed `P-003`, and the repository states no vision.

## Documentation impact

The public Rumble rankings guide becomes the canonical reader-facing explanation. Existing quickstarts retain audience-specific instructions and link to it instead of duplicating the complete algorithm. Internal design remains the durable technical carrier for cross-repository behavior.
