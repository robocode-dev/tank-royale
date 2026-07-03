# Tank Royale — AI Agent Instructions

## Instructions

Load `.agents/instructions/core-principles.md` plus the file(s) matching your task type before
starting work.

| Task | File(s) |
|------|---------|
| Planning, proposals, specs | `.agents/instructions/openspec.md` — stop after proposal; wait for approval |
| Architecture, ADRs | `.agents/instructions/architecture.md` |
| Debugging, timing, race conditions | `.agents/instructions/debugging.md` · `docs/DEBUGGING-GUIDE.md` |
| Bot API (Java / Python / .NET) | `.agents/instructions/cross-platform.md` · `.agents/instructions/core-principles.md` |
| Protocol, WebSocket, server comms | `docs/architecture/models/flows/README.md` · `docs/architecture/models/message-schema/README.md` |
| Testing, builds, Gradle | `.agents/instructions/testing-and-build.md` |
| Documentation, README, Javadoc | `.agents/instructions/documentation.md` |
| Changelog, release notes | `.agents/instructions/changelog.md` |
| Code style, naming, conventions | `.agents/instructions/coding-conventions.md` |
| File encoding, UTF-8 | `.agents/instructions/standards.md` |
| General coding (default) | `.agents/instructions/core-principles.md` |

## Skills

When the user runs a slash command, read the skill file and follow it exactly.

| Command | Skill file |
|---------|-----------|
| `/dot-scout [path]` | `.agents/skills/dot-scout/SKILL.md` |
| `/dot-prime [target]` | `.agents/skills/dot-prime/SKILL.md` |
| `/dot-audit [target]` | `.agents/skills/dot-audit/SKILL.md` |
| `/release` | `.agents/skills/release/SKILL.md` |
| `/update-deps` | `.agents/skills/update-deps/SKILL.md` |
| `/deploy-sample-bots [dir]` | `.agents/skills/deploy-sample-bots/SKILL.md` |
| `/structurizr` | `.agents/skills/structurizr/SKILL.md` |
