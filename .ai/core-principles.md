# Core Principles

<!-- KEYWORDS: clean code, principles, minimal changes, atomic commits, SRP, DRY, YAGNI, governance -->

## Project Governance

- Open source (Apache 2.0), solo maintainer: Flemming N. Larsen
- Distributed via: GitHub Releases · Maven Central (Java) · NuGet (.NET) · PyPI (Python)
- No enterprise processes — spare-time project; manual GUI smoke testing is acceptable
- Users report bugs via GitHub issues

## Clean Code Philosophy

- Meaningful names, small focused functions, single responsibility
- Implement ONLY what's required — no drive-by refactors
- Keep diffs focused and reviewable; question scope creep
- Favor: composition over inheritance, pure functions, early returns, named constants
- Avoid: magic numbers, premature optimization, tight coupling, clever abstractions without clear benefit
- Follow SOLID, DRY, YAGNI

## Git Workflow (Critical)

- ✅ Create/edit files, stage changes, create branches when asked
- ❌ **NEVER `git commit` or `git push` without explicit user approval**
- ⚠️ Always ask: "Ready to commit these changes?"

## Communication Standards

- One-sentence response after completing a task — no summaries or rationale
- **NEVER create summary/documentation files** (`*_Fix.md`, `*_Summary.md`, etc.) unless asked
- Temporary scripts must be cleaned up after the task; ask if they should be kept permanently
- Brief explanation format: "Changed X in file Y because Z"
- **Start responses with "AI Guidelines loaded."** to confirm you've read the instructions

## AI Agent Boundaries

- Never make git commits directly — suggest messages, leave execution to the human
- **Never start implementing tasks.md after creating an OpenSpec proposal** — wait for human to branch and assign model
- **Never start fixing or implementing anything without asking the user first** — always confirm scope and approach before touching code, config, or docs
