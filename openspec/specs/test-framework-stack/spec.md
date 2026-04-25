# Spec: Test Framework Stack

## Purpose

Defines the test dependency versions and tooling constraints for all modules in the project. Ensures consistent, auditable test dependencies via a single version catalog and a coherent JUnit 6 + Kotest 6 platform.

## Requirements

### Requirement: JUnit 6 as the test platform
The project SHALL use JUnit 6.0.3 (Jupiter + Platform) as its test execution platform, declared via the JUnit BOM in `gradle/test-libs.versions.toml`. No module SHALL pin a JUnit version independently of the catalog.

#### Scenario: BOM governs all JUnit artifact versions
- **WHEN** a module declares `junit-jupiter`, `junit-jupiter-params`, or `junit-platform-launcher` as test dependencies
- **THEN** the version is resolved from `org.junit:junit-bom:6.0.3` and no explicit version is specified on the individual artifact

#### Scenario: No JUnit 5 artifacts remain
- **WHEN** the full dependency tree is resolved for any module
- **THEN** no artifact with `groupId` `org.junit.jupiter` or `org.junit.platform` at version `5.x` is present

---

### Requirement: Kotest 6 as the test framework
The project SHALL use Kotest 6.1.11 with the `kotest-runner-junit6` runner. The legacy `kotest-runner-junit5-jvm` artifact SHALL NOT appear in any module's dependency graph.

#### Scenario: Kotest runner resolves correctly under JUnit 6
- **WHEN** `./gradlew test` is executed on any module that uses Kotest specs
- **THEN** Kotest discovers and executes all `StringSpec`, `FunSpec`, and `FunSpec`-derived test classes without error

#### Scenario: kotest-datatest bundling verified
- **WHEN** the `gradle/test-libs.versions.toml` is updated
- **THEN** `kotest-datatest` is declared as a separate artifact only if it does not exist as part of the Kotest 6 core BOM; otherwise it is removed from the catalog

---

### Requirement: AssertJ Core at 3.27.7
The project SHALL use AssertJ Core 3.27.7. This version fixes CVE-2026-24400 (XXE in `isXmlEqualTo()`).

#### Scenario: AssertJ version bump takes effect
- **WHEN** the dependency is resolved in any module that declares `assertj-core`
- **THEN** the resolved version is `3.27.7` and no transitive resolution pulls in an older version

---

### Requirement: AssertJ Swing core artifact only
Any module that uses AssertJ Swing SHALL declare only `org.assertj:assertj-swing:3.17.1`. The `assertj-swing-junit` and `assertj-swing-junit-jupiter` artifacts SHALL NOT be declared as dependencies.

#### Scenario: No JUnit-integration artifact from AssertJ Swing
- **WHEN** the dependency tree is resolved for `gui`
- **THEN** neither `assertj-swing-junit` nor `assertj-swing-junit-jupiter` appears in the resolved configuration

#### Scenario: EDT management via Kotest hooks
- **WHEN** a GUI test opens a Swing dialog
- **THEN** the dialog is constructed and shown inside a `SwingUtilities.invokeAndWait {}` block managed by a Kotest `beforeSpec` or `beforeTest` hook, not by an AssertJ Swing JUnit extension

---

### Requirement: MockK compatibility confirmed
MockK 1.14.9 SHALL remain at its current version. All existing MockK usages SHALL continue to function under JUnit 6 without modification.

#### Scenario: MockK mocks work inside Kotest specs on JUnit 6
- **WHEN** a test using `mockk<T>()` or `spyk()` runs under the Kotest 6 + JUnit 6 platform
- **THEN** the mock is created, configured, and verified without error

---

### Requirement: Version catalog is the single source of truth
All test dependency versions across all modules SHALL be declared in `gradle/test-libs.versions.toml`. No `build.gradle.kts` SHALL specify a version literal for a test dependency covered by the catalog.

#### Scenario: Grep finds no hardcoded test versions
- **WHEN** all `build.gradle.kts` files in the repository are searched for patterns matching JUnit or Kotest version literals (e.g., `"5.14"`, `"6.0."`, `"6.1."`)
- **THEN** no matches are found outside `gradle/test-libs.versions.toml`

---

### Requirement: Full test suite passes on upgrade branch
All module test suites SHALL pass without modification to test logic after the dependency upgrade. Tests that fail solely due to stricter JUnit 6 CSV parsing SHALL be fixed as part of this change.

#### Scenario: ./gradlew test exits cleanly
- **WHEN** `./gradlew test` is run on the `upgrade-junit6` branch with no other changes
- **THEN** all test tasks in all modules complete with BUILD SUCCESSFUL and zero test failures

#### Scenario: JUnit 6 CSV strictness addressed
- **WHEN** any `@CsvSource` or `@CsvFileSource` test is encountered
- **THEN** the test passes under JUnit 6's stricter CSV parser (extra characters after closing quotes cause a parse error in JUnit 6 — any such cases are fixed before merge)
