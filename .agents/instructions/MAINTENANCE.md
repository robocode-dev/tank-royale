# AI Instructions Maintenance Guide

## File Structure

```
/AGENTS.md                              ← Router only (<20 lines)
/.agents/instructions/
  ├─ README.md                          ← Metadata table + AI Learning Loop
  ├─ MAINTENANCE.md                     ← This file
  ├─ core-principles.md                 ← Governance, git workflow, clean code
  ├─ cross-platform.md                  ← Bot API cross-platform rules
  ├─ coding-conventions.md              ← Language-specific style
  ├─ testing-and-build.md               ← Gradle, tests, validation
  ├─ documentation.md                   ← Docs, CHANGELOG.md, API docs
  ├─ standards.md                       ← UTF-8, encoding, repo boundaries
  ├─ architecture.md                    ← ADRs, C4, design decisions
  └─ openspec.md                        ← Routes to /openspec/AGENTS.md
```

## When to Add a New File

Create `.agents/instructions/*.md` when: new topic area, 50+ lines of reusable content, doesn't fit existing files.

## Adding New Instructions

1. Create `/.agents/instructions/your-topic.md` (50–150 lines; start with `<!-- KEYWORDS: ... -->`)
2. Add row to `.agents/instructions/README.md` metadata table
3. Add row to `AGENTS.md` quick routing table

## Rules

- `AGENTS.md`: routing only, stay under 20 lines
- Each `.agents/instructions/*.md`: 50–150 lines
- No OpenSpec blocks in `.agents/instructions/` files (reference via `openspec.md`)
- No duplicate content across files (DRY)
- Update `README.md` metadata when adding/removing files

## Checklist

- [ ] File is 50–150 lines
- [ ] `.agents/instructions/README.md` metadata table updated
- [ ] `AGENTS.md` routing table updated
- [ ] No content duplicated from other `.agents/instructions/` files
- [ ] Keywords are specific and searchable
