# OpenSpec Workflow

<!-- KEYWORDS: planning, proposal, spec, change, RFC, OpenSpec, /opsx, onboard, apply, explore, archive, propose -->

## When to Load

Load when task involves: planning, proposals, new capabilities, breaking changes, or keywords "spec", "RFC", "plan".

**Always open `/openspec/AGENTS.md` before coding for these tasks.**

## Key Locations

- Instructions: `/openspec/AGENTS.md`
- Project specs: `/openspec/project.md`
- Change specs: `/openspec/specs/`
- Change history: `/openspec/changes/`

## OpenSpec Commands (/opsx)

| Command | What it does |
|---------|--------------|
| `/opsx:propose` | Create a change and generate all artifacts (proposal, specs, design, tasks) |
| `/opsx:explore` | Think through problems before or during work (no code changes) |
| `/opsx:apply`   | Implement tasks from a change (loop through checkboxes until done) |
| `/opsx:archive` | Archive a completed change to `openspec/changes/archive/` |
| `/opsx:new`     | Start a new change, step-by-step artifact creation |
| `/opsx:continue`| Resume artifact creation or implementation on an existing change |
| `/opsx:ff`      | Fast-forward: create all artifacts at once without steps |
| `/opsx:verify`  | Verify implementation matches current artifacts |
| `/opsx:onboard` | Guided tutorial through a complete OpenSpec cycle |

## Workflow

1. **User mentions planning/proposal keywords**
2. **Load relevant instructions**
   - Open `/openspec/AGENTS.md` (if exists) or follow `.agents/instructions/openspec.md`.
3. **Follow OpenSpec process for formal proposals**
4. **STOP after creating the proposal — wait for explicit human approval before implementing**
5. **Apply changes only after the user has reviewed and approved the proposal**

## Skills

### openspec-apply-change
Implement tasks from an OpenSpec change.
1. **Select Change**: Infer name from context or run `openspec list --json`.
2. **Read Artifacts**: Read proposal, specs, design, and tasks.
3. **Loop Tasks**: Working through pending tasks one by one, making code changes and checking off checkboxes.
4. **Pause/Resume**: Stop on ambiguity or error, wait for guidance, then resume.

### openspec-onboard
Guided onboarding tutorial.
- Scan codebase for small improvement tasks (TODOs, missing tests, etc.).
- Guide user through: Explore → New → Proposal → Specs → Design → Tasks → Apply → Archive.
- Narrate each step and explain the rationale.

## ⛔ Approval Gate

**Never start implementing a change proposal without explicit human approval of the Proposal, Design, and Specs.**

After scaffolding `proposal.md`, `design.md`, and `specs/`:
- Present the proposal, design, and specs to the user.
- Wait for a clear "yes", "approved", "go ahead", or equivalent confirmation for the entire plan and design.
- Only then move to Stage 2 (Implementation Tasks and Coding).

This applies even when the user phrases the request as "plan and implement" — create the proposal, design, and specs first, present them, and wait.

## Important

`<!-- OPENSPEC:START -->` / `<!-- OPENSPEC:END -->` blocks are managed by OpenSpec tooling — do not edit content within them.
