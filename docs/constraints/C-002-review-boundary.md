---
id: C-002
type: constraint
status: active
links: []
title: Every mutation of main goes through a branch and a human-merged PR
source: core-principles agent instructions (pre-CH-001), Cliewen change loop
enforcement: human
---

# C-002 — Every mutation of main goes through a branch and a human-merged PR

No direct commits or pushes to `main` — by agents or anyone else. Every change rides a branch, becomes a PR, and a human (the maintainer) merges it. Agents never merge their own PRs and never push to `main`; an agent that finishes a change ends at an open PR, not a merge.

This replaces the pre-Cliewen prose gates ("never commit without explicit approval", the ADR Review Gate, the OpenSpec approval gate): the PR **is** the approval gate, and the corpus wall (`clue validate --forbid-changes` in CI) is the machine half of the review.

**Promotion trigger:** branch protection on `main` with `validate` as a required status check — then `enforcement: machine` for the wall half; the human merge stays human by design.
