# Development Setup

Practical guide for getting a working build environment for Robocode Tank Royale.

## Setup

`.claude/commands` and `.claude/skills` are Git symlinks that point into `.agents/`. On Linux and macOS this works automatically after cloning.

On Windows, do this **once before cloning** (or before your first `git checkout`):

1. Enable Developer Mode: Settings → System → For developers → Developer Mode → On
2. `git config --global core.symlinks true`

Without Developer Mode, Git materialises symlinks as plain text stub files instead of real filesystem links.

---

## Prerequisites

The build requires the following tools:

| Tool | Version | Purpose |
|------|---------|---------|
| JDK | 17+ | Build tool (Gradle) |
| JDK | 11 | Java/JVM Bot API toolchain |
| .NET SDK | 8.0 | .NET Bot API |
| Python | 3.x + venv | Python Bot API |
| DocFX | 2.78.4 | .NET API docs |
> End users only need Java 11+. The extra tooling is required for building all modules.

---

## Option A — Dev Container (recommended)

Requires [Docker](https://docs.docker.com/get-docker/) and
[VS Code](https://code.visualstudio.com/) with the
[Dev Containers extension](https://marketplace.visualstudio.com/items?itemName=ms-vscode-remote.remote-containers).

1. Open the repo folder in VS Code.
2. When prompted, click **Reopen in Container** (or run `Dev Containers: Reopen in Container` from the command palette).
3. VS Code builds the image and runs `scripts/setup.sh` automatically — all tools are installed inside the container.

Everything runs as `devuser` with passwordless `sudo`, so no interaction is needed.

---

## Option B — Plain Ubuntu/Debian host

> **Note:** `scripts/setup.sh` targets Linux (Ubuntu/Debian) and is the same script used inside the
> dev container. **Linux/Ubuntu is the preferred build platform.** Building on Windows is possible,
> but requires manually installing each tool listed in the prerequisites table — no automated script
> is provided for Windows.


Run the setup script once from the repo root:

```bash
chmod +x scripts/setup.sh
./scripts/setup.sh
```

The script is **idempotent** — re-running it skips tools that are already present.

What it installs (if missing):

- **.NET 8.0 SDK** — via `apt` (`dotnet-sdk-8.0`), placed on the system PATH automatically
- **Java 17 + Java 11** — via Eclipse Temurin / Adoptium apt repository
- **Python 3 + venv** — via apt, version-specific package detected automatically
- **DocFX 2.78.4** — as a `dotnet` user tool

Environment variables (`DOTNET_ROOT`, `JAVA_HOME`, `PATH`) are appended to `~/.bashrc` and `~/.profile` so they persist across sessions. **Reload your shell after the first run:**

```bash
source ~/.bashrc
```

---

## Building

All build commands are run from the repo root via the Gradle wrapper.

```bash
# Full build of all modules
./gradlew build

# Build without running tests
./gradlew build -x test

# Clean everything first
./gradlew clean build

# Build a specific subproject
./gradlew :bot-api:java:build
./gradlew :bot-api:dotnet:build
./gradlew :bot-api:python:build
```

The first build downloads Gradle and all dependencies automatically.

---

## Agent-assisted development

This repo supports AI coding agents via the [`AGENTS.md`](AGENTS.md) convention
(tested with Claude Code, Codex, GitHub Copilot, and OpenCode). `CLAUDE.md`
simply re-exports `AGENTS.md`.

### Instruction files

`AGENTS.md` maps each task type to the instruction file the agent should load
before working. Files live in [`.agents/instructions/`](.agents/instructions/):

| Task | Instruction file |
|------|------------------|
| Planning, proposals, specs | `openspec.md` |
| Architecture, ADRs | `architecture.md` |
| Debugging, timing, race conditions | `debugging.md` (+ `docs/DEBUGGING-GUIDE.md`) |
| Bot API (Java / Python / .NET) | `cross-platform.md` + `core-principles.md` |
| Testing, builds, Gradle | `testing-and-build.md` |
| Documentation | `documentation.md` |
| Changelog, release notes | `changelog.md` |
| Code style, naming | `coding-conventions.md` |
| File encoding, UTF-8 | `standards.md` |
| General (default) | `core-principles.md` |

### Skills (slash commands)

Skills live under [`.agents/skills/`](.agents/skills/). On Claude Code they are
exposed as slash commands via the `.claude/commands` and `.claude/skills`
symlinks (see [Setup](#setup) for Windows symlink prerequisites):

| Command | Purpose |
|---------|---------|
| `/release` | Release to Maven Central, NuGet, PyPI, npmjs |
| `/update-deps` | Check and upgrade dependency versions |
| `/deploy-sample-bots [dir]` | Build and deploy sample-bot zips |
| `/structurizr` | Generate C4 SVGs from DSL workspaces |
| `/dot-scout [path]` | Detect applicable code-quality principles |
| `/dot-prime [target]` | Activate principles before editing |
| `/dot-audit [target]` | Audit code/docs against principles |

### OpenSpec workflow

The [`openspec/`](openspec/) directory holds change proposals
(`changes/` → in-progress and `changes/archive/` → completed) and shared
specs (`specs/`). The workflow is:

1. `/opsx:propose` (or manually create `openspec/changes/<name>/proposal.md`,
   `tasks.md`, and `specs/`)
2. Present the proposal — **stop and wait for maintainer approval.**
3. `/opsx:apply` to implement once approved.
4. `/opsx:archive` when done.

Full instructions: [`.agents/instructions/openspec.md`](.agents/instructions/openspec.md).

### Repo rules for agents

- **Never commit without explicit maintainer instruction.**
- After every file modification, run `./gradlew clean build`; stop and fix on
  non-zero exit.
