## 1. Branch Setup

- [x] 1.1 Create branch `upgrade-junit6` from `main`

## 2. Pre-Upgrade Audit

- [x] 2.1 Grep all `build.gradle.kts` files for hardcoded JUnit/Kotest version literals — document any found outside the catalog
- [x] 2.2 Grep all test sources for `@CsvSource` and `@CsvFileSource` usages — list files that may be affected by JUnit 6 stricter CSV parsing
- [x] 2.3 Verify on Maven Central whether `io.kotest:kotest-datatest` exists as a separate artifact in Kotest 6.1.11 or is bundled into core

## 3. Update Version Catalog

- [x] 3.1 Bump `junit` version to `6.0.3` in `gradle/test-libs.versions.toml`
- [x] 3.2 Add `junit-bom = { module = "org.junit:junit-bom", version.ref = "junit" }` to the libraries block
- [x] 3.3 Bump `kotest` version to `6.1.11` in `gradle/test-libs.versions.toml`
- [x] 3.4 Rename `kotest-runner-junit5-jvm` library entry to `kotest-runner-junit6` with artifact `io.kotest:kotest-runner-junit6`
- [x] 3.5 Remove `kotest-datatest` entry from catalog if bundled in Kotest 6 core; otherwise keep and verify artifact name
- [x] 3.6 Bump `assertj` version to `3.27.7` in `gradle/test-libs.versions.toml`
- [x] 3.7 Bump `junit-platform-launcher` to `6.0.3` (or remove if covered by the BOM)

## 4. Update Module Build Files

- [x] 4.1 Update `gui/build.gradle.kts` — replace `kotest-runner-junit5-jvm` with `kotest-runner-junit6`; add `platform(testLibs.junit.bom)`
- [x] 4.2 Update `server/build.gradle.kts` — same runner rename, BOM addition, remove `kotest.datatest` (bundled in Kotest 6)
- [x] 4.3 Update `booter/build.gradle.kts` — no test deps; skip
- [x] 4.4 Update `runner/build.gradle.kts` — same runner rename and BOM addition
- [x] 4.5 Update `bot-api/java/build.gradle.kts` — add BOM
- [x] 4.6 Update `bot-api/tests/build.gradle.kts` — no test deps (prepares bot archives only); skip
- [x] 4.7 Update `lib/common/build.gradle.kts` and `lib/intent-diagnostics/build.gradle.kts` — runner rename and BOM; `lib/client` has no test deps
- [x] 4.8 Confirm no `build.gradle.kts` outside `gradle/test-libs.versions.toml` contains a JUnit or Kotest version literal

## 5. Fix Test Failures

- [x] 5.1 Run `./gradlew :gui:test :server:test :lib:common:test :lib:intent-diagnostics:test` — all pass
- [x] 5.2 Run `./gradlew :bot-api:java:test :runner:test` — all pass
- [x] 5.3 No `@CsvSource`/`@CsvFileSource` usages found; no CSV failures
- [x] 5.4 No `InaccessibleObjectException` observed
- [x] 5.5 Fixed root `build.gradle.kts`: global test classpath attr → JVM 17, test launcher → Java 17 JVM, `compileTestJava` → Java 17 compiler (Java 11 javac cannot read Java 17 class files)

## 6. Update ADR-0042

- [x] 6.1 Replace the AssertJ Swing dependency block in ADR-0042 — declare `assertj-swing:3.17.1` core only; remove `assertj-swing-junit-jupiter`
- [x] 6.2 Add an explicit note in the Rationale or Consequences section of ADR-0042 about AssertJ Swing's abandonment risk and the Kotest-hook-based EDT management approach

## 7. Final Verification

- [x] 7.1 Run `./gradlew test` across all modules — all JVM modules pass; pre-existing TypeScript `npmTest`/`npmPack` implicit dependency warning (Gradle 9 validation) is unrelated to this change
- [x] 7.2 Dependency tree clean — all JUnit Platform `1.13.4` requests from Kotest's internal POM are resolved upward to `6.0.3` by the BOM; no JUnit 5.x artifacts at runtime
- [x] 7.3 `kotest-runner-junit5-jvm` absent from all resolved configurations; `kotest-runner-junit6:6.1.11` in use
