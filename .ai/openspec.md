# OpenSpec Workflow

<!-- KEYWORDS: planning, proposal, spec, change, RFC, OpenSpec -->

## When to Load

Load when task involves: planning, proposals, new capabilities, breaking changes, or keywords "spec", "RFC", "plan".

**Always open `/openspec/AGENTS.md` before coding for these tasks.**

## Key Locations

- Instructions: `/openspec/AGENTS.md`
- Project specs: `/openspec/project.md`
- Change specs: `/openspec/specs/`
- Change history: `/openspec/changes/`

## Workflow

1. User mentions planning/proposal keywords
2. Load `/openspec/AGENTS.md`
3. Follow OpenSpec process for formal proposals
4. **STOP after creating the proposal — wait for explicit human approval before implementing**
5. Apply changes only after the user has reviewed and approved the proposal

## ⛔ Approval Gate

**Never start implementing a change proposal without explicit human approval.**

After scaffolding `proposal.md`, `tasks.md`, and spec deltas:
- Present the proposal to the user
- Wait for a clear "yes", "approved", "go ahead", or equivalent confirmation
- Only then move to Stage 2 (implementation)

This applies even when the user phrases the request as "plan and implement" — create the proposal first, present it, and wait.

## Important

`<!-- OPENSPEC:START -->` / `<!-- OPENSPEC:END -->` blocks are managed by OpenSpec tooling — do not edit content within them.
