---
id: CH-031
type: change
status: open
links: [P-003, M-008, CAP-016]
title: Complete the Rumble client locally
---

# CH-031 — Complete the Rumble client locally

## What

Complete `robocode-dev/rumble-client` so a registered contributor can synchronize one reviewed Rumble snapshot, select and execute pinned battles through the locally built Battle Runner, retain replay-bound result evidence, durably journal completed ranked results, and submit acknowledged issue-ops batches without repository-content write access.

## Why

P-003/M-008 is the next unfinished Rumble milestone. Configuration, ranked synchronization, selection, bot-cache preparation, and runtime packaging already exist in the external client, while battle execution, durable result handling, submission, and end-to-end evidence remain unfinished. BR-049 is merged and may be consumed from a local Tank Royale build, so development no longer depends on a public Tank Royale release.

## Route

Recommended route: full. This change completes accepted CAP-016 behavior and binds cross-repository acceptance evidence for M-008. Discovery would change the route only if the external client already satisfies and evidences all RCL criteria, reducing the work to evidence reconciliation.

## Plan

Serves [P-003/M-008](../../docs/plans/P-003-rumble.md) and supersedes the incomplete implementation references to CH-012 and CH-030 without changing the accepted RCL criterion meanings.

## Scope

- Verify and complete the existing RCL-001 through RCL-003 configuration, synchronization, cache, and seeded-selection paths.
- Add a reproducible local Tank Royale dependency path so client development and CI can consume the merged Runner BR-049 API without publishing Tank Royale.
- Implement strict ranked/practice separation, full-round Battle Runner execution, complete result transcription, and replay retention for RCL-004 and RCL-005.
- Implement append-only journaling, epoch quarantine, receipt-driven retry, and bounded Issues-only submission for RCL-006 and RCL-007.
- Complete the multi-runtime execution and network-boundary evidence for RCL-008.
- Prove RCL-009 through an automated clean-install flow spanning the client and result-data ingestion contracts.
- Digest verified external evidence into CAP-016 and P-003 only after the M-008 exit criterion is demonstrably met.

## Non-goals

- Publishing Tank Royale 1.2.0 or any other release solely to support client development.
- Changing Rumble game behavior, the Runner public API, the accepted result envelope, or `rumble-data` ingestion semantics unless a blocking incompatibility is first recorded and approved.
- Granting repository-content, branch, release, package, Pages, fact, or projection write access to a client credential.
- Central services, centrally stored replay files, persistent secrets, or fork-pull-request result transport.
- Completing the separate M-009 user-documentation milestone.

## Compatibility

Local development uses the merged Runner source while ranked distribution remains gated on an immutable released Runner artifact. Ranked execution fails closed on unsupported schemas, inconsistent snapshots, source-hash or identity mismatches, incomplete battles, and behavior-version incompatibility. Practice mode has no path to ranked journal or submission state.
