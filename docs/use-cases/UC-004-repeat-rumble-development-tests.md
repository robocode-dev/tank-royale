---
id: UC-004
type: use-case
status: draft
links: [G-004, CAP-012, CAP-014, CAP-015, CAP-016]
title: Repeat and reset development Rumble tests without affecting public participants
provenance: inferred
reversal-cost: low
---

# Repeat and reset development Rumble tests without affecting public participants

## Actors

The maintainer or developer, local test tooling, the development catalog/result workflows, and an outside test account.

## Trigger

A code, workflow or documentation change needs reproducible cross-component verification.

## Main flow

1. Run the documented local integration command with an isolated fixture workspace; inspect its concise report and redacted diagnostics.
2. When actual GitHub behavior needs verification, select the clearly marked development Rumble and prepare its known fixtures and account prerequisites.
3. Exercise the relevant author, contributor, withdrawal or dashboard journey; collect versions, environment identities, receipts and observations automatically.
4. Preview and reset only the development resources when needed, creating a fresh test generation and leaving public resources untouched.
5. For released changes, persist a run manifest before the short outside-account public-Beta smoke check and label clean versus prepared evidence honestly. Withdraw every temporary bot/team/version created by the run and verify absence from active published rankings and matchmaking; retain accepted facts and snapshots.

## Outcome

Development tests can be repeated without hand-transcribing evidence or contaminating public rankings. A development test result is never represented as proof of live installation permissions. M-029 supplies the initial harness, feature milestones extend it, and M-037 proves the complete single-command suite and automatically redacted report.

## Failure and recovery

A public destination, mismatched environment, or ambiguous reset target is rejected before mutation. Resetting test state cannot import old queued results into the new generation. Missing GitHub permissions or publication failures are reported with retained evidence rather than repaired using a maintainer identity and declared successful.

Interrupted or failed public smoke runs resume cleanup from their durable manifest. If withdrawal is temporarily unavailable, report pending cleanup and leave the final acceptance milestone incomplete; never solve it with a public reset.
