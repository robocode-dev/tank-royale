---
cliewen-skill: true
version: 0.10.0
---

<!-- Generated from Cliewen's canonical skill sources; edit those sources, not this file. -->

# clue-extract

Brownfield adoption: transform an existing repository's specification corpus into a Cliewen `/docs` corpus. Use once per adopted repository; the extraction is that repository's first `clue-delta` loop. Apply the **Rehearsal before mutation**, **Decision records**, **Repository-local conventions**, and **Review boundary** below.

## Rehearsal before mutation

After the extraction's full change is proposed, begin with a mandatory report-only pass. Write the rehearsal report under `/changes/<CH-xxx-slug>/`; do not change the target source corpus, Cliewen `/docs` corpus, tests, routing, or hosted state.

The rehearsal report inventories source formats and entry points, proposed artifact mappings, preserved and minted IDs, confidence and reversal cost, test-purpose work, instruction conflicts, planned deletions, and named plan doors. An unresolved conflict becomes an `open-questions.md` entry and stops before mutation.

Only explicit human direction begins the existing full extraction change's mutate phase. That phase digests the rehearsal into the durable extraction report under `/docs/analysis`, then performs the accepted conversion; the ready PR deletes both the transient change workspace and the parallel source corpus.

## Target contract

The extraction PR is complete only when all of these hold:

1. **The full taxonomy exists:** `/docs` has goals, plans, capabilities, decisions, constraints, analysis, and architecture, with an indexed README in every folder and README, criteria, and design files in each capability folder. Extract meaning; do not invent it. A folder with nothing real to hold stays empty but indexed.
2. **Everything extracted is born inferred and cost-routed:** Use `provenance: inferred` plus `reversal-cost: low|high` on every non-decision artifact, classifying whether its meaning is cheap and local or expensive to reverse. Decisions instead use `status: inferred` and `author: agent`; their ADR/PDR record type is already the high-cost route. Human review may promote records to `verified`, file by file or in bulk. An active capability cannot depend by one `links:` edge on high-cost inferred meaning, while low-cost inferred findings may remain legitimately deferred. Extracted decisions also follow **Decision records** below.
3. **Existing criterion IDs survive; a criterion with none is minted deterministically:** Declare each capability's namespace with `ac-prefix:` and keep source IDs verbatim — never renumber, since IDs are meaning-immutable and existing test tags must keep resolving. A namespace may contain one or more uppercase alphanumeric segments joined by single hyphens, and a canonical criterion ID may carry a lowercase letter suffix after its numeric portion; case and punctuation are exact in the corpus. When a source requirement carries no stable ID of its own, mint one in its capability's `ac-prefix:` namespace: take the requirements without IDs in the source's own stated or file order and assign the next numeric slot after the maximum numeric component already declared in that namespace, ignoring letter suffixes for the maximum; an empty namespace starts at one. The corpus is the registry, a minted ID never collides with one kept verbatim, and the same source state always mints the same IDs. Record the preserved and minted mapping in the extraction report below.
4. **Every test keeps or gains exactly one purpose:** Existing criterion tags remain. Untagged tests get `Unit`, `Sanity`, or `Arch` according to intent. On the JVM, normalize each supported Java or Kotlin executable so its canonical AC identity, proof type, and direction attach together through literal JUnit method tags or the stable `test<PREFIX><digits>[lowercase-suffix]_<Type><Direction>_<description>` name; remove hyphens from segmented prefixes only in the named form and use underscores for hyphens in literal JUnit tags. Class-level AC tags, comments, dynamic or multi-line tag expressions, and metadata split across methods are unsupported evidence; record and resolve the gap instead of installing an external rule or letting `clue` guess.
5. **Evidence status is explicit at the narrowest honest level:** Whole-file draft phasing remains available: a capability whose extracted criteria are not ready for active use stays `status: draft`, and the extraction report names the gap and plan door. An active criteria file may also phase individual promises: tag each genuinely not-yet-proven criterion `@draft`, exempting only that criterion while evidence-backed siblings remain active. A genuine `Test-type: Human` criterion is already proven by naming it in the pull request acceptance brief and needs no code reference; it is not a placeholder for a missing test. Machine-proven criteria use supported Go, per-executable JVM, or Cucumber evidence classified by their declared `Unit`, `Integration`, `E2E`, or `Performance` type and positive/negative direction, with `(single-direction)` only when one direction is honest; legacy criteria without a declared proof type retain the one-supported-reference rule. A capability is therefore not the smallest activation unit. Extraction is never partial by silent omission: every draft file or criterion states why it is draft and names the change that will prove it.
6. **`clue validate` is green before the ready PR opens:** The extracted corpus is judged by the same rules as a greenfield corpus.
7. **The source corpus dies in the same PR:** Delete parallel specification trees, registries, and source-format skills; Git history is their archive. Two systems of record is zero systems of record.
8. **Routing is rewritten and reconciled:** Point every assistant entry point the repository carries — `AGENTS.md` and any other assistant-specific entry file, such as `CLAUDE.md` or `.cursor/rules` — to `/docs/README.md` and the installed `clue-*` skills; `AGENTS.md` is the flagship instance of this class, not its only member. Absorb compatible pre-existing instructions as repository-local conventions; record conflicts as open questions.
9. **An extraction report lands in `/docs/analysis`:** The mutate phase digests the rehearsal into the report, recording what was found, what mapped where, and what was dropped and why.
10. **Unsolved adoption items become named plan doors:** Never leave a silent gap.
11. **Every converted file carries exactly one frontmatter block:** When a source file already has frontmatter, the conversion replaces it, folding retained fields into the Cliewen block — even when an invisible prefix such as a UTF-8 byte-order mark hides the opening fence. Strip BOMs; after conversion, sweep the tree for a complete frontmatter block at the start of an artifact body: leftover source frontmatter is an extraction failure, and `clue validate` rejects both shapes.
12. **The committed extraction receives a clean agentic review:** Run `clue-verify`, including its automatic review loop, before opening the ready extraction PR.

