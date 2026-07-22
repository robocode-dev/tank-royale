---
cliewen-skill: true
version: 0.5.1
---

<!-- Generated from Cliewen's canonical skill sources; edit those sources, not this file. -->

# clue-verify

Run this verification and review workflow before opening or updating any Cliewen PR. Complete the local checks and agentic review loop before publishing; complete the hosted-head check immediately after publishing and before reporting the PR ready. Plain changes use only checks relevant to their changed surface and do not invoke this skill. When the `clue` CLI exists, `clue validate` performs the mechanical half; until then, check by hand. Never fix a failure by weakening the check.

- [ ] The change uses the correct workspace under **Change scope and tiers** below.
- [ ] Every artifact touched has frontmatter `id`, `type`, `status`, `links`, and `title`, plus decision `author`/`accepted-by`, constraint `source`/`enforcement`, capability `goal`, and any other type-specific fields.
- [ ] Every `links` entry resolves to an existing ID.
- [ ] The proposal names a real plan item or explicitly declares the change plan-less.
- [ ] Plan bookkeeping reflects the merge, and no completed plan changed.
- [ ] Every active acceptance criterion has positive and negative tests, or its capability honestly stays `draft` with the gap stated.
- [ ] Every `/docs/**` folder has a README; index blocks list every sibling artifact and no deleted file.
- [ ] The change was assessed against every constraint and quality scenario.
- [ ] Repository-local conventions satisfy the contract below.
- [ ] Diagrams are inline Mermaid and readable when rendered.
- [ ] The full-change workspace is absent after digest; `main` never contains `/changes/`.
- [ ] Every decision satisfies **Decision records** below, including routing, timeless content, provenance, objections, and pending approval signatures.
- [ ] The current commit received a clean pass under **Agentic review loop** below; every substantive edit after an earlier clean pass triggered a new pass.
- [ ] The final handoff identifies the review mode (`context-isolated` or `in-context fallback`) and the reviewed commit.
- [ ] Every intended edit, including each review fix, is committed and `git status --porcelain` is empty.
- [ ] `git merge-base HEAD origin/main` equals `origin/main` after fetching; no other change workspace is visible on this branch.
- [ ] After publishing, the current branch is the ready hosted PR's head branch, its head SHA equals local `HEAD`, and the reported verification ran against that commit.
- [ ] The branch and hosted PR satisfy the **Review boundary** below.

## Agentic review loop

Run this loop automatically; never ask the human to clear context or initiate a separate review.

1. Finish every intended edit and commit the complete candidate. Determine the current commit and its base on accepted `main`, then run the applicable local checks against that commit.
2. If the coding-agent host supports context-isolated delegation, start a new read-only reviewer without the implementation conversation. Give it only the repository, branch, base, and declared intent: recover a full change's proposal from branch history; for a light change provide the user's request and accepted clarifications without implementation rationale. If isolated delegation is unavailable, perform an explicit adversarial pass in the current context and label it `in-context fallback`; never describe that fallback as independent review.
3. Instruct the reviewer to inspect the complete base diff, durable corpus, tests, constraints, and quality scenarios. It returns no edits, only actionable findings about correctness, intent mismatch, regressions, security, missing evidence, or unjustified complexity. Each finding includes severity, location, evidence, the operative requirement or declared intent that is violated, the concrete consequence, and a remediation. Apply authoritative decisions and the repository's explicit lifecycle rules before treating nearby wording as contradictory: human-controlled merge does not require duplicate human code review, and lifecycle-successor evidence satisfies a requirement when the repository declares that transition. Historical descriptions, optional activity, alternative readings, and lifecycle-correct state are not actionable defects by themselves. Exclude stylistic preference, optional improvement, and speculative scope expansion.
4. Resolve every actionable finding in the implementing context. A finding that requires a new decision or changed intent becomes an open question and stops the change. Otherwise commit the repairs and rerun applicable local checks against the repaired commit.
5. Start a new review pass after every substantive edit; a previous clean result applies only to the commit it reviewed. Continue until the current commit receives a pass with no actionable findings. Do not publish with unresolved findings or without a clean pass.
6. Report the final review mode and reviewed commit with the verification evidence. Context isolation reduces implementation anchoring but is not a substitute for human judgment or permission to merge.

## Change scope and tiers

Classify scope before using the Cliewen loop. A change is **plain** only when it has no effect on product behavior, intent, executable evidence, decisions, plans, policy, or methodology. `/docs`, `/changes`, product code, tests, configuration, build and release machinery, security and governance policy, AGENTS.md rules, skills, and lint rules are protected and never plain. Changes to commands, contracts, user workflow, or normative instructions are not editorial. Uncertainty is not plain.

