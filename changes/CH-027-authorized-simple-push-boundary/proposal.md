---
id: CH-027
type: change
status: open
links: []
title: Allow an explicitly authorized direct push to main for simple work
---

# CH-027 — Allow an explicitly authorized direct push to main for simple work

## What

Amend constraint C-002 so that its absolute prohibition on direct commits and pushes to `main` binds full Cliewen changes unconditionally, while simple work may be committed and pushed to `main` directly when the maintainer explicitly authorizes that push for that specific change. Update every live carrier that states the old contract, and record the amendment in the decision log.

## Why

C-002 currently says "no direct commits or pushes to `main`, by agents or anyone else". That is stricter than Cliewen's own review boundary, which already separates the two routes: a full change is bound to a branch, a draft PR, and a human merge, whereas simple work "follows explicit user integration authority and repository policy", where "route selection alone never authorizes a push" and "an agent pushes directly to an integration branch only when the user explicitly authorizes it and permissions allow it". Tank Royale adopted the stricter local variant at CH-001 without a decision that weighed the cost.

The cost is real for a solo-maintained repository. Editorial corrections, in-contract configuration adjustments, and other work that leaves the accepted contract unchanged currently pay a full branch-plus-PR-plus-merge cycle whose only reviewer is the person who wrote the change. The protection C-002 exists to give, that no accepted-contract change slips into `main` without a human deciding to accept it, is untouched by this amendment: it is exactly the full route that keeps the absolute prohibition.

The exception is deliberately narrow. It is not a standing permission, it does not follow from the route classification, and it does not exist for full changes at any strength of authorization.

## Scope

- Amend `docs/constraints/C-002-review-boundary.md`: title, body, promotion trigger, and residual.
- Require an authorized direct push to carry an `Authorized-Push:` commit trailer naming the authorization, so the human-enforced half of the constraint leaves a trace in `main`'s history.
- Update the complete live-carrier inventory for the old contract: `AGENTS.md`, `DEVELOPMENT.md`, `.agents/instructions/core-principles.md`, `.github/pull_request_template.md`, and the constraint's index row in `docs/constraints/README.md`.
- Record the amendment as a row in `docs/decisions/log.md`.
- Repair the `CH` counter in `.clue/id-ledger.yaml`, which stood at `1` while the corpus documented CH-001 through CH-026, so `clue id next CH` stopped minting identities that collide with changes already named in durable prose.

## Non-goals

- Relaxing anything about full changes. A full change still branches, opens a draft PR, and waits for a human merge, and an agent still never merges its own PR.
- Turning the exception into a default, a standing grant, or a property an agent may infer from the simple route.
- Enabling branch protection on `main`, or changing CI, the `validate` job, or the required status check.
- Rewriting pinned history that records the old contract: `docs/analysis/AN-001-openspec-extraction.md` and the existing `docs/decisions/log.md` rows stay as written.
- Editing the vendored Cliewen skill mirrors under `.agents/skills/` and `.claude/skills/`, which are generated from upstream canonical sources and already state the two-route boundary this amendment aligns with.

## Decision to be taken

The amendment itself is a corpus-policy decision that is cheap and local to reverse (revert the constraint and its carriers), so it routes to a dated row in `docs/decisions/log.md` rather than an ADR or a new constraint. The constraint file remains the durable statement of the rule.

## Open item for the maintainer

The `Authorized-Push:` trailer is the one element of this change that goes beyond the literal request. Without it, a human-enforced permission leaves no evidence: after the fact nobody can tell an authorized push from an unauthorized one. It is cheap to strike at review if you would rather keep the authorization purely conversational.

Note also that the exception depends on the maintainer retaining push permission to `main`. Branch protection that requires a pull request with no bypass would make the exception unexercisable, which is why the promotion trigger is reworded rather than left as it stands.

## Plan

This change is declared plan-less. It serves no milestone in P-001, P-002, or P-003: it corrects a process rule adopted as a side effect of CH-001, and P-001's remaining milestone (M-002, test purpose tags) is unrelated.
