# AI Instructions Maintenance Guide

## File Structure

```
/AGENTS.md                              ← Routing hub: critical rules, routes, skills, repo-local conventions
/.agents/instructions/
  ├─ README.md                          ← Metadata table + AI Learning Loop
  ├─ MAINTENANCE.md                     ← This file
  ├─ core-principles.md                 ← Governance, clean code, communication
  ├─ cross-platform.md                  ← Bot API cross-platform rules
  ├─ coding-conventions.md              ← Language-specific style
  ├─ testing-and-build.md               ← Gradle, tests, validation
  ├─ documentation.md                   ← Docs, README, API docs, diagrams
  ├─ changelog.md                       ← CHANGELOG.md, release notes
  ├─ standards.md                       ← UTF-8, encoding, repo boundaries
  ├─ architecture.md                    ← ADRs, C4, design decisions
  └─ debugging.md                       ← Bug hunting, timing, race conditions
```

Process (change loop, proposals, review boundary) lives in the corpus (`docs/README.md`) and the `clue-*` skills — never in `.agents/instructions/`.

## When to Add a New File

Create `.agents/instructions/*.md` when: new topic area, 50+ lines of reusable content, doesn't fit existing files.

## Adding New Instructions

1. Create `/.agents/instructions/your-topic.md` (50–150 lines; start with `<!-- KEYWORDS: ... -->`)
2. Add row to `.agents/instructions/README.md` metadata table
3. Add row to the task-specific instructions table under repo-local conventions in `AGENTS.md`

## Rules

- `AGENTS.md`: routing hub only. It follows the hub that `clue init` generates (critical rules, routes, skills) and keeps this repository's own layer under "Repo-local conventions"; no topic content
- Each `.agents/instructions/*.md`: 50–150 lines
- No change-process rules in `.agents/instructions/` files (the corpus and `clue-*` skills own process)
- No duplicate content across files (DRY)
- Update `README.md` metadata when adding/removing files

## Checklist

- [ ] File is 50–150 lines
- [ ] `.agents/instructions/README.md` metadata table updated
- [ ] `AGENTS.md` task-specific instructions table updated
- [ ] No content duplicated from other `.agents/instructions/` files
- [ ] Keywords are specific and searchable
