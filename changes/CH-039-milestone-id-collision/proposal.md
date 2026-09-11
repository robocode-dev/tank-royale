---
id: CH-039
type: change
status: draft
links: [P-004]
title: Resolve duplicate milestone identity
---

# CH-039 — Resolve duplicate milestone identity

## What

Cliewen 0.25.0 now validates milestone identities globally, and the corpus has two different milestones named M-016. Preserve P-005's completed TypeScript console-capture milestone as M-016 and assign a fresh coordinated identity to P-004's still-todo container-runtime milestone, updating its internal references and ledger state. This change also carries the pending Cliewen 0.25.0 migration so the repository's managed carriers and validation wall move together.

## Why

The duplicate identity makes the corpus invalid and would keep the upgraded CI validation wall red. Reassigning the unfinished P-004 row avoids changing the completed P-005 milestone or the decision record that names it, while keeping the plan's promise and dependencies intact.

The change affects corpus bookkeeping and Cliewen adoption metadata only; it does not change product behavior or acceptance criteria. The repository has no vision artifact, so this change proceeds without one.
