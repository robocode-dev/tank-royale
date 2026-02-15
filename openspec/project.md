# Project Context

## Project Governance

**Open Source, Single Maintainer**

This is an **open source project on GitHub** (Apache License 2.0), primarily maintained by **Flemming N. Larsen** as a spare-time, non-profit effort. It is **NOT a corporate project**.

- **Maintainer:** [@flemming-n-larsen](https://github.com/flemming-n-larsen)
- **Contributors:** Welcome! See [CONTRIBUTING.md](../CONTRIBUTING.md)
- **Repository:** https://github.com/robocode-dev/tank-royale
- **License:** Apache License 2.0, Copyright © 2022 Flemming N. Larsen

**Distribution Channels:**

- **GitHub Releases:** Primary distribution as platform installers and JAR artifacts
- **Maven Central:** Java/JVM Bot API (via nexus-publish)
- **NuGet:** .NET Bot API
- **PyPI:** Python Bot API

**Development Reality:**

- Solo developer — no team coordination or migration guides needed
- Manual testing acceptable (automated where reasonable)
- Users report bugs via GitHub issues
- No enterprise overhead (performance SLAs, benchmarking requirements, etc.)

## Purpose

Robocode Tank Royale is a programming game where players code autonomous tank-bots that compete against each other in a
virtual battle arena. The goal is to help users learn programming and AI skills in a fast-running, real-time competitive
game.

**"Build the best – destroy the rest!"**

Key goals:

- Provide a fun programming game that helps users develop programming and AI skills
- Serve education with clear goals and benchmarkable performance
- Enable competition (e-sports for programming games)
- Support cross-platform play via WebSocket protocol
- Maintain backward compatibility with bot behavior and game physics

## Tech Stack

### Build System

- **Gradle** with Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`)
- Version catalog: `gradle/libs.versions.toml`
- Plugins: Shadow JAR, R8, jsonschema2pojo, nexus-publish

### Languages & Runtimes

- **Kotlin 2.2.0** – primary language for server, GUI, booter, recorder
- **Java 11+** – runtime target (JDK 17-21 required for building)
- **Python** – bot API and sample bots
- **.NET / C#** – bot API and sample bots
- **WebAssembly (Wasm)** – work in progress for browser-based bots

### Core Libraries

- `org.java-websocket:Java-WebSocket` – WebSocket communication
- `com.google.code.gson:gson` – JSON serialization
- `org.jetbrains.kotlinx:kotlinx-serialization-json` – Kotlin serialization
- `com.github.weisj:jsvg` – SVG rendering (GUI)
- `com.miglayout:miglayout-swing` – Swing layout (GUI)
- `com.github.ajalt.clikt:clikt` – CLI parsing
- `com.android.tools:r8` – code shrinking/optimization
- `org.slf4j:slf4j-api` – logging

### Project Modules

| Module           | Description                     |
|------------------|---------------------------------|
| `server`         | Authoritative game server       |
| `gui`            | Swing-based desktop GUI         |
| `booter`         | Bot process launcher            |
| `recorder`       | Battle recording/playback       |
| `lib:common`     | Shared utilities                |
| `lib:client`     | WebSocket client library        |
| `bot-api:java`   | Java/JVM Bot API                |
| `bot-api:dotnet` | .NET Bot API                    |
| `bot-api:python` | Python Bot API                  |
| `sample-bots:*`  | Example bots (Java, C#, Python) |
| `docs-build`     | Documentation site              |

## Project Conventions

### Code Style

- **Kotlin/Java**: Follow standard conventions (PascalCase classes, camelCase methods)
- **Python**: Follow PEP8; use standard Python conventions
- **C#/.NET**: Follow standard .NET naming conventions
- **Documentation**: Use American English
- Keep commits small and focused
- Use `TODO`/`FIXME` comments where appropriate
- Add code comments where decisions might be non-obvious

### Architecture

Full architecture documentation (ADRs, C4 views, message schemas, flows) lives in
[`docs-internal/architecture/`](../docs-internal/architecture/README.md). Key patterns: monorepo, authoritative server,
WebSocket + JSON protocol, schema-driven message contracts, cross-platform symmetric APIs, tick-based simulation.

> **Do not duplicate architecture details here.** Reference the architecture docs instead.

### Testing Strategy

- Provide tests where reasonable
- Manual testing is acceptable when automation isn't practical
- Ensure PRs are buildable and testable
- Don't break existing functionality
- Changes to Bot APIs require keeping all platforms in sync

### Git Workflow

- **Pull requests**: All contributions via PRs to `main`
- **Small, focused PRs**: Avoid "big bang" changes; don't mix unrelated work
- **Link context**: Reference related issues/discussions in PRs
- **Explain verification**: Describe how to test the change
- **Coordinate large changes**: Contact maintainer before implementing major features
- **Maintainer**: [@flemming-n-larsen](https://github.com/flemming-n-larsen)

## Domain Context

### Core Concepts

- **Bot**: An autonomous tank controlled by user code; never directly controlled during battle
- **Battle/Match**: A simulation where bots compete; runs tick-by-tick on the server
- **Tick**: One discrete step of the game loop; bots receive state and submit actions each tick
- **Arena**: The virtual battlefield with walls and dimensions
- **Recording**: `.battle.gz` files in `recordings/` for replay and analysis

### Game Elements

- **Movement**: Bots can move forward/backward and turn
- **Scanning**: Bots have a radar to detect other bots
- **Firing**: Bots can fire bullets at detected enemies
- **Events**: Bots receive events (hit by bullet, collision, scanned enemy, etc.)

### Bot API Languages

- **Java/JVM**: Also supports Groovy, Kotlin, Scala, Jython, Clojure
- **.NET**: Also supports C#, F#, Visual Basic, IronPython
- **Python**: Native Python implementation
- **Custom**: Any language supporting WebSocket can implement the protocol

## Important Constraints

### Stability Rules (Non-negotiable)

- **No breaking game physics**: Core game rules and physics must remain stable
- **Backward compatibility**: Changes must not break existing bots
- **Server is authoritative**: No alternative drop-in server replacements

### Bot API Rules (Non-negotiable)

- **1:1 semantic equivalence with Java**: All official Bot APIs (Python, .NET, Wasm) must be semantically identical to
  the Java Bot API, which is the reference implementation
- **Java is the reference**: The classic Robocode API was originally written for Java, and Tank Royale remains loyal to
  this heritage. The Java Bot API is the most battle-tested implementation and serves as the authoritative source for
  all other platform implementations. When in doubt, Java's behavior is authoritative.
- **Properties allowed**: Python/C# may use properties instead of getters/setters, but semantics must be identical
- **Same defaults and validation**: Default values, validation rules, and error handling must match across platforms
- **Same event order**: Events must fire in the same order across all platforms
- **No extra/missing methods**: All platforms must have equivalent methods; no platform-specific additions to the
  public API
- **Rationale**: This hard requirement exists for maintainability, testing, documentation, and learning purposes

### Build Requirements

- **JDK 17+ required** for building
- **Java 11+ required** for running (end users)
- Use Dev Container (`.devcontainer/`) for consistent development environment
- Python 3.x for Python bot development

### Design Constraints

- Bot API changes must sync across all platforms (Java, .NET, Python)
- Protocol changes affect all clients; coordinate via `schema/schemas/`
- Keep documentation updated with code changes

> See [ADR-0003](../docs-internal/architecture/adr/0003-cross-platform-bot-api-strategy.md) for cross-platform
> design rationale and [architecture docs](../docs-internal/architecture/) for full architectural context.

### License

- Apache License 2.0
- Copyright © 2022 Flemming N. Larsen

## External Dependencies

### Required for Development

- **JDK 17-21**: Eclipse Temurin recommended
- **Gradle**: Use included wrapper (`gradlew`/`gradlew.bat`)
- **Dev Container**: VS Code devcontainer for pre-configured environment

### Required for Running

- **Java 11+**: For running server/GUI/booter
- **Python 3.10+**: For Python bots
- **.NET Runtime 8+**: For .NET bots

### External Services

- **GitHub**: Source control and issue tracking
- **Maven Central**: Java library publishing (via nexus-publish)
- **PyPI**: Python package publishing
- **NuGet**: .NET package publishing

### Documentation

- Main docs: https://robocode-dev.github.io/tank-royale/
- [Getting Started](https://robocode-dev.github.io/tank-royale/tutorial/getting-started)
- [Bot API docs](https://robocode-dev.github.io/tank-royale/api/apis.html)
- [Build tools](../docs-build/docs/dev/tools.md)
- [Roadmap](https://github.com/robocode-dev/tank-royale/wiki/Roadmap)

