---
id: CH-030
type: change
status: open
links: [P-003, M-008, CAP-016]
title: Complete the Rumble client
---

# CH-030 — Complete the Rumble client

## What

Resume the paused Rumble-client implementation now that Tank Royale 1.1.0 is released. Complete `robocode-dev/rumble-client` so a registered contributor can synchronize the reviewed bot catalog and result-data snapshot, run a pinned ranked battle, retain replay-bound local evidence, submit a bounded issue-ops batch, and receive durable ingestion acknowledgement without repository-content write access.

## Why

P-003/M-008 is the first unfinished Rumble delivery milestone. The bot catalog, result-data engine and matchmaking contracts, and the official Tank Royale 1.1.0 release now exist. The previous CH-012 proposal established CAP-016 and its acceptance contract, but deliberately paused implementation until that release gate opened.

## Route

Recommended route: full. This change delivers the draft CAP-016 behavior and its automated evidence across a new public client repository and the Rumble data contracts. Discovery would change the route only if implementation proves that the complete M-008 behavior is already present and evidenced, in which case the work would reduce to a simple evidence reconciliation.

## Plan

Serves [P-003/M-008](../../docs/plans/P-003-rumble.md). It resumes the `create-rumble-client` roadmap step previously proposed as CH-012.

## Scope

- Implement configuration validation, one mutually consistent ranked snapshot, seeded selection, and strict ranked/practice separation for RCL-001 through RCL-004.
- Execute pinned full-round battles through Battle Runner, transcribe complete results, retain replay-bound evidence, and durably journal ranked results for RCL-005 and RCL-006.
- Submit acknowledged issue-ops batches with an Issues-only credential for RCL-007; V1 does not implement fork-pull-request submission.
- Supply the reproducible multi-runtime container and documented bare-metal fallback for RCL-008.
- Add focused positive and negative automated evidence, then prove RCL-009 with a fully automated registered-client submission into `rumble-data`.
- Digest verified evidence into CAP-016 and P-003 only after the M-008 exit criterion is demonstrably met.

## Non-goals

- Changing the published `rumble-bots` catalog, `rumble-data` ingestion semantics, Tank Royale engine, Battle Runner public API, or Rumble game presets.
- Granting repository-content, branch, release, package, Pages, fact, or projection write access to a client credential.
- Central services, centrally stored replay files, persistent secrets, or fork-pull-request result transport.
- Publishing the M-009 user-documentation milestone or making P-001/M-002 purpose-tagging complete beyond RCL evidence created here.

## Compatibility

The client consumes only versioned `rumble-bots` and `rumble-data` contracts. Ranked execution fails closed on unsupported schema versions, inconsistent snapshots, source-hash mismatches, unregistered identities, or a behavior-version mismatch. Practice mode remains separate from all ranked journal and submission state.