## Source mappings

Per-source mappings live in this skill's `mappings/` folder: [openspec.md](mappings/openspec.md) for OpenSpec corpora, [madr.md](mappings/madr.md) for MADR and Nygard-style decision records. The target contract above governs every extraction; a mapping only describes one source format. A new source format adds a mapping file, never another skill. If no mapping exists, writing one is the extraction PR's first task.

## Boundaries

Never invent unstated requirements, renumber or rename IDs, leave the source corpus alive for reference, promote your own output to `verified`, or change test code beyond adding or normalizing the supported purpose and evidence metadata.

## Decision records

Route every decision by reversal cost. A cheap-and-local-to-reverse decision is a dated row in `docs/decisions/log.md` (columns `Date | Decision | Why | Change/PR`); otherwise write an ADR for software or corpus architecture, or a PDR for how the project works. A decision adopting a well-established practice cites it by name and records only the local why.

Agent-authored decisions start `status: inferred` and `author: agent`. Merging makes them binding without changing that status. Only explicit human approval promotes a decision to `verified`; record every approver in `accepted-by:`, use the first approval date, and cite the venue. An explicit objection keeps the decision `inferred` and becomes an open question.

`accepted-by:` records only approval given under Cliewen's merge boundary, never acceptance a source record already carried. A record converted from a format with its own acceptance history — names, roles, dates predating the corpus — preserves that history as body prose and keeps `accepted-by: []`, the same empty list any unsigned record carries.

Every decision record is timeless: state what is decided and only the enduring context and rationale needed to understand it. Keep triggering incidents, chronology, conversations, implementation details, and review history in findings, the change workspace, the PR, and Git history.

A decision that changes a methodology contract inventories every live carrier that states the affected contract and updates that complete inventory in the same change. Live carriers include current corpus truth, canonical and generated skills, templates, public or contributor guidance, implementation explanations, CLI text, and distribution metadata. Historical analyses, completed plans, and changelog entries remain pinned history. Add focused guards for stable repaired claims, but do not present those anchors as proof that an arbitrary future carrier inventory is complete; that general obligation remains agent-enforced until a mechanism can derive it.

## Repository-local conventions