A plain change stays outside this skill: use an ordinary branch from the current tip of `main`, run checks relevant to the changed surface, open a ready PR, and leave merge to a human. Do not assign a CH identity, read the corpus, declare a plan item, create proposal artifacts, run Cliewen verification, update plan bookkeeping, or add a Cliewen-mandated changelog entry. Plain changes do not consume the one-Cliewen-change-in-flight slot and never build on unmerged work.

A change is light only when all of these hold: no decision is made, no acceptance criterion or capability meaning changes, no semantic plan mutation occurs, and no methodology carrier such as a skill, AGENTS.md rule, or lint rule is touched. Typical light changes: typos or documentation clarity on a protected Cliewen surface, dependency bumps, pure refactors, and CI plumbing. A light change skips the transient workspace; its branch and ready PR remain mandatory, and the PR description is the proposal with a real plan item or an explicit plan-less declaration.

Every other change uses the full loop and a `/changes/CH-xxx-slug/` workspace. Escalate immediately if a decision, open question, meaning change, or methodology-carrier edit appears during work.

## Decision records

Route every decision by reversal cost. A cheap-and-local-to-reverse decision is a dated row in `docs/decisions/log.md` (columns `Date | Decision | Why | Change/PR`); otherwise write an ADR for software or corpus architecture, or a PDR for how the project works. A decision adopting a well-established practice cites it by name and records only the local why.

Agent-authored decisions start `status: inferred` and `author: agent`. Merging makes them binding without changing that status. Only explicit human approval promotes a decision to `verified`; record every approver in `accepted-by:`, use the first approval date, and cite the venue. An explicit objection keeps the decision `inferred` and becomes an open question.

Every decision record is timeless: state what is decided and only the enduring context and rationale needed to understand it. Keep triggering incidents, chronology, conversations, implementation details, and review history in findings, the change workspace, the PR, and Git history.

## Repository-local conventions

For a Cliewen change, apply the repository-local conventions declared in AGENTS.md, including digest requirements such as a user-facing changelog entry. Plain changes follow only the repository conventions that apply to their changed surface. Local conventions extend the methodology and never override it. If AGENTS.md conflicts with a skill, record the conflict in `open-questions.md` and stop for a human decision; never choose silently.

## Review boundary

Every change branches from the current tip of `main`, never from unaccepted work. Each author takes one Cliewen change to its PR before starting another; independent authors may work in parallel from `main`, and plain changes do not consume this slot. If work must build on an unmerged change, record a blocking open question and stop unless the human explicitly authorizes it. If another change merges first, rebase onto the new `main` tip and repeat verification.

Open the PR ready for review only after local verification and the automatic agentic review loop pass on the current commit, never as a draft. The PR is the completed proposal's authorization and protected-integration boundary, not a demand for duplicate human code review: the agent may publish the candidate, but only a human-controlled PR merge accepts it. Unfinished work stays on the branch. An agent never merges its own PR, creates a local merge commit into `main`, or pushes to `main`.

A PR alone displays hosted CI but does not enforce it. Where hosting supports enforcement, the PR triggers CI, branch protection makes its required status check a merge precondition, and the agent cannot silently skip the gate. Never weaken the workflow or required-check policy to make a change pass.

Ready means the hosted PR contains the exact locally reviewed and verified state. Before reporting any change ready, commit every intended edit, run the applicable local verification and a clean agentic review pass against that commit, require `git status --porcelain` to be empty, push the reviewed commit, and confirm that the ready hosted PR's head branch and SHA equal the current local branch and `HEAD`. Perform the hosted check immediately after opening or updating the PR; if either side differs, reconcile without silently overwriting remote work, rerun verification and review on the resulting commit, require a clean worktree, push the reviewed commit, and check again. A human-requested local stopping point such as "commit only" is preserved work, not a completed or mergeable change, and the agent says that no ready PR exists.

After opening and confirming the PR, an agent stops and waits on further Cliewen work; independent plain changes may still proceed from accepted `main`. Review fixes stay on the same branch and PR: commit them, rerun local verification and the automatic agentic review loop against that commit, require a clean worktree, push the reviewed commit there, and repeat the hosted-head check before reporting ready again. A follow-up Cliewen change exists only when a human has accepted this one and explicitly scoped the follow-up.

After a human reports the merge, orient before starting anything else: describe the plan's next unfinished step in plain language and ask whether to start it, or say that the plan has nothing left and ask what comes next.
