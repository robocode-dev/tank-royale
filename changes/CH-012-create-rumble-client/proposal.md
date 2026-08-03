---
id: CH-012
type: change
status: open
links: [P-003, M-008]
title: Create Rumble Client
---

# CH-012 — Create Rumble Client

## What

Create the community-owned `robocode-dev/rumble-client` repository: a local ranked and practice battle client built on Tank Royale's Battle Runner. In ranked mode it will follow the Rumble data repository's canonical-location and engine-pin records, obtain the synchronized bot catalog and per-game-type matchmaking advice, run the selected battle against the pinned engine, record every completed result in a local journal with replay evidence, and submit batches through the registered-client issue-ops flow. Practice mode will run local battles without creating or submitting any ranked result.

The client will make ranked execution reproducible and intentionally constrained: it will verify schema versions, source hashes, catalog commit, game type, and `behaviorVersion` before a battle; it will preserve unsent journal entries across failures; and it will never receive repository-content, branch, projection, or Git-history write access. A rebuildable container will be the primary distribution and sandbox boundary, with documented bare-metal operation as a fallback.

## Why

M-006 supplies the reviewed bot sources and M-007 supplies the engine pin, catalog mirror, registered-client contract, result inbox, and match advice. The missing component is the contributor-side loop that turns those published contracts into actual, attributable ranked battles. Completing it lets a registered community member contribute a result end to end without a human copying data or granting a client repository write access.

## Scope

- Create `robocode-dev/rumble-client` under the community organization with contributor, security, and operating guidance, a reproducible build, and focused automated verification.
- Define the client configuration, local journal, replay-evidence, bot-cache, and submitted-batch contracts, and add capability ownership, acceptance criteria, and durable decisions to this repository's corpus before implementing the external client.
- Implement schema-aware synchronization of `rumble-data`'s canonical pointer, engine pin, synchronized catalog, client registration, and matchmaking projections, plus immutable local cache validation.
- Implement ranked selection for `1v1`, `twinduel`, and `melee`, preferring configured own bots while using seeded selection from published advice without treating advice as a reservation.
- Integrate the Battle Runner to execute a full pinned battle, transcribe `BattleResults` into the data repository's result-envelope format, hash and retain a local replay, append it to the journal, and reject incompatible engine or catalog state before execution.
- Implement registered-client issue-ops submission with batch retry/backoff and acknowledgement-driven journal rollover. Define the fork-pull-request transport as the portable fallback where the data-repository contract supports it.
- Provide a rebuildable, egress-constrained container carrying the pinned Tank Royale runtime and the Java, .NET, Python, and Node.js bot runtimes, with documented bare-metal fallback operation.
- Record M-008 evidence in P-003 only after a registered-client ranked battle reaches `robocode-dev/rumble-data` through issue-ops and its CI ingests it without manual intervention.

## Non-goals

- Changing the `rumble-bots` catalog protocol, the `rumble-data` ingestion or aggregation semantics, Tank Royale's engine, Battle Runner public behavior, or Rumble game presets.
- Giving any client a token able to modify source, branches, releases, packages, Pages content, raw facts, or derived projections in either Rumble repository.
- Running a central service, storing replay files centrally, introducing persistent secrets, or adding paid infrastructure.
- Publishing the audience guides scheduled for M-009 or adding GUI `TwinDuel` selection scheduled for M-010.

## Compatibility

The client consumes the versioned `bots/index.json` catalog from `robocode-dev/rumble-bots` and the canonical location, engine, registered-client, catalog, and `matches_needed-<game-type>.json` contracts published by `robocode-dev/rumble-data`. It must reject unknown schema versions and any disagreement between the running engine and the published `behaviorVersion`; ranked results are only valid for the pinned catalog and engine state. The external client contracts introduced by this change will be versioned, and the existing repositories remain authoritative for their own contracts.

## Plan

Serves [P-003/M-008](../../docs/plans/P-003-rumble.md) and implements the `create-rumble-client` step in the Rumble roadmap.
