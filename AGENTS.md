# Tank Royale AI Agent Instructions

This is the **main routing hub** for AI assistants. Detailed instructions are in modular files for token efficiency.

**👉 Most tasks:** Load specific `.ai/*.md` file(s) based on your task below.  
**👉 Architecture/ADRs:** Load `.ai/architecture.md`  
**👉 Planning/specs:** Load `.ai/openspec.md` then `/openspec/AGENTS.md`

## Quick Routing

| Task Type | Load These |
|-----------|-----------|
| **Planning, proposals, specs** | `.ai/openspec.md` → `/openspec/AGENTS.md` ⚠️ |
| **Architecture decisions, ADRs** | `.ai/architecture.md` |
| **Bot API (Java/Python/.NET)** | `.ai/cross-platform.md` + `.ai/core-principles.md` |
| **Testing, builds, Gradle** | `.ai/testing-and-build.md` |
| **Documentation, README, Javadoc** | `.ai/documentation.md` |
| **Code style, naming, conventions** | `.ai/coding-conventions.md` |
| **File encoding, UTF-8, standards** | `.ai/standards.md` |
| **General coding task** | `.ai/core-principles.md` (default) |

## Full Navigation

See `.ai/README.md` for:
- Detailed file descriptions
- Complete routing decision tree
- Token budgets and file sizes
- AI learning loop process
- Target files for feedback

## Hub Rules (Critical)

**This file is a routing hub. Maintain it as such.**

1. **AGENTS.md is index only** – Routes to specific `.ai/*.md` pages (never add detailed content here)
2. **Stay under 60 lines** – If it grows beyond that, move content to `.ai/`
3. **Update Quick Routing table when:** Adding new major task types (not for every `.ai/` file)
4. **Never embed:** OpenSpec blocks, ADR guidelines, or topic-specific instructions
5. **Reference, don't duplicate:** If content exists in `.ai/README.md`, link to it, don't repeat it
6. **Single purpose:** Route tasks to appropriate `.ai/*.md` files – that's it

**When to modify AGENTS.md:**
- ✅ New major task area (add one row to Quick Routing)
- ✅ Update "Last updated" date
- ✅ Fix links or navigation
- ❌ Don't add new instruction content
- ❌ Don't grow beyond ~60 lines
- ❌ Don't embed managed blocks (OpenSpec, etc.)

**For everything else:** Use/update `.ai/` files or `.ai/MAINTENANCE.md`

## Task Completion Notes

**⚠️ CRITICAL: No summaries after task completion.**

Provide a **one-sentence acknowledgment only**. Examples:

✅ "Updated GameServer.kt to use fixed turn timeout, removed notifyReady() call"  
✅ "Created `.ai/architecture.md` with ADR guidelines"  
❌ "This change improves performance by eliminating unnecessary notifications..."  
❌ Multi-paragraph explanations with background, rationale, or justification

**Why:** Users see the diff. Brief confirmation is all that's needed.

---

**Last updated:** 2026-02-14  
**Maintenance:** When adding instructions, update `.ai/README.md` routing table + metadata
