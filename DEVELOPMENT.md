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

# Run the offline Rumble fixture checks and write a redacted result and report
./gradlew rumbleFixtureCheck
```

The first build downloads Gradle and all dependencies automatically.

### Rumble fixture check

`./gradlew rumbleFixtureCheck` runs the offline checks of the Rumble modules (`lib:rumble-protocol`, `rumble`, `lib:rumble` and `lib:bot-project`) and the Python checks in `scripts/rumble`, groups them into named cases, and writes two files below `build/rumble-fixture/`: `result.json`, which a program can read, and `report.md`, which a person can read. Both have tokens, the home folder, the user name and absolute paths removed automatically. The command needs no network, never runs a live check, writes nothing outside the build folder, and refuses to start when `-Drumble.live=true` is given. It collects every failure before it fails, and a case that ran no checks counts as failed. A green run says nothing about the sign-in App on a real fork, public installation permissions or a real account; the report says so every time. Python 3 must be on the path (`python` on Windows, `python3` elsewhere).

### Settings for development

These environment variables and Java system properties change how the programs behave while you develop and test them. A released program needs none of them.

| Setting | Read by | What it does |
|---|---|---|
| `RUMBLE_ENVIRONMENT` (variable) | the desktop program and the `rumble` command line | Names the Rumble to use: `public` (the default when it is not set) or `test`, the development Rumble. The command line's `-e` option overrides it. An unknown name is reported, never replaced. Use `test` for every trial that signs in, submits a bot or sends results. |
| `rumble.jar` (system property) | the desktop program | The full path of a locally built session program (`rumble-cli/build/libs/robocode-tankroyale-rumble-cli-<version>.jar`, from `./gradlew :rumble-cli:fatJar`). The desktop program then uses that jar for contribution sessions and fetches nothing. Without it, the desktop program fetches the released session program of its own version. |
| `ROBOCODE_DEBUG` (variable) | the Bot APIs (Java, .NET, Python, TypeScript) | `true` or `false` overrides whether a bot tells the server in its handshake that a debugger is attached. The server takes it as information only. Without it the Bot API detects a debugger itself. |
| `ROBOCODE_TURN_TIMING_DIAGNOSTICS` (variable) | bots started by the Battle Runner | Set to `true` by the Battle Runner for the bots it starts when its turn timing diagnostics are on; the team message stress sample bots read it. |
| `rumble.live` (system property) | `./gradlew :lib:rumble:test` and `./gradlew rumbleFixtureCheck` | `-Drumble.live=true` runs the Rumble library's live test, which reads the public Rumble, and `-Drumble.live.send=true` sends one real result batch to the test Rumble with your saved sign-in; the fixture check refuses to start with it, because it never runs a live check. |
| `winConsole` (Gradle property) | `./gradlew :gui:jpackageWin` | `-PwinConsole=true` builds a Windows installer whose program opens a console window, for diagnostics. |

The Rumble's bot launchers, in the bot repository, read their own variables: `RUMBLE_PYTHON` names the Python to run a Python bot with, and a session and the booter set it to the Python environment they prepare; `RUMBLE_JAVA_CACHE` and `RUMBLE_NODE_CACHE` name the folders where the Java and TypeScript launchers keep the Bot API and the packages they fetch, in the temporary folder by default.

Examples:

```bash
# The desktop program from a local build, against the development Rumble, with a locally built session program
RUMBLE_ENVIRONMENT=test java -Drumble.jar="$PWD/rumble-cli/build/libs/robocode-tankroyale-rumble-cli-1.4.0.jar" \
  -jar gui/build/libs/robocode-tankroyale-gui-1.4.0-all.jar

# The command line against the development Rumble
java -jar rumble-cli/build/libs/robocode-tankroyale-rumble-cli-1.4.0.jar session status -e test
```

```bat
rem An installed desktop program on Windows: its launcher takes Java settings through JAVA_TOOL_OPTIONS
set RUMBLE_ENVIRONMENT=test
set JAVA_TOOL_OPTIONS=-Drumble.jar=C:\path\to\robocode-tankroyale-rumble-cli-1.4.0.jar
"C:\Program Files\robocode-tank-royale-gui\robocode-tank-royale-gui.exe"
```

The Rumble menu is always shown; the earlier developer switch `RUMBLE_MENU` (and `rumble.menu`) is no longer read.

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

### Change workflow (Cliewen)

The permanent truth about the system lives in the [`docs/`](docs/README.md) corpus ([Cliewen](https://github.com/cliewen/cliewen) conventions since CH-001). Every non-trivial change runs the change loop (`clue-delta` skill): branch → `/changes/CH-xxx-slug/` proposal + tasks → implement → digest into the corpus → PR. The `/changes/` workspace is transient — it never reaches `main`; `clue validate` (the `validate` CI job) judges the corpus on every PR. The branch + PR + human merge is the review gate.

### Repo rules for agents

- **A full change reaches `main` only through a human-merged PR; agents never
  merge their own PRs.** Simple work may be pushed to `main` directly only when
  the maintainer explicitly authorizes that push for that specific change, per
  change and never as a standing permission, and the commits carry an
  `Authorized-Push:` trailer (constraint
  [C-002](docs/constraints/C-002-review-boundary.md) — for a full change the PR
  is the approval gate).
- After every file modification, run `./gradlew clean build`; stop and fix on
  non-zero exit.
