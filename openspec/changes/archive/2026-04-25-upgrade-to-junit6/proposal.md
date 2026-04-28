## Why

The project currently uses JUnit 5.14.1, Kotest 5.9.1, and AssertJ Core 3.27.3. JUnit 6.0.3 (GA, February 2026) and Kotest 6.1.11 (April 2026) are now stable and mutually compatible — Kotest 6 dropped its custom test engine and runs exclusively on the JUnit Platform, making the two frameworks tightly aligned. Upgrading now establishes the correct baseline before implementing the GUI test framework (ADR-0042), which would otherwise be built on a dependency stack that immediately needs upgrading.

## What Changes

- **BREAKING** Bump JUnit from `5.14.1` → `6.0.3` across all modules (Jupiter API, Engine, Params, Platform Launcher — all unified under 6.0.3)
- **BREAKING** Bump Kotest from `5.9.1` → `6.1.11`; replace `kotest-runner-junit5-jvm` artifact with `kotest-runner-junit6`
- Bump AssertJ Core from `3.27.3` → `3.27.7` (security fix CVE-2026-24400; no breaking changes)
- MockK stays at `1.14.9` (already current; works with JUnit 6 without a dedicated extension)
- Remove `kotest-datatest` artifact if now bundled into Kotest 6 core (verify during implementation)
- Update `ADR-0042` to use `assertj-swing` core only (no `assertj-swing-junit-jupiter`); add explicit note on AssertJ Swing abandonment risk and the Kotest-managed EDT approach

## Capabilities

### New Capabilities

- `test-framework-stack`: Version and configuration contract for the project-wide test dependency stack (JUnit 6, Kotest 6, AssertJ Core, MockK); covers which artifacts are used, which are removed, and the Gradle task configuration required for the new platform

### Modified Capabilities

<!-- None — no existing specs cover test infrastructure -->

## Impact

- `gradle/test-libs.versions.toml` — version bumps, artifact renames
- `gradle/libs.versions.toml` — verify no JUnit references bleed in from main deps
- Every `build.gradle.kts` that references `testLibs.kotest.runner` or `testLibs.junit.*` — update artifact names
- Modules with tests: `gui`, `server`, `booter`, `lib:common`, `lib:client`, `bot-api/java`, `bot-api/tests`, `runner`
- `docs-internal/architecture/adr/0042-gui-test-automation-and-screenshot-framework.md` — update dependency block and add AssertJ Swing risk note
- All work done on a dedicated branch (`upgrade-junit6`); merged to `main` once all module test suites pass