For a Cliewen change, apply the repository-local conventions declared in AGENTS.md, including digest requirements such as a user-facing changelog entry. When a release adds or narrows a corpus obligation, preview and apply the supported `clue migrate` migrations before validating the adopted repository; `clue init` remains a non-destructive materializer, not an updater. Plain changes follow only the repository conventions that apply to their changed surface. Local conventions extend the methodology and never override it. If AGENTS.md conflicts with a skill, record the conflict in `open-questions.md` and stop for a human decision; never choose silently.

## Review boundary

Every change branches from the current tip of `main`, never from unaccepted work. Each initiating author takes one Cliewen change to its PR before starting another; independent authors may work in parallel from `main`, and plain changes do not consume this slot. Reviewing or helping update an existing PR does not mint another change or create a global lock. If work must build on an unmerged change, record a blocking open question and stop unless the human explicitly authorizes it. If another change merges before first publication, rebase onto the new `main` tip and repeat verification. After a PR is published, incorporate a newer accepted `main` by merging it into the PR branch with a normal push, never by rewriting hosted history, then repeat verification and review.

For a full Cliewen change, the human accepts the ready pull request with a merge commit. Configure the protected default branch to allow merge commits and disable squash and rebase-and-merge: the merge commit keeps the proposal, implementation, digest, and durable corpus commits reachable from `main`, while the other modes can discard or rewrite that reviewed chain. Rebasing an unpublished local branch before its first publication remains allowed; it is preparation, not the acceptance mode. A forge that cannot enforce the merge-commit boundary is outside the supported full-change adoption path.

Open the PR ready for review only after local verification and the automatic agentic review loop pass on the current commit, never as a draft. The PR is the completed proposal's authorization and protected-integration boundary, not a demand for duplicate human code review: the agent may publish the candidate, but only a human-controlled PR merge accepts it. Unfinished work stays on the branch. An agent never merges its own PR, creates a local merge commit into `main`, or pushes to `main`.

A PR alone displays hosted CI but does not enforce it. Where hosting supports enforcement, the PR triggers CI, branch protection makes its required status check a merge precondition, and the agent cannot silently skip the gate. Never weaken the workflow or required-check policy to make a change pass.

Every review of an existing hosted PR is bound to its observed head SHA. A clean result applies only to that commit; every substantive edit invalidates it. An actionable finding is durable PR state, not private agent memory: where the host supports resolvable review conversations, publish the finding there and leave it unresolved until a hosted commit contains the reviewed repair. If the reviewer cannot publish a resolvable finding, report the PR as not merge-ready and disclose that the host cannot enforce the finding; never claim a chat-only finding has equivalent protection.

Any agent that edits an existing PR becomes the updater for that turn. Before editing, fetch the PR and record its hosted head; before publishing, recheck that head and push only a normal fast-forward update, never force. If the head changed or the push is rejected as non-fast-forward, fetch and reconcile without overwriting remote work, then rerun verification and review on the resulting commit. If the PR merged or closed, stop and report local work as unpublished; never create a follow-up change without explicit human scope.

Ready means the hosted PR contains the exact locally reviewed and verified state. Before reporting any change ready, commit every intended edit, run the applicable local verification and a clean agentic review pass against that commit, require `git status --porcelain` to be empty, push the reviewed commit, and confirm that the ready hosted PR's head branch and SHA equal the current local branch and `HEAD`. Perform the hosted check immediately after opening or updating the PR. Resolve satisfied review conversations only after the hosted head contains their reviewed repair. If either side differs, apply the updater rule above, rerun verification and review on the resulting commit, require a clean worktree, push the reviewed commit, and check again. A human-requested local stopping point such as "commit only" is preserved work, not a completed or mergeable change, and the agent says that no ready PR exists.

After opening and confirming its initiated PR, an agent stops before initiating another light or full Cliewen change; independent plain changes may still proceed from accepted `main`, and the agent may review or help update an existing PR under the handoff above. Review fixes stay on the same branch and PR and repeat the complete updater handoff before reporting ready again. A follow-up Cliewen change exists only when a human has accepted this one and explicitly scoped the follow-up.

After a human reports the merge, orient before starting anything else: describe the plan's next unfinished step in plain language and ask whether to start it, or say that the plan has nothing left and ask what comes next.
