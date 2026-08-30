---
id: CH-032
type: change
status: open
links: [P-003, M-008, CAP-014, CAP-015, CAP-016]
title: Prove live Rumble result ingestion
---

# CH-032 — Prove live Rumble result ingestion

## What

Populate the public Rumble catalog with a second reviewed 1v1 bot, register the `flemming-rumble-01` contributor identity, and prove one real ranked result reaches `rumble-data` through the client's Issues-only transport.

## Why

P-003/M-008 remains open solely for the live proof that CH-031 deliberately deferred. The official Tank Royale 1.1.0 release satisfies the release gate, while the locally built Runner continues to provide BR-049 for this proof. No Tank Royale release is needed.

## Route

Recommended route: full. P-003 requires every milestone to use its own proposal, and this change binds public cross-repository evidence before marking M-008 complete. Discovery would change the route only if a prior accepted result already proves the exact M-008 exit criterion for the registered identity and public catalog.

## Plan

Serves [P-003/M-008](../../docs/plans/P-003-rumble.md) without changing the accepted RBC, RDA, or RCL criterion meanings.

## Scope

- Add one source-only Python bot named `Vector 1.0.0` to `robocode-dev/rumble-bots`, reviewed and merged through its normal pull-request flow.
- Register `flemming-n-larsen` and the stable public client ID `flemming-rumble-01` in `robocode-dev/rumble-data`, reviewed and merged through its normal pull-request flow.
- Synchronize the reviewed catalog, run one real ranked 1v1 battle through the local Runner build, and submit it with a repository-scoped Issues read/write token.
- Verify the closed result issue, durable receipt, immutable raw fact, regenerated projections, and local replay and journal acknowledgement before digesting the evidence.

## Non-goals

- Publishing Tank Royale, Battle Runner, or a Rumble client distribution.
- Populating TwinDuel or melee, changing the engine pin, or changing any Rumble contract.
- Giving the client repository-content, branch, release, package, Pages, fact, or projection write access.
- Persisting a credential, replay, journal, local configuration, or generated cache in any repository.
