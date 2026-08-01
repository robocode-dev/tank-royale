---
cliewen-skill: true
version: 0.11.2
---

<!-- Generated from Cliewen's canonical skill sources; edit those sources, not this file. -->

# clue-plan

Use when creating a plan or changing what a plan promises.

1. Create or revise a plan through `clue-delta`; a plan mutation is itself a branch and PR. The digest is the plan file in `/docs/plans/`.
2. Keep plans as flat `P-xxx-slug.md` files with status in frontmatter (`draft` → `active` → `completed`). Milestones (`M-xxx`) are rows in the plan's milestone table, each with a verifiable exit criterion.
3. Treat semantic mutation and bookkeeping differently:
   - **Semantic:** Direction, scope, milestone addition/removal, or anything else that changes the plan's promise requires human acceptance and a decision record under **Decision records** below. Agents may propose; only humans accept. The default vehicle is a dedicated plan change and PR. A revision discovered during implementation may ride with that implementing change only when the PR declares the plan revision, a correctly typed decision record backs it, the PR calls it out for deliberate approval, and an explicit objection can revert the revision while leaving the milestone open without blocking the rest of the change.
   - **Bookkeeping:** Marking a milestone done belongs in the implementing change's merge digest, never a separate PR.
4. Treat `status: completed` as immutable and never delete a completed plan. Before freezing it, distill its durable lessons and rejected paths into decision records.

## Decision records

Route every decision by reversal cost. A cheap-and-local-to-reverse decision is a dated row in `docs/decisions/log.md` (columns `Date | Decision | Why | Change/PR`); otherwise write an ADR for software or corpus architecture, or a PDR for how the project works. A decision adopting a well-established practice cites it by name and records only the local why.

Agent-authored decisions start `status: inferred` and `author: agent`. Merging makes them binding without changing that status. Only explicit human approval promotes a decision to `verified`; record every approver in `accepted-by:`, use the first approval date, and cite the venue. An explicit objection keeps the decision `inferred` and becomes an open question.

`accepted-by:` records only approval given under Cliewen's merge boundary, never acceptance a source record already carried. A record converted from a format with its own acceptance history — names, roles, dates predating the corpus — preserves that history as body prose and keeps `accepted-by: []`, the same empty list any unsigned record carries.

Every decision record is timeless: state what is decided and only the enduring context and rationale needed to understand it. Keep triggering incidents, chronology, conversations, implementation details, and review history in findings, the change workspace, the PR, and Git history.

A decision that changes a methodology contract inventories every live carrier that states the affected contract and updates that complete inventory in the same change. Live carriers include current corpus truth, canonical and generated skills, templates, public or contributor guidance, implementation explanations, CLI text, and distribution metadata. Historical analyses, completed plans, and changelog entries remain pinned history. Add focused guards for stable repaired claims, but do not present those anchors as proof that an arbitrary future carrier inventory is complete; that general obligation remains agent-enforced until a mechanism can derive it.
