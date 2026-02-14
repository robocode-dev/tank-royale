# OpenSpec Workflow

## When to Load This

Load when task involves:
- Planning or proposals
- New capabilities or breaking changes
- Architecture shifts or big performance/security work
- Ambiguous requirements needing formal spec
- Keywords: "proposal", "spec", "change", "plan", "RFC"

## OpenSpec Integration

This project uses **OpenSpec** (https://github.com/Fission-AI/OpenSpec) for formal change proposals.

When the user mentions planning, proposals, or significant architecture changes, **always open `@/openspec/AGENTS.md`** before coding.

### Learn About OpenSpec

Read `/openspec/AGENTS.md` to understand:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

### Key Locations

- **OpenSpec instructions:** `/openspec/AGENTS.md`
- **Project specs:** `/openspec/project.md`
- **Change specs:** `/openspec/specs/`
- **Change history:** `/openspec/changes/`

## Workflow

1. **User mentions planning/proposal keywords**
2. **Load** `/openspec/AGENTS.md` (⚠️ not this file)
3. **Follow OpenSpec process** for formal proposals
4. **Apply changes** through approved spec workflow

## Important

The `<!-- OPENSPEC:START -->` / `<!-- OPENSPEC:END -->` block is **managed by OpenSpec** for auto-updates.

If you see it in `.ai/` files or AGENTS.md, keep it untouched and do not edit content within it.

