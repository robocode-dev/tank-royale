---
id: CH-041
type: change
status: open
links: [P-004, M-011]
title: Reconcile M-011 Rumble delivery
---

# CH-041 — Reconcile M-011 Rumble delivery

## What

Reconcile P-004/M-011 with the accepted repository and hosted Rumble client state once the two parts of its exit criterion are verified together.

## Why

The Rumble documentation for P-003/M-009 is present on `main`, but `rumble-client#9` is still open and conflicting. The plan row therefore remains `todo`, while its evidence incorrectly says that the pull request is mergeable.

## Route

Recommended route: full. This change updates a plan-promise status and its durable evidence. Discovery would change the route only if M-011 were found not to require a corpus change after all.

## Plan

Serves [P-004/M-011](../../docs/plans/P-004-rumble-hardening.md).

## Scope

- Keep M-011 `todo` until the external pull request is actually merged by its maintainer.
- Re-fetch and verify the published Rumble documentation and `rumble-client#9` after the external merge.
- Update P-004's M-011 status and evidence, then run the corpus and change verification gates.

## Non-goals

- Merging `rumble-client#9`; agents do not merge their own or external pull requests.
- Changing the P-003/M-009 contract or the Rumble client implementation in this repository.
- Closing M-014, which remains gated on M-011 and the other P-004 prerequisites.
