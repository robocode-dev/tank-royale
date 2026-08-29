---
id: ADR-044
type: decision
status: verified
links: [P-003, M-008, CAP-015, CAP-016, ADR-043]
title: Durable Rumble Result Acknowledgement
author: agent
accepted-by: Flemming N. Larsen (2026-08-03, Codex conversation)
---

# ADR-044: Durable Rumble Result Acknowledgement

## Context

The Rumble client retains ranked results in a local journal until ingestion acknowledges them. Result ingestion crosses two independently failing boundaries: publishing accepted facts to the canonical data repository and delivering per-result outcomes through the forge issue API. A receipt published before its fact can cause data loss, while a fact published without a retryable receipt can leave the client queue stuck.

## Decision

The result-data workflow publishes a successful per-result receipt only after the commit containing its accepted fact reaches the canonical repository. Retrying an identical result that is already retained returns the same successful outcome without creating another fact. A conflicting reuse of a battle ID or payload remains rejected.

The client removes a journal record only after correlating it with that post-publication successful receipt. Publication failure or missing receipt leaves the record retryable.

## Rationale

Publication-first ordering ensures a success never refers to an uncommitted fact. Idempotent retry closes the opposite failure window: if fact publication succeeds but receipt delivery fails, the next attempt can safely recover the success instead of losing the fact or permanently blocking the journal.

## Consequences

- Accepted facts remain exactly-once even when submissions are retried.
- Successful receipts prove that the corresponding fact reached the canonical repository.
- Identical retries are successful no-ops; conflicting duplicates remain validation failures.
- Processed issues remain open when publication fails before receipt delivery.

## References

- [CAP-015 — Rumble result data](../capabilities/CAP-015-rumble-result-data/README.md)
- [CAP-016 — Rumble client](../capabilities/CAP-016-rumble-client/README.md)
- [Rumble result aggregation design](../design/rumble/aggregation-and-dashboard.md)
