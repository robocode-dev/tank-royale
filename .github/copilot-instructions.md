# Copilot Instructions

**FIRST ACTION:** Follow this sequence before starting any task:

1. Read `/AGENTS.md` (routing hub: system-of-record, change loop, task routing)
2. Read `/docs/README.md` (the corpus — the permanent truth about the system)
3. Based on task keywords, load relevant `/.agents/instructions/*.md` files (index: `/.agents/instructions/README.md`)
4. Start the task

The corpus under `/docs/` is the system-of-record (Cliewen conventions); a full change runs the change loop via the `clue-delta` skill and reaches `main` only through a human-merged PR, while simple work reaches `main` directly only under the maintainer's explicit per-change push authorization, recorded in an `Authorized-Push:` trailer (constraint [C-002](/docs/constraints/C-002-review-boundary.md)). Repo-local conventions (testing, style, encoding, debugging) live in `/.agents/instructions/` and are routed through `/AGENTS.md`.

> **Principles are loaded on demand.** Run `/dot-prime` before working on a file to activate the relevant `.principles` for that path.
