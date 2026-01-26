<!-- OPENSPEC:START -->

# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:

- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:

- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# Tank Royale AI Agent Instructions

This file serves as the **routing hub** for AI coding assistants. Detailed instructions are modularized in the `.ai/`
directory for token efficiency and focused context loading.

## Quick Reference - What to Load

Load specific instruction files from `.ai/` based on your task:

| **Task/Keywords**                  | **Load These Files**        |
|------------------------------------|-----------------------------|
| General guidelines, clean code     | `.ai/core-principles.md`    |
| Bot API changes, Java/Python/.NET  | `.ai/cross-platform.md`     |
| Java, Python, C# style conventions | `.ai/coding-conventions.md` |
| Testing, building, Gradle          | `.ai/testing-and-build.md`  |
| Docs, VERSIOND.md, Javadoc         | `.ai/documentation.md`      |
| File encoding, standards           | `.ai/standards.md`          |
| **Planning, specs, proposals**     | `@/openspec/AGENTS.md` ⚠️   |

**Default Strategy:** If task is unclear, load `.ai/core-principles.md` + `.ai/cross-platform.md` first.

## AI Learning Loop

**Capture feedback during chat to improve AI instructions permanently.**

When the user gives corrective feedback like:

- "Remember ..." or "Always ..." (preferred)
- "@ai-learn: ..." (explicit trigger)

**Action:** Recognize this as a learning opportunity, propose an edit to the appropriate `.ai/*.md` file, and apply it.

**When in doubt:** Ask "Would you like me to add this to the AI instructions?"

See `.ai/README.md` for target file mapping and best practices.

## Full Index

See `.ai/README.md` for detailed routing guide, file metadata, and token budgets.

---

<!-- 
MAINTENANCE NOTE (for human developers):
When updating agent instructions:
1. Keep this file (AGENTS.md) as lightweight router only
2. Add/modify content in specific `.ai/*.md` topic files
3. Update keyword mappings in table above
4. Run token count check: each .ai/*.md should be 50-150 lines (500-1500 tokens)
5. Keep OpenSpec section (OPENSPEC:START/END) untouched for auto-updates
-->
