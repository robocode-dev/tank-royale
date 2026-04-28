# Tank Royale AI Agent Instructions

> ⚠️ **AI Agent:** You are reading this file as part of the startup sequence. Confirm to the user with `✓ Read AGENTS.md.` before proceeding with any task.

Routing hub — load `.agents/instructions/*.md` files based on the task type below.

## Quick Routing

| Task Type | Load These |
|-----------|-----------|
| **Planning, proposals, specs** | `.agents/instructions/openspec.md` ⚠️ **STOP after proposal — wait for human approval before implementing** |
| **Audit, Scout, Prime** | `.agents/instructions/commands.md` |
| **Architecture decisions, ADRs** | `.agents/instructions/architecture.md` |
| **Debugging, bug hunting, timing issues** | `.agents/instructions/debugging.md` + `docs-internal/DEBUGGING-GUIDE.md` |
| **Bot API (Java/Python/.NET)** | `.agents/instructions/cross-platform.md` + `.agents/instructions/core-principles.md` |
| **Protocol, WebSocket, server comms** | `docs-internal/architecture/models/flows/README.md` (sequence diagrams) + `docs-internal/architecture/models/message-schema/README.md` (message schemas) |
| **Testing, builds, Gradle** | `.agents/instructions/testing-and-build.md` |
| **Documentation, README, Javadoc** | `.agents/instructions/documentation.md` |
| **Changelog, release notes** | `.agents/instructions/changelog.md` |
| **Code style, naming, conventions** | `.agents/instructions/coding-conventions.md` |
| **File encoding, UTF-8, standards** | `.agents/instructions/standards.md` |
| **General coding task** | `.agents/instructions/core-principles.md` (default) |

**Full navigation & maintenance:** `.agents/instructions/README.md` · `.agents/instructions/MAINTENANCE.md`

---

## Critical Workflow Rules

### Git Commits: Review-First Policy

**Rule:** Never commit changes automatically. Always wait for user review.

**Workflow:**
1. Make code changes to files
2. Show what changed (via `git diff` or describe changes)
3. Wait for user review and approval
4. Only commit when a user explicitly requests it
5. Include a user-approved message in the commit

**Applies to:**
- All code changes
- All documentation changes
- All configuration changes
- Every file modification

**Why:** User must review all changes before they're locked in. Automatic commits prevent code review and make it impossible for users to request modifications before commit.

---

### Validation Gate (mandatory)

**Rule:** After every file modification, run the project validation command before proceeding to the next step.

**Workflow:**
1. Modify file(s)
2. Run a validation command for the affected module
3. If non-zero exit → STOP, fix all errors, re-run validation
4. Only proceed to the next step when validation exits clean
5. Never mark a task complete while validation is failing

**Validation commands:**
- `./gradlew clean build`

**Why:** Catches discrepancies between spec and implementation immediately, before they compound. Mirrors the static analysis feedback loop that would otherwise require a JetBrains-native agent.
