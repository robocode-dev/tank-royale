# Tank Royale — AI Agent Instructions

## System of record

**Start at [`docs/README.md`](docs/README.md)** — the corpus is the permanent truth about this system (Cliewen conventions since CH-001; see [AN-001](docs/analysis/AN-001-openspec-extraction.md)). Requirements live as Gherkin acceptance criteria under `docs/capabilities/*/criteria.md`; every mutation of `main` runs the change loop (`clue-delta` skill): branch → `/changes/CH-xxx-slug/` proposal → implement → digest → PR. A **light** change — one that decides nothing, changes no acceptance criterion or capability meaning, mutates no plan semantically, and touches no methodology carrier — skips the workspace: the PR description is the proposal, and the moment a decision or open question appears, escalate to the full loop. `clue validate` judges the corpus; markdown prose is never hard-wrapped (one line per paragraph). Never commit or push to `main` directly; agents never merge their own PRs.

AC IDs are namespaced per capability via `ac-prefix` frontmatter (registry in `docs/README.md`); the corpus is the registry, IDs are meaning-immutable. Test purpose tags (every test carries exactly one AC ID or Unit/Sanity/Arch tag) are being introduced capability by capability — see P-001/M-002; extracted criteria sit at `status: draft` until their tests are wired.

## Repo-local conventions

Load the file(s) matching your task type before starting work:

| Task | File(s) |
|------|---------|
| Debugging, timing, race conditions | `.agents/instructions/debugging.md` · `docs/DEBUGGING-GUIDE.md` |
| Bot API (Java / Python / .NET / TS) | `.agents/instructions/cross-platform.md` |
| Protocol, WebSocket, server comms | `docs/architecture/models/flows/README.md` · `docs/architecture/models/message-schema/README.md` |
| Testing, builds, Gradle | `.agents/instructions/testing-and-build.md` |
| Documentation, README, Javadoc | `.agents/instructions/documentation.md` |
| Changelog, release notes | `.agents/instructions/changelog.md` |
| Code style, naming, conventions | `.agents/instructions/coding-conventions.md` |
| File encoding, UTF-8 | `.agents/instructions/standards.md` |
| Product facts, clean-code baseline | `.agents/instructions/core-principles.md` |

Principles are loaded on demand: run `/dot-prime` before working on a file to activate the relevant `.principles` for that path.

## Skills

Reusable task instructions live in `.agents/skills/`. When the user runs a slash command, read the skill file and follow it exactly.

| Skill | Purpose |
|---|---|
| `clue-delta` | The change loop — use for every mutation of `main` |
| `clue-plan` | Create or revise a plan with verifiable milestones |
| `clue-analysis` | Spikes and findings — every analysis ends in a `docs/analysis` document |
| `clue-verify` | Pre-merge checklist, the human-readable twin of `clue validate` |
| `clue-extract` | Brownfield adoption (already executed here as CH-001) |
| `/dot-scout [path]` · `/dot-prime [target]` · `/dot-audit [target]` | Principles system: discover, activate, audit |
| `/release` · `/update-deps` · `/deploy-sample-bots [dir]` · `/structurizr` | Release, dependency, sample-bot, and diagram workflows |
