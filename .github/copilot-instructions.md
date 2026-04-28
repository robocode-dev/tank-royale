# Copilot Instructions

**FIRST ACTION:** Follow this sequence before starting any task:

1. Read `/.agents/instructions/README.md` (metadata index & AI learning loop)
2. Read `/.agents/instructions/MAINTENANCE.md` (how to maintain AI instructions)
3. Read `/AGENTS.md` (routing hub for task-specific files)
4. Based on task keywords, load relevant `/.agents/instructions/*.md` files
5. Start the task

All AI instructions — governance, git workflow, cross-platform rules, conventions, testing, documentation, standards, architecture, and planning — live in `/.agents/instructions/` and are routed through `/AGENTS.md`.

> **Principles are loaded on demand.** Run `/dot-prime` before working on a file to activate the relevant `.principles` for that path.
