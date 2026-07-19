# Core Principles

<!-- KEYWORDS: clean code, principles, minimal changes, SRP, DRY, YAGNI, governance -->

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

## Change Process

The change loop, the review boundary, and all process gates live in the corpus and `AGENTS.md` — not here. Binding rules: [C-002](/docs/constraints/C-002-review-boundary.md) (every mutation of `main` rides a branch and a human-merged PR), [C-001](/docs/constraints/C-001-no-hard-wrapped-markdown.md) (no hard-wrapped markdown prose).

## Communication Standards

- One-sentence response after completing a task — no summaries or rationale
- **NEVER create summary/documentation files** (`*_Fix.md`, `*_Summary.md`, etc.) unless asked
- Temporary scripts must be cleaned up after the task; ask if they should be kept permanently
- Brief explanation format: "Changed X in file Y because Z"
