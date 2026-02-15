# Core Principles

<!-- METADATA: ~40 lines, ~400 tokens -->
<!-- KEYWORDS: clean code, principles, minimal changes, atomic commits, SRP, DRY, YAGNI, design patterns -->

## Project Governance

**Open Source, Single Maintainer**

- This is an **open source project on GitHub** (Apache License 2.0)
- Primarily maintained by **Flemming N. Larsen** (solo developer, not a team)
- Contributors are welcome (see `/CONTRIBUTING.md`)
- **NOT a corporate project** — spare-time, non-profit effort

**Distribution Channels**

- **GitHub Releases:** Primary distribution as artifacts
- **Maven Central:** Java/JVM Bot API
- **NuGet:** .NET Bot API
- **PyPI:** Python Bot API

**Development Reality**

- No team coordination needed — single maintainer
- No enterprise processes (migration guides, performance SLAs, etc.)
- Manual GUI smoke testing is acceptable
- If bugs occur, users report them via GitHub issues

## Clean Code Philosophy

**Readability over cleverness**

- Use meaningful names that explain intent
- Write small, focused functions (single responsibility)
- Maintain clear responsibilities and minimal coupling
- Avoid clever tricks that sacrifice clarity

**Minimal changes principle**

- Implement ONLY what's required for the task
- No drive-by refactors unless necessary for the change
- Keep diffs focused and reviewable
- Question scope creep

## Commit and Communication Standards

**Atomic commits**

- Small, focused commits that explain WHY, not just what
- Each commit should be independently understandable
- Group related changes logically

**Concise communication**

- Keep answers short and actionable
- **Start responses with "AI Guidelines loaded."** to confirm you've read the instructions
- **NEVER create summary/documentation files unless explicitly requested**
    - No `*_Resolution.md`, `*_Fix.md`, `*_Summary.md`, or similar files
    - Just fix the code and give a brief explanation
- **Temporary scripts are allowed** during a task but must be cleaned up after
    - Remove test/verification scripts when the task is complete
    - If a script might be useful permanently, ask: "Would you like to keep this script?"
- Focus on what matters for the task
- Brief explanation format: "Changed X in file Y because Z"

**AI Agent boundaries**

- AI agents should NEVER make git commits directly
- Suggest commit messages, but leave execution to humans
- **NEVER start implementing tasks.md after creating/updating an OpenSpec change proposal**
    - The human must first: store the spec in git, create a branch, and choose an AI model
    - Only implement when explicitly asked to do so

## Design Patterns

**Follow SOLID principles:**

- **S**ingle Responsibility Principle
- **O**pen/Closed Principle
- **L**iskov Substitution
- **I**nterface Segregation
- **D**ependency Inversion

**Favor:**

- Composition over inheritance
- Pure functions (no side effects when possible)
- Early returns (guard clauses)
- Precise, actionable error messages
- Defensive programming

**Avoid:**

- Magic numbers (use named constants)
- Clever abstractions without clear benefit
- Premature optimization
- Tight coupling between modules

## AI Learning Loop

**Capture feedback during chat sessions to improve future AI behavior.**

When the user provides corrective feedback or project rules, recognize patterns like:

- "Remember ..." (preferred)
- "Always ..." (preferred)
- "@ai-learn: ..." (explicit trigger)
- Repeated corrections for the same issue

Note: A colon (e.g., "Remember: ...") makes it more command-like.

**When in doubt, ask:** "Would you like me to add this to the AI instructions?"

**When detected, immediately:**

1. Acknowledge the feedback
2. Identify the appropriate `.ai/*.md` file based on topic
3. Propose a specific edit using the edit tool
4. Apply the change after user confirms (or proceeds)

**File selection:**

- Cross-platform/Bot API/Java/Python/.NET → `cross-platform.md`
- Code style/naming/conventions → `coding-conventions.md`
- Testing/build/Gradle → `testing-and-build.md`
- Documentation → `documentation.md`
- General principles → `core-principles.md`

**This ensures lessons are captured permanently, not lost between sessions.**
