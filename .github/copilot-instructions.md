# Copilot Instructions

**FIRST ACTION:** Follow this sequence before starting any task:

1. Read `/.ai/README.md` (metadata index & AI learning loop)
2. Read `/.ai/MAINTENANCE.md` (how to maintain AI instructions)
3. Read `/AGENTS.md` (routing hub for task-specific files)
4. Based on task keywords, load relevant `/.ai/*.md` files
5. Start the task

All AI instructions — governance, git workflow, cross-platform rules, conventions, testing, documentation, standards, architecture, and planning — live in `/.ai/` and are routed through `/AGENTS.md`.

<!-- .principles: begin -->
# Code Principles — AI Coding Guidelines

When writing or reviewing code, follow the layered principle system below.

## Layer 1 — Always Active

Non-negotiable fundamentals that apply to every line of code: single responsibility, no duplication, reveal intention, fail fast, validate input, delete dead code.

## Layer 2 — Context-Dependent

Additional principles activated by what you're building. Covers API design, concurrency, domain modeling, testing, cloud-native, and infrastructure patterns.

## Layer 3 — Risk-Elevated

Extra scrutiny for high-risk areas where mistakes are costly or hard to reverse: authentication, financial transactions, personal data (PII), public APIs, performance-critical paths, and distributed systems.
<!-- .principles: end -->
