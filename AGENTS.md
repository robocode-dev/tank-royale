# Tank Royale AI Agent Instructions

Routing hub — load `.ai/*.md` files based on task type below.

## Quick Routing

| Task Type | Load These |
|-----------|-----------|
| **Planning, proposals, specs** | `.ai/openspec.md` ⚠️ **STOP after proposal — wait for human approval before implementing** |
| **Audit, Scout, Prime** | `.ai/commands.md` |
| **Architecture decisions, ADRs** | `.ai/architecture.md` |
| **Debugging, bug hunting, timing issues** | `.ai/debugging.md` |
| **Bot API (Java/Python/.NET)** | `.ai/cross-platform.md` + `.ai/core-principles.md` |
| **Protocol, WebSocket, server comms** | `schema/schemas/README.md` (sequence diagrams + message schemas) |
| **Testing, builds, Gradle** | `.ai/testing-and-build.md` |
| **Documentation, README, Javadoc** | `.ai/documentation.md` |
| **Changelog, release notes** | `.ai/changelog.md` |
| **Code style, naming, conventions** | `.ai/coding-conventions.md` |
| **File encoding, UTF-8, standards** | `.ai/standards.md` |
| **General coding task** | `.ai/core-principles.md` (default) |

**Full navigation & maintenance:** `.ai/README.md` · `.ai/MAINTENANCE.md`

---

## Critical Workflow Rules

### Git Commits: Review-First Policy

**Rule:** Never commit changes automatically. Always wait for user review.

**Workflow:**
1. Make code changes to files
2. Show what changed (via `git diff` or describe changes)
3. Wait for user review and approval
4. Only commit when user explicitly requests it
5. Include user-approved message in commit

**Applies to:**
- All code changes
- All documentation changes  
- All configuration changes
- Every file modification

**Why:** User must review all changes before they're locked in. Automatic commits prevent code review and make it impossible for users to request modifications before commit.
