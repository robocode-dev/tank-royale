---
id: UC-003
type: use-case
status: draft
links: [G-004, CAP-012, CAP-014, CAP-015, CAP-016]
title: Withdraw an entry without erasing competition history
provenance: inferred
reversal-cost: low
---

# Withdraw an entry without erasing competition history

## Actors

The bot owner or a moderator, bot management, catalog publication, and ranking aggregation.

## Trigger

An owner no longer wants an entry competing, or a moderator needs to remove a test or unwanted entry from active competition.

## Main flow

1. Select the exact bot version in My bots and choose Withdraw from Rumble. Moderators may select other owners' entries through their authorized management view.
2. Inspect the environment, identity, dependent teams, active-slot effect and effect on current rankings, then confirm.
3. Trusted automation checks authority and records the withdrawal. The entry and its dependent teams become ineligible; unrelated member bots remain active.
4. Observe catalog and dashboard publication. The entry leaves active rankings, its active slot is freed, and its historical records and ownership remain.

## Outcome

The entry no longer participates in active competition without being treated as disqualified or deleting past accepted results. Explicit restoration is possible only when all eligibility conditions still hold.

## Failure and recovery

An unauthorized actor cannot withdraw or restore an entry. A stale version selection is shown as a conflict, not redirected to a newer version. Repeated withdrawal is safe and publication delay is visible. Restoration cannot override bans, superseding versions, or inactive team dependencies. Withdrawal of a public test bot never invokes the test-environment reset operation.
