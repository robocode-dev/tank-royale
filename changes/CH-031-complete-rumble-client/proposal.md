---
id: CH-031
type: change
status: open
links: [P-003, M-008, CAP-016]
title: Complete the Rumble client locally
---

# CH-031 — Complete the Rumble client locally

## What

Complete the locally testable `robocode-dev/rumble-client` path so it can synchronize one reviewed Rumble snapshot, select and execute pinned battles through the locally built Battle Runner, retain replay-bound result evidence, durably journal completed ranked results, and prepare acknowledged issue-ops batches without repository-content write access.

## Why

P-003/M-008 is the next unfinished Rumble milestone. Configuration, ranked synchronization, selection, bot-cache preparation, and runtime packaging already exist in the external client, while battle execution, durable result handling, and submission remain unfinished. BR-049 is merged and may be consumed from a local Tank Royale build, so development no longer depends on a public Tank Royale release. The public bot population and credential-bearing live submission needed to complete M-008 are deliberately deferred.

## Route

Recommended route: full. This change adds the cross-repository team catalog contract and implements accepted CAP-016 behavior through hermetic evidence. Discovery would change the route only if the catalog already carried immutable team membership and the external client already satisfied the locally provable RCL criteria.

## Plan

Serves [P-003/M-008](../../docs/plans/P-003-rumble.md) without completing it. It supersedes incomplete implementation references to CH-012 and CH-030, promotes the existing TwinDuel team design into the catalog contract, and replaces the incorrect draft RCL-003 selection meaning with RCL-010.

## Scope

- Verify and complete RCL-001, RCL-002, and the RCL-010 configuration, synchronization, cache, and team-aware seeded-selection paths.
- Add immutable `teamMembers` metadata to the generated and synchronized catalog contracts, with focused RBC-004 evidence.
- Add a reproducible local Tank Royale dependency path so client development and CI can consume the merged Runner BR-049 API without publishing Tank Royale.
- Implement strict ranked/practice separation, full-round Battle Runner execution, complete result transcription, and replay retention for RCL-004 and RCL-005.
- Implement append-only journaling, epoch quarantine, receipt-driven retry, and bounded Issues-only submission for RCL-006 and RCL-007.
- Test execution, journaling, and submission against hermetic cross-repository fixtures without claiming live public ingestion.
- Digest verified external evidence into CAP-014 through CAP-016 while leaving RCL-008, RCL-009, and P-003/M-008 incomplete.

## Non-goals

- Publishing Tank Royale 1.2.0 or any other release solely to support client development.
- Populating the public catalog across all supported runtimes or performing a credential-bearing live result submission.
- Changing Rumble game behavior, the Runner public API, the accepted result envelope, or `rumble-data` ingestion semantics unless a blocking incompatibility is first recorded and approved.
- Granting repository-content, branch, release, package, Pages, fact, or projection write access to a client credential.
- Central services, centrally stored replay files, persistent secrets, or fork-pull-request result transport.
- Completing the separate M-009 user-documentation milestone.

## Compatibility

Local development uses the merged Runner source while ranked distribution remains gated on an immutable released Runner artifact. `teamMembers` is an additive catalog field that defaults to an empty list for individual bots; TwinDuel requires two active team entries with exactly two active members each. Ranked execution fails closed on unsupported schemas, inconsistent snapshots, source-hash or identity mismatches, incomplete battles, and behavior-version incompatibility. Practice mode has no path to ranked journal or submission state.
