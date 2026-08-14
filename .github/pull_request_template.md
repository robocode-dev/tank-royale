## Acceptance brief

<!-- REQUIRED: Replace this comment with a concise, human-facing brief before requesting merge. -->

- Plan item and whether it remains wanted: <!-- REQUIRED -->
- Added or changed criteria, verbatim scenarios, and advisory scenario-resolution verdicts (`verifies`, `verifies-something-adjacent`, or `undetermined`); a criterion newly or materially declaring `Test-type: Human` is named here — this line is that criterion's proof, not a separate confirmation: <!-- REQUIRED, or none -->
- What becomes binding on merge (inferred decisions, invalidated or superseded records): <!-- REQUIRED, or none -->

> A green build, complete form, or confident agent is not evidence that this outcome is wanted or that the criteria reflect reality. Keep this brief to one screen; split a change rather than hiding material meaning.

## Summary

<!-- What changes, and why is it needed? -->

## Verification

<!-- List the checks relevant to the changed surface and their results. -->

## Cliewen proposal

<!-- Delete this section and the Cliewen checklist for a plain change. Plain means no effect on behavior, intent, evidence, decisions, plans, policy, commands, contracts, user workflow, or methodology. -->

- Change ID: `CH-xxx`
- Change tier: <!-- full or light -->
- Plan item served: <!-- P-xxx / M-xxx, or explicitly plan-less -->
- Proposal location: <!-- /changes/CH-xxx-slug/proposal.md for full changes; this PR description for light changes -->
- Agentic review mode and reviewed commit: <!-- context-isolated or in-context fallback; SHA -->
- Hosted head reviewed before this update: <!-- SHA, or new PR -->
- Outstanding actionable findings: <!-- unresolved review-conversation links, or none -->

## Traceability and Decisions

- Acceptance criteria or capability meaning changed: <!-- links, or none -->
- Decision records added or changed: <!-- links, or none -->
- Constraints assessed (including verifiable quality bars): <!-- links and effects, or none -->

## Cliewen checklist

- [ ] This is the initiating author's only initiated Cliewen change; review or update help on an existing PR does not consume another slot.
- [ ] The proposal was committed before implementation, or this is a correctly scoped light change whose proposal is this PR description.
- [ ] The plan item or plan-less declaration is truthful, and all artifact links resolve.
- [ ] Decisions are recorded at the right tier, and active constraints were assessed.
- [ ] Changed active acceptance criteria have positive and negative executable evidence.
- [ ] User-visible impact is described under `[Unreleased]` in `CHANGELOG.md`, or the change has no user-visible impact.
- [ ] Full-change tasks are complete, plan bookkeeping is current, and no transient `/changes/` workspace remains.
- [ ] Generated artifacts were regenerated from their canonical sources where applicable.
- [ ] The current commit received a clean agentic review pass, and every substantive fix after an earlier pass triggered a new review.
- [ ] Reviews of an existing PR name its hosted head, and actionable findings are unresolved hosted conversations until their reviewed fixes are published.
- [ ] `go build ./...`, coverage-gated `go test ./...`, `go run ./cmd/clue validate --forbid-changes`, and `git diff --check` pass.

## Review boundary

- [ ] The branch started from the current tip of `main` and does not build on unmerged work.
- [ ] Every intended edit is committed, the worktree is clean, and the reported verification ran against the current commit.
- [ ] The branch was updated without force, and this ready pull request's head branch and SHA equal the locally verified branch and `HEAD`.
- [ ] Satisfied review conversations were resolved only after their reviewed fixes reached this hosted head.
- [ ] The pull request is ready for human review and merge; no agent will merge it, and no part of this change reached `main` outside this pull request.
