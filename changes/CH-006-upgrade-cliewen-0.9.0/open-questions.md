---
id: CH-006-open-questions
type: change
status: active
links: [CH-006]
title: Open questions for CH-006
---

# CH-006 — Open questions

## Q1 — G-001 is inferred, and its reversal is not cheap

**Blocking.** [G-001](../../docs/goals/G-001-programming-game-for-learning-and-competition.md) carries `provenance: inferred` and `status: accepted`, so 0.9.0 requires it to declare a reversal cost. Both honest answers have consequences, and neither is an agent's to pick:

- **`reversal-cost: high`** is the truthful classification — reversing the product's goal is not cheap and local. But under ADR-035 a high-cost inferred artifact cannot sit in an active capability's activation slice, and G-001 is one link from all twelve active capabilities. Validation then reports twelve activation blockers and CI stays red until the goal is verified.
- **`provenance: verified`** removes the requirement and the blockers, and is very likely the right end state: G-001 was extracted from `openspec/project.md` at CH-001 and describes this product accurately. But verification is a human act — an agent recording it would make the field mean nothing.
- **`reversal-cost: low`** would keep CI green while asserting something false about the goal. Rejected.

Every other inferred artifact was classifiable without this conflict: the criteria and design files are `status: draft` and bind nothing yet, and the architecture, flow, plan, and analysis records are descriptions whose correction costs one file edit.

**Needed from the maintainer:** read G-001 and either confirm it is right — in which case its provenance becomes `verified` and the field is no longer required — or say it needs work, in which case `high` is recorded and the twelve blockers stand until it is revised and verified.
