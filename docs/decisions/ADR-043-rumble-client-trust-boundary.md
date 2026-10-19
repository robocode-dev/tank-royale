---
id: ADR-043
type: decision
status: verified
links: [P-003, M-008, CAP-016, CAP-015, ADR-044, ADR-049]
title: Rumble Client Trust Boundary
author: agent
accepted-by: Flemming N. Larsen (2026-08-16, Claude Code conversation)
---

# ADR-043: Rumble Client Trust Boundary

## Context

Ranked battles execute reviewed but untrusted bot source on contributor-owned machines. The Rumble needs useful community results without giving clients authority over repository state and without offering a convenient way to submit only favorable outcomes.

## Decision

The client exposes mutually exclusive ranked and practice modes. Ranked mode executes only the synchronized catalog and engine snapshot, journals every completed battle before transport, and removes records only after ingestion acknowledgement. Practice mode may execute local sources but cannot write ranked journal state or invoke submission.

The append-only local journal is the durability boundary and issue-ops is the only V1 submission transport. The client removes records only after the [publication-first ingestion acknowledgement](ADR-044-durable-rumble-result-acknowledgement.md). Under the accepted [browser-form submission decision](ADR-049-rumble-browser-form-submission.md), the client prepares complete batches and holds no submission credential; the contributor posts each unchanged batch through the result form. An account with push access may optionally use API issue creation with an Issues-only credential that grants no repository-content, branch, release, package, Pages, fact, or projection write access. That convenience is not the supported outside-contributor path or a requirement to retain an API-posting mode in the client. Fork-pull-request submission is introduced only if the result-data contract later supports it.

Replay evidence remains local, bound to its result by battle ID and SHA-256 hash. The primary runtime is a rebuildable, egress-constrained container; bare-metal execution is an explicit fallback with a weaker isolation boundary.

## Rationale

Separating private experimentation from automatic ranked journaling retains every completed ranked result for submission. The contributor posts complete prepared batches; the client offers no result-selection filter. Durable acknowledgement preserves results across publication and receipt-delivery failures. Credential-free form submission avoids entrusting the client with repository permissions, while local replay evidence supports moderation without making the shared data repository a binary store. The container provides one reproducible multi-runtime execution boundary without requiring central infrastructure.

## Consequences

- Ranked execution fails closed when synchronized identity, catalog, engine, or advice contracts are incompatible.
- The contributor posts each batch manually through the result form, and the client collects public receipts without a submission credential.
- Contributors can practice freely without generating ranked state.
- Temporary forge or network failures retain unsent results locally.
- Moderators may request replay evidence, but the Rumble cannot recover evidence a contributor loses.
- V1 depends on the forge's issue API; transport portability is deferred until the receiving capability implements it.
- Bare-metal users accept the residual risk of running reviewed bot source outside the container boundary.

## References

- [Cross-cutting design overview](../design/README.md)
- [CAP-016 — Rumble client](../capabilities/CAP-016-rumble-client/README.md)
- [CAP-015 — Rumble result data](../capabilities/CAP-015-rumble-result-data/README.md)
- [Rumble client design](../design/rumble/client-battles-and-results.md)
