## Planning workflow

Use when creating a plan or changing what a plan promises.

1. Before planning anything, check what direction the corpus states: run `clue validate --intent`. A plan whose goals serve no stated vision is not blocked, but a repository that has never stated one is the moment to offer [Intent discovery](intent-discovery.md), and a plan that contradicts an active vision is a conflict to raise rather than to plan around.
2. Create or revise a plan through `clue-delta`; a plan mutation is itself a branch and PR. The digest is the plan file in `/docs/plans/`.
3. Keep plans as flat `P-xxx-slug.md` files with status in frontmatter (`draft` → `active` → `completed`). Milestones (`M-xxx`) are rows in the plan's milestone table, each with a verifiable exit criterion.
4. Treat semantic mutation and bookkeeping differently:
   - **Semantic:** Direction, scope, milestone addition/removal, or anything else that changes the plan's promise requires a declared revision and human direction. A revision may ride with the implementing change that uncovered it; use a plan-only change when no implementation is active or it makes the review clearer. A revision is not automatically a decision record: record one under [Decision records](decision-records.md) only when the selected course is a future-shaping choice.
   - **Bookkeeping:** Marking a milestone done belongs in the implementing change's merge digest, never a separate PR. Closing the plan is the same bookkeeping: the change completing the last milestone also sets it `completed`, in that digest. A campaign is over the moment its last milestone is evidenced, so leaving it `active` publishes an index claiming work is in flight that is not. Designate the successor plan there too when one is decided; not having decided one never holds the closure open. Every milestone's evidence must be in the table before that digest lands, because the closed plan is immutable afterwards.
5. Treat `status: completed` as immutable and never delete a completed plan.
