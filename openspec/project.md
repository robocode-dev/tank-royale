# Project Context

## Purpose

Robocode Tank Royale is an open-source programming game platform where users write autonomous tank "bots" that compete
in real-time battles. The project provides the game server and client GUI, plus Bot APIs for multiple languages, example
bots, and tooling for packaging and distribution. The goal is to offer an extensible, multi-platform environment for
learning programming, experimenting with AI strategies, and running online competitions.

## Tech Stack

- Java / JVM 11 (primary runtime for server and GUI)
- Kotlin 2+ (build scripts and some JVM components)
- Gradle 9.2.1+ (Kotlin DSL) as the build system
- Python 3.1+ (Bot API; supported version >= 3.10)
- .NET 8 (Bot API)
- WebSocket protocol for Bot API communication
- JSON Schemas located under `schema/schemas` used for protocol definition and code generation
- Documentation via GitHub Pages (site built in `docs-build`)

Notes:

- Runtime requirement for end users: Java 11+ to run the application.
- Recommended JDK for building the project: JDK 171 (build scripts expect 171).

## Project Conventions

This repository is multi-module and contains JVM, Python and .NET subprojects. The following conventions describe how
contributors should work in the codebase.

### Code Style

- Encoding: UTF-8 for all source and resource files. The build enforces UTF-8 Java compile encoding.
- Java and Kotlin: Target Java 11 for compiled bytecode (the Gradle toolchain and compiler options are configured
  accordingly).
- Kotlin-specific: When working on Kotlin code, prefer modern Kotlin 2 coding-style idioms — favor immutability, concise
  and expressive APIs, use of data classes, sealed interfaces, extension functions, standard library utilities and
  coroutines where appropriate. Prefer idiomatic error-handling patterns and avoid overuse of nullable types where
  possible.
- Keep sample bots and small examples as source files (not precompiled) to make them easy to read and modify.
- Clean Code: Always apply Clean Code principles when writing new code or modifying existing code. Clear naming,
  small focused functions, single responsibility, high readability and simple control flow are expected. It is allowed
  and encouraged to refactor surrounding code prior to adding a feature or fix to improve clarity and reduce
  technical debt.
- No automatic linters or formatters are enforced in the repository (by decision). Contributors should nonetheless
  follow idiomatic style for each language:
  - Java/Kotlin: common Java conventions (camelCase, PascalCase for types, descriptive names).
  - Python: follow PEP8 where practical.
  - .NET: follow typical C# conventions.

### Architecture Patterns

- Multi-module Gradle layout: root project with multiple subprojects (server, gui, bot APIs, sample-bots, schema, docs,
  etc.).
- Each subproject that publishes artifacts uses the `maven-publish` conventions and produces sources/javadoc jars when
  applicable.
- Schema-driven API: JSON schemas under `schema/` define protocol payloads. Code generation (where used) is placed under
  `bot-api/*/generated` or `generated` packages.
- Packaging: jpackage/jlink integration is available in Gradle for producing platform installers and trimmed runtimes;
  packaging tasks and icon paths are centralized in Gradle configuration.

### Testing Strategy

- Unit and integration tests are colocated in the respective subprojects (conventional Gradle locations: `src/test`, or
  language-specific test dirs).
- There is a top-level `tests/` directory for cross-cutting or external validation tests.
- Tests run using Gradle (JVM tests) and the language-specific tooling for Python/.NET where appropriate. CI should run
  all test suites on pull requests.
- Quick local checks: `./gradlew test` (or the platform-appropriate Gradle wrapper) runs JVM tests and orchestrates
  multi-module testing.
- Developer final step: When writing or modifying code, always run `./gradlew test` as the final local verification step
  before creating a pull request to ensure no regressions have been introduced.

### Git Workflow

- Branching strategy: GitHub Flow (branch per feature or fix, open a pull request, require at least one approving review
  and CI green before merge).
- Commit message convention: Conventional Commits are required. Use a scope when relevant (example:
  `feat(gui): add replay controls` or `docs(openspec): add project context`).
- Direct commits to `main` are discouraged. Create a short-lived branch, open a PR, and merge when CI and reviews pass.
- Keep pull requests small and focused. Rebase or squash as appropriate to keep history readable.

## Domain Context

- Bots connect to the server using a WebSocket-based Bot API that follows the JSON schema definitions under
  `schema/schemas`.
- The repository contains full Bot API implementations for Python, JVM (Java/Kotlin and other JVM languages) and .NET;
  these expose helpers to connect, inspect game state, and issue commands.
- Sample bots are provided in `sample-bots/` for different languages to demonstrate API usage and to serve as canonical
  examples.
- Real-time constraints: battles are time-sensitive, so bot implementations must be designed to react and make decisions
  within the pacing limits of the protocol.

## Important Constraints

- Build JDK requirement: Building the project requires JDK 171 (Gradle build scripts validate the JDK). Runtime for end
  users is Java 11+.
- Python Bot API targets Python >= 3.10.
- Packaging: jpackage and jlink are used for native installers; a compatible JDK and platform-specific packaging tools
  are required when creating installers.
- Signing/publishing: Maven Central / OSSRH publishing and signing are supported but require appropriate credentials and
  PGP keys (signing is skipped when keys are absent).
- Cross-platform resource expectations: certain assets (icons, license files) live under `gfx/` and `LICENSE` and are
  referenced by packaging tasks.

## External Dependencies

- Maven Central (primary artifact repository for JVM dependencies).
- PyPI (for any published Python Bot API releases).
- NuGet (for .NET Bot API packages if published).
- GitHub Pages (documentation site published from `docs-build` outputs).
- The JSON schemas in `schema/` are the single source of truth for public Bot API messages; generated client code or
  bindings must remain compatible with them.

## How to update this document

- Update `openspec/project.md` when the project adds/removes primary languages, changes minimum platform/runtime
  versions, or alters Git workflow and commit conventions.
- For changes that affect build or runtime constraints, update the corresponding build files (`build.gradle.kts`,
  `bot-api/*/pyproject.toml`, etc.) in the same commit and reference the change in the commit message.

---

Last updated: please keep this file current when significant project policies or constraints change.
