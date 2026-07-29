---
id: CH-006
type: change
status: active
links: []
title: Upgrade Cliewen from 0.5.1 to 0.9.0 and restore the wall steps the local copy is missing
---

# CH-006 — Upgrade Cliewen from 0.5.1 to 0.9.0

**Plan-less.** No milestone in [P-001](../../docs/plans/P-001-cliewen-adoption.md) covers routine upgrades; M-003 (done at CH-004) covered the one-time move to a public pin, and M-001's "green on `main`" is a standing state this change preserves rather than a milestone it advances.

## What and why

This repository has run Cliewen 0.5.1 since CH-004 while the upstream released 0.6.0 through 0.9.0. Two things followed from that.

**The corpus no longer satisfies the current rules.** Under 0.9.0, an artifact with `provenance: inferred` must declare `reversal-cost: low | high` (upstream ADR-035), and `status: verified` is no longer in the vocabulary for an analysis. Running 0.9.0 against this repository unchanged reports 69 blocking issues: 68 missing `reversal-cost` fields and AN-001's status.

**The CI wall diverged.** `.github/workflows/clue.yml` is a copy of a file Cliewen ships, and CH-004 edited it to download the verified release binary instead of running a binary vendored under `.github/tools/`. Because the wall is a copy, later upstream steps never arrived. This repository's wall is missing two of them:

- **change-scope detection** — every pull request validates the corpus, including product-only ones that touch no corpus file;
- **the acceptance-brief gate** — a Cliewen pull request whose body carries no completed acceptance brief is not failed here, so the human merge gate has no enforced content. This repository also has no pull-request template, so nothing prompts the brief either.

The direct verified download is kept: it is this repository's deliberate choice and works. The divergence from the shipped wall is now confined to the install step and stated in the file's header comment, so the next upgrade can tell intent from drift.

## Scope

Full tier: it changes a protected agent-skill surface, adds a governance gate to CI, and records a routing judgment (`reversal-cost`) on 67 corpus artifacts.

Not in scope: verifying inferred meaning. Classifying an artifact's reversal cost is not the same act as a human confirming the artifact is right, and this change performs only the former. See `open-questions.md` for the single artifact where the two cannot be separated.
