# Tank Royale AI Agent Instructions

Routing hub — load `.ai/*.md` files based on task type below.

## Quick Routing

| Task Type | Load These |
|-----------|-----------|
| **Planning, proposals, specs** | `.ai/openspec.md` → `/openspec/AGENTS.md` ⚠️ |
| **Architecture decisions, ADRs** | `.ai/architecture.md` |
| **Bot API (Java/Python/.NET)** | `.ai/cross-platform.md` + `.ai/core-principles.md` |
| **Protocol, WebSocket, server comms** | `schema/schemas/README.md` (sequence diagrams + message schemas) |
| **Testing, builds, Gradle** | `.ai/testing-and-build.md` |
| **Documentation, README, Javadoc** | `.ai/documentation.md` |
| **Code style, naming, conventions** | `.ai/coding-conventions.md` |
| **File encoding, UTF-8, standards** | `.ai/standards.md` |
| **General coding task** | `.ai/core-principles.md` (default) |

**Full navigation & maintenance:** `.ai/README.md` · `.ai/MAINTENANCE.md`
