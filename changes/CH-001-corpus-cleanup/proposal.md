---
id: CH-001
type: change
status: open
links: []
title: Clean pre-existing Cliewen corpus findings
---

# CH-001 — Clean pre-existing Cliewen corpus findings

## What

Bring the Tank Royale corpus to a clean `clue validate` result after the Cliewen 0.16.0 upgrade, beginning with mechanical Markdown layout findings and then resolving metadata and source findings with human-confirmed provenance.

## Why

The 0.16.0 upgrade exposes 537 findings that already exist on `origin/main`. Leaving them unresolved makes the new validation wall permanently red and prevents the repository from benefiting from the upgraded checks.

## Dependency

This cleanup is intentionally based on the unmerged `upgrade-cliewen-0-16-0` branch because it uses the 0.16.0 validator, generated skills, caller, and identity ledger. The user explicitly authorized cleanup before merging the release upgrade. Accepting this change therefore binds both the upgrade and this cleanup; neither branch is an independent base.

## Scope

- Reflow existing hard-wrapped Markdown without changing meaning.
- Add or repair decision `author` and capability `goal` metadata only where corpus history or a human confirms the value.
- Resolve stale constraint sources and missing residual declarations.
- Re-run validation after each batch and leave no unexplained findings in the final candidate.
