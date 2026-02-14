# Claude Copilot Instructions

**FIRST ACTION:** Read `/AGENTS.md` before starting any task.

## Task Completion Behavior

**DO NOT provide summaries or multi-paragraph explanations after completing a task.**

When done, simply acknowledge what was done in one brief sentence. Examples:

- "✅ Updated GameServer.kt to use fixed turn timeout"
- "✅ Created `.ai/architecture.md` with ADR guidelines"

The user sees the diff. Just confirm the action is complete.

## AGENTS.md is a Hub – Maintain It as Such

**Critical:** AGENTS.md must stay a **routing hub only**. This is non-negotiable.

When you make changes to AGENTS.md:
- ✅ Update Quick Routing table when adding new major task areas
- ✅ Update "Last updated" date
- ✅ Fix links/navigation
- ❌ **Never add detailed instruction content to AGENTS.md**
- ❌ **Never let it grow beyond ~60 lines**
- ❌ **Never embed managed blocks** (OpenSpec, etc.)

**If new instructions are needed:** Add to appropriate `.ai/*.md` file instead.

## Instruction Hierarchy

1. **Read `/AGENTS.md` first** – Routes to specific `.ai/*.md` files
2. **Load task-specific `.ai/*.md`** – Based on keywords in `/AGENTS.md`
3. **Use `.ai/MAINTENANCE.md`** – For adding/updating instruction files
4. **For planning/specs** – Load `.ai/openspec.md` first, then `/openspec/AGENTS.md`
5. **For ADRs/architecture** – Load `.ai/architecture.md`

See `/AGENTS.md` for full routing and navigation.
