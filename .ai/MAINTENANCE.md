# AI Instructions Maintenance Guide

This guide is for developers/project maintainers who need to add or update AI coding instructions.

## Architecture

```
/AGENTS.md                    ← Main routing index (KEEP SHORT, <50 lines)
/.ai/
  ├─ README.md               ← Navigation hub, token budgets, routing tree
  ├─ core-principles.md
  ├─ cross-platform.md
  ├─ coding-conventions.md
  ├─ testing-and-build.md
  ├─ documentation.md
  ├─ standards.md
  ├─ architecture.md     ← New: ADR guidelines (moved from AGENTS.md)
  └─ openspec.md             ← New: OpenSpec integration (moved from AGENTS.md)
/openspec/
  ├─ AGENTS.md              ← Formal proposal/spec instructions (separate)
  ├─ project.md
  └─ ...
```

## When to Add New Instructions

### ✅ DO: Create a new `.ai/*.md` file when:
- You're adding instructions for a **new topic area**
- Instructions are **general/reusable** across multiple tasks
- Content is **50-150 lines** (500-1500 tokens)
- Topic doesn't fit existing files

### ❌ DON'T: Add to AGENTS.md when:
- It's topic-specific (goes in `.ai/` instead)
- It makes AGENTS.md exceed ~50 lines
- It repeats what's in other files

## Step-by-Step: Adding New Instructions

### 1. Create the File

```bash
# Create /.ai/your-topic.md
# Keep it concise: 50-150 lines, ~500-1500 tokens
# Start with "## When to Load This" section
```

Example structure:
```markdown
# Topic Name

## When to Load This

Load when task involves:
- Keyword 1, keyword 2, keyword 3
- Links to real-world examples

## Content

... your detailed instructions ...
```

### 2. Update `.ai/README.md`

Add entries in three places:

**a) File Metadata table:**
```markdown
| `your-topic.md` | ~50 | ~500 | keyword1, keyword2, keyword3 |
```

**b) Routing Decision Tree:**
```markdown
├─ Keyword phrase? → Load `your-topic.md`
```

**c) Target Files (AI Learning Loop):**
```markdown
| Your topic area | `your-topic.md` |
```

**d) File Descriptions (end of file):**
```markdown
### `your-topic.md`

One-line description: What this file covers.
```

### 3. Update `/AGENTS.md` Routing Table

Add one row to the "Quick Routing" table:
```markdown
| **Your task type** | `.ai/your-topic.md` |
```

### 4. Validate

- [ ] File is 50-150 lines
- [ ] Token estimate is accurate (rough: ~10 tokens per line)
- [ ] All three README.md tables updated
- [ ] AGENTS.md routing table updated
- [ ] Keywords are specific and searchable

## Special Cases

### OpenSpec Integration

**Never embed OpenSpec blocks in `.ai/*.md` files.**

The `<!-- OPENSPEC:START/END -->` markers are managed by OpenSpec tooling and auto-updated. If this integration existed:
- It lives **only in `/openspec/AGENTS.md`**
- `.ai/openspec.md` references it and explains when to load `/openspec/AGENTS.md`
- AGENTS.md doesn't contain the block

See `.ai/openspec.md` for how to route to formal proposals.

### ADR Guidelines

ADR instructions moved to `.ai/architecture.md` so they don't clutter the main router.

- **Update ADR rules?** Edit `.ai/architecture.md`
- **Need ADR format examples?** Link to `docs-internal/architecture/adr/`
- **ADRs too long?** Add trim guidance in `architecture.md`

## Files That Stay Elsewhere

These are **not in `.ai/`** for good reasons:

| File | Why |
|------|-----|
| `/AGENTS.md` | Main router; deliberately short to stay focused |
| `/openspec/AGENTS.md` | Formal proposal workflow; separate from coding instructions |
| `.ai/README.md` | Navigation hub; central routing logic |

## Token Budget Rules

- **AGENTS.md:** ~50 lines max (this is just a router)
- **Each `.ai/*.md`:** ~50-150 lines (500-1500 tokens)
- **Total all `.ai/` files:** ~300-400 lines
- **Load strategy:** Agent picks only relevant files based on keywords

The goal: Keep token budgets predictable, avoid loading everything.

## Maintenance Checklist

When updating instructions:

- [ ] Changes go in appropriate `.ai/*.md` file, **not AGENTS.md**
- [ ] AGENTS.md stays <50 lines (pure router/index)
- [ ] Updated `.ai/README.md` metadata table
- [ ] Updated `.ai/README.md` routing decision tree
- [ ] Updated `.ai/README.md` AI learning loop target files
- [ ] Updated `.ai/README.md` file descriptions
- [ ] Updated AGENTS.md quick routing table if new topic
- [ ] File is 50-150 lines (validate token efficiency)
- [ ] Keywords in metadata are searchable and specific
- [ ] No embedded OpenSpec blocks (use `.ai/openspec.md` instead)

## Anti-Patterns to Avoid

❌ **Don't:** Add topic-specific content to AGENTS.md  
❌ **Don't:** Put OpenSpec blocks in `.ai/` files (use `.ai/openspec.md` reference)  
❌ **Don't:** Create `.ai/` files for one-off, non-reusable instructions  
❌ **Don't:** Let `.ai/` files grow beyond 150 lines (split or trim)  
❌ **Don't:** Forget to update README.md metadata when adding files  
❌ **Don't:** Add "How to Update AI Instructions" to AGENTS.md (you're reading it)

## Questions?

- **Where do ADR guidelines go?** `.ai/architecture.md`
- **Where do planning instructions go?** `.ai/openspec.md` (which redirects to `/openspec/AGENTS.md`)
- **How long should a `.ai/` file be?** 50-150 lines; ~10 tokens per line
- **When should I create a new `.ai/` file?** When you have 50+ lines of focused, reusable content
- **Can AGENTS.md have sections?** Only routing/quick reference; detailed content goes in `.ai/`


