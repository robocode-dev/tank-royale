---
version: 0.3.0
cliewen-skill: true
---

# clue-plan

Use when creating a plan or changing what a plan promises.

1. **A plan is itself a change**: create it via `clue-delta` (branch, `/changes/`, PR). The digest is the plan file in `/docs/plans/`.
2. Plans are flat files, `P-xxx-slug.md`, status in frontmatter (`draft` → `active` → `completed`). Milestones (`M-xxx`) are rows in the plan's milestone table, each verifiable.
3. Two kinds of mutation, different rules:
   - **Semantic** (direction, scope, milestones added/removed — anything altering what the plan *promises*): its own change/PR, human-accepted, backed by a decision record — a PDR for direction and process, an ADR if architectural. Plan adjustments ARE decisions. Agents may *propose*; only humans accept.
   - **Bookkeeping** (milestone marked done): part of the feature change's merge digest, never a separate PR.
4. `status: completed` freezes the plan **immutable, never deleted**. Before freezing, distill the plan's lessons (direction changes, rejected paths) into decision records: the plan is the ledger, the records are the wisdom.
