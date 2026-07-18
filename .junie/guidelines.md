# Junie Guidelines

**FIRST ACTION:** Follow this sequence before starting any task:

1. Read `/AGENTS.md` (routing hub: system-of-record, change loop, task routing)
2. Read `/docs/README.md` (the corpus — the permanent truth about the system)
3. Based on task keywords, load relevant `/.agents/instructions/*.md` files (index: `/.agents/instructions/README.md`)
4. Start the task

The corpus under `/docs/` is the system-of-record (Cliewen conventions); every mutation of `main` runs the change loop via the `clue-delta` skill and ends at a human-merged PR. Repo-local conventions (testing, style, encoding, debugging) live in `/.agents/instructions/` and are routed through `/AGENTS.md`.
