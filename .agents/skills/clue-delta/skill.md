---
cliewen-skill: true
version: 0.10.0
---

<!-- Generated from Cliewen's canonical skill sources; edit those sources, not this file. -->

# clue-delta

Use for every Cliewen change: features, fixes, docs, and plans whose meaning belongs in the corpus or methodology. Plain changes are classified by AGENTS.md before the corpus is loaded and do not invoke this skill. Apply the **Change scope and tiers**, **Decision records**, **Repository-local conventions**, and **Review boundary** below throughout the loop.

1. **Branch:** Follow the review boundary and name the branch `ch-xxx-slug`. Take the next free CH number by searching Git history and `/changes/` for the highest used number.
2. **Propose:** For a full change, create `/changes/CH-xxx-slug/` and commit it before implementation:
   - `proposal.md` states what and why; its frontmatter `links` names the real plan item it serves or explicitly declares the change plan-less.
   - `tasks.md` is an ordered `- [ ]` checklist with dependencies first and at most one nested level. Mark `[x]` the moment a task completes, never in batch at the end. Mark an infeasible task `[-]` with its reason on the same line. A behavior-changing task names the acceptance-criterion IDs it serves; if none exists, add the criterion before implementation. Tests trace to criteria, never transient tasks.
   - `open-questions.md` records blocking questions. When one appears, write it and stop; the human answer becomes a decision record.
   - A human may opt into a spec-first pause after Propose. Record the pause in `tasks.md` and stop before implementation until the human directs work to continue; otherwise proceed directly to Implement.
3. **Implement:** Update the permanent corpus. Capabilities own README, criteria, and design files. Write criteria as Gherkin tagged with their canonical `<PREFIX>-<digits>[lowercase-suffix]` identity; every new or materially revised criterion declares `Test-type: Unit|Integration|E2E|Performance|Human` on the first line of its scenario body and gets focused positive and negative evidence in that class (or records `(single-direction)` when only one direction exists). `Human` needs no code evidence — the acceptance brief's criteria line is its proof; use it for a criterion deliberately verified by human judgment, never as a placeholder for a test not yet written. A criterion genuinely not yet proven carries `@draft` on its tag line instead, exempting only that criterion from the active-file test requirement — the capability itself does not need to stay `draft` because one criterion is unproven. Every test declares exactly one purpose: the criterion ID, `Unit`, `Sanity`, or `Arch`, using framework tags where available and the normalized test-name prefix in Go; AC evidence also carries its declared test type and direction. On the JVM, all three evidence parts attach to the same Java or Kotlin executable through literal JUnit method tags or the stable `test<PREFIX><digits>[lowercase-suffix]_<Type><Direction>_<description>` name; class tags, comments, and unrelated methods cannot supply missing parts. When a criterion's meaning changes, retire it with `@retired`, keep the tombstone, mint a new ID, and remove or retag its tests.
4. **Digest:** After every task is `[x]` or `[-]` with a reason, update permanent `/docs`, regenerate README indexes, apply repository-local digest conventions, record decisions, and update plan bookkeeping. Retiring a non-criterion artifact means deleting its file in this same digest — never leaving a `status: retired` file behind — and naming the dead ID in a `supersedes:` field on its successor, or on `docs/decisions/log.md` when a demoted decision has none; criteria tombstones (`@retired`, file kept so the test tag keeps failing) and completed plans (frozen, never deleted) are the named exceptions. Delete the change workspace. The digest is never a task in `tasks.md`; deletion is the digest, so a self-referential digest task cannot be completed honestly.
5. **Verify, review, and propose for acceptance:** Run `clue-verify`, including its automatic agentic review loop on the verified committed candidate, then open the PR under the review boundary. For a full change, fill the acceptance brief at the top of the PR body with the plan item and whether it remains wanted, every added or changed criterion and its scenario-resolution verdict — naming any newly or materially declared `Human`-class criterion there as its proof — and what merge binds or supersedes; keep it to one screen and never leave template placeholders. Never ask the human to initiate the review. Merging accepts the change; decision provenance follows **Decision records** below.

Keep deltas small: Git merges text, not meaning.

## Change scope and tiers

Classify scope before using the Cliewen loop. Three rules decide the tier, by how deeply the change reaches into meaning; take the first rule that matches.

1. **Plain — nothing about meaning changes.** No product behavior, intent, evidence, decision, plan, policy, or methodology changes. Protected product, corpus, test, configuration, build/release, governance/security, agent-rule, skill, and lint surfaces are never plain; neither are commands, contracts, workflows, or normative instructions. Plain work stays outside this skill: branch from `main`, run relevant checks, open a ready PR for human merge, and use no CH identity or Cliewen bookkeeping.
2. **Light — meaning is touched but not changed.** No decision, acceptance-criterion or capability meaning change, semantic plan mutation, or methodology carrier. Typical: protected-surface clarity, dependency bumps, pure refactors, and CI plumbing. Use a Cliewen branch and ready PR whose description names the plan item or plan-less scope, but no transient workspace.
3. **Full — everything else.** Product behavior changes are full even when an existing criterion already states the behavior. Use the whole loop with `/changes/CH-xxx-slug/`.

Two guards hold above the rules. **Uncertainty escalates:** when the tier is unclear, take the higher one. **Discovery escalates immediately:** the moment a decision, an open question, a meaning change, or a methodology-carrier edit appears during work, move to the full loop before continuing.

## Decision records

Route every decision by reversal cost. A cheap-and-local-to-reverse decision is a dated row in `docs/decisions/log.md` (columns `Date | Decision | Why | Change/PR`); otherwise write an ADR for software or corpus architecture, or a PDR for how the project works. A decision adopting a well-established practice cites it by name and records only the local why.

Agent-authored decisions start `status: inferred` and `author: agent`. Merging makes them binding without changing that status. Only explicit human approval promotes a decision to `verified`; record every approver in `accepted-by:`, use the first approval date, and cite the venue. An explicit objection keeps the decision `inferred` and becomes an open question.

`accepted-by:` records only approval given under Cliewen's merge boundary, never acceptance a source record already carried. A record converted from a format with its own acceptance history — names, roles, dates predating the corpus — preserves that history as body prose and keeps `accepted-by: []`, the same empty list any unsigned record carries.

Every decision record is timeless: state what is decided and only the enduring context and rationale needed to understand it. Keep triggering incidents, chronology, conversations, implementation details, and review history in findings, the change workspace, the PR, and Git history.

A decision that changes a methodology contract inventories every live carrier that states the affected contract and updates that complete inventory in the same change. Live carriers include current corpus truth, canonical and generated skills, templates, public or contributor guidance, implementation explanations, CLI text, and distribution metadata. Historical analyses, completed plans, and changelog entries remain pinned history. Add focused guards for stable repaired claims, but do not present those anchors as proof that an arbitrary future carrier inventory is complete; that general obligation remains agent-enforced until a mechanism can derive it.

## Repository-local conventions

For a Cliewen change, apply the repository-local conventions declared in AGENTS.md, including digest requirements such as a user-facing changelog entry. When a release adds or narrows a corpus obligation, preview and apply the supported `clue migrate` migrations before validating the adopted repository; `clue init` remains a non-destructive materializer, not an updater. Plain changes follow only the repository conventions that apply to their changed surface. Local conventions extend the methodology and never override it. If AGENTS.md conflicts with a skill, record the conflict in `open-questions.md` and stop for a human decision; never choose silently.


## Durable work state

An agent's private memory is never where work lives. Anything needed to implement, continue, review, or hand off work belongs in a corpus artifact, the change workspace, or the pull request; private conversation does not survive a change of agent.

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
