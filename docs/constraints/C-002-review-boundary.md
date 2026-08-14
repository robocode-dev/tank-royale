---
id: C-002
type: constraint
status: active
links: []
title: Mutations of main are human-gated, by a merged PR or an authorized direct push for simple work
source: core-principles agent instructions (pre-CH-001), Cliewen change loop, maintainer authorization (CH-027)
enforcement: human
---

# C-002 — Mutations of main are human-gated, by a merged PR or an authorized direct push for simple work

Every full Cliewen change rides a branch, becomes a pull request, and is merged by a human (the maintainer). No exception: a full change is never committed or pushed to `main` directly, by agents or anyone else, and an agent that finishes one ends at an open PR, not a merge.

Simple work may be committed and pushed to `main` directly, but only when the maintainer explicitly authorizes that push for that specific change. The authorization is per change and never carries over: a prior authorization, a general permission, or the fact that similar work was authorized before does not authorize the next push. The simple route itself authorizes nothing; classifying work as simple is a statement about the accepted contract, never about integration. Without an explicit authorization for the change at hand, simple work also rides a branch and a pull request.

An authorized direct push carries an `Authorized-Push:` trailer naming the authorization on each commit it lands. A human-enforced permission that leaves no evidence cannot be audited afterwards: without the trailer, an authorized push and an unauthorized one are indistinguishable in `main`'s history.

Agents never merge their own PRs.

This replaces the pre-Cliewen prose gates ("never commit without explicit approval", the ADR Review Gate, the OpenSpec approval gate): for a full change the PR **is** the approval gate, and the corpus wall (`clue validate --forbid-changes` in CI) is the machine half of the review.

**Promotion trigger:** branch protection on `main` with `validate` as a required status check, configured so the maintainer keeps the ability to push directly, then `enforcement: machine` for the wall half. Protection that requires a pull request with no bypass would make the simple-work exception unexercisable, so that configuration is not a promotion of this constraint but a change to it, and this constraint is revised first. The human merge and the per-change authorization stay human by design.

**Residual:** Human review and merge remain deliberately human-enforced even when branch protection machine-enforces the validation wall. The per-change push authorization has no machine half at all: only the maintainer knows whether a given push was authorized, and the `Authorized-Push:` trailer records that claim rather than proving it.
