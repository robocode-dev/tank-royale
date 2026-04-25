## Context

The project test stack today:

| Library | Version | Artifact |
|---------|---------|---------|
| JUnit Jupiter API/Engine/Params | 5.14.1 | `junit-jupiter-api`, `junit-jupiter-engine`, `junit-jupiter-params` |
| JUnit Platform Launcher | 1.14.1 | `junit-platform-launcher` |
| Kotest Runner | 5.9.1 | `kotest-runner-junit5-jvm` |
| Kotest DataTest | 5.9.1 | `kotest-datatest` |
| AssertJ Core | 3.27.3 | `assertj-core` |
| MockK | 1.14.9 | `mockk` |

Version pins live in `gradle/test-libs.versions.toml`. Individual modules declare test dependencies against this catalog; none pin versions independently. All modules target Java 21 runtime (≥ JUnit 6's Java 17 minimum).

Two version catalog files are relevant: `gradle/libs.versions.toml` (main deps) and `gradle/test-libs.versions.toml` (test-only deps). JUnit references exist only in the test catalog.

## Goals / Non-Goals

**Goals:**
- Upgrade JUnit to 6.0.3 and Kotest to 6.1.11 in `gradle/test-libs.versions.toml`
- Update all `build.gradle.kts` files that reference the renamed Kotest runner artifact
- Verify all existing tests pass on the new stack
- Update ADR-0042 to reflect the correct AssertJ Swing dependency (core only) and document the abandonment risk
- Perform all work on a `upgrade-junit6` branch

**Non-Goals:**
- Rewriting existing tests to use JUnit 6-specific APIs
- Adding new tests as part of this change
- Upgrading the Gradle wrapper or Kotlin version
- Addressing AssertJ Swing's long-term replacement (tracked separately via ADR-0042)

## Decisions

### 1. Use the JUnit BOM for version alignment

JUnit 6 unifies Platform, Jupiter, and Vintage under a single version (`6.0.3`). Use the BOM to avoid specifying individual artifact versions:

```toml
# gradle/test-libs.versions.toml
[versions]
junit = "6.0.3"

[libraries]
junit-bom            = { module = "org.junit:junit-bom",                      version.ref = "junit" }
junit-jupiter        = { module = "org.junit.jupiter:junit-jupiter",           version.ref = "junit" }
junit-jupiter-params = { module = "org.junit.jupiter:junit-jupiter-params",    version.ref = "junit" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher", version.ref = "junit" }
```

In each `build.gradle.kts` that uses the BOM:
```kotlin
dependencies {
    testImplementation(platform(testLibs.junit.bom))
    testImplementation(testLibs.junit.jupiter)
    // ...
}
```

**Why BOM over individual pins:** Eliminates version skew between Platform and Jupiter — a common source of subtle failures in JUnit 5 projects. JUnit 6 unified versioning makes the BOM even more natural.

### 2. Replace Kotest runner artifact

Kotest 6 renames its JUnit Platform runner:

```toml
# Before
kotest-runner-junit5-jvm = { module = "io.kotest:kotest-runner-junit5-jvm", version.ref = "kotest" }

# After
kotest-runner-junit6 = { module = "io.kotest:kotest-runner-junit6", version.ref = "kotest" }
```

Verify whether `kotest-datatest` is now bundled into Kotest 6 core or still a separate artifact; remove the separate declaration if redundant.

**Why not keep `junit5-jvm` artifact:** The artifact no longer exists in Kotest 6. Using the correct artifact name avoids a silent fallback to an incompatible engine.

### 3. AssertJ Swing: core artifact only, no JUnit integration module

ADR-0042 proposes `assertj-swing-junit-jupiter` for EDT management. That artifact targets JUnit 5's extension model and is from an abandoned project. Instead:

- Depend only on `org.assertj:assertj-swing:3.17.1` (core)
- Manage EDT in tests via Kotest's `beforeSpec`/`afterSpec` hooks and `SwingUtilities.invokeAndWait {}`
- Drop `assertj-swing-junit` and `assertj-swing-junit-jupiter` from the planned dependency list

This avoids binding the GUI test framework to a JUnit-version-specific integration artifact from an unmaintained library.

### 4. MockK: no changes needed

MockK 1.14.9 works with JUnit 6 projects. The project does not use `MockKExtension` (JUnit 5 extension); MockK is used directly via `mockk<T>()` factory calls in Kotest specs. No migration required.

### 5. AssertJ Core: minor bump

`3.27.3` → `3.27.7`. No API changes. Fixes CVE-2026-24400 (XXE in `isXmlEqualTo()`). Low risk.

### 6. Branch strategy

All changes land on `upgrade-junit6` branched from `main`. Merge criteria:
- `./gradlew test` passes on all modules with tests
- No `@Deprecated` usages introduced by the migration remain unaddressed

## Risks / Trade-offs

| Risk | Mitigation |
|------|-----------|
| JUnit 6 CSV parsing is stricter (extra chars after closing quote → exception) | Audit `@CsvFileSource` and `@CsvSource` usages before merging; fix any failing cases |
| Kotest 6 introduced breaking changes to its Gradle plugin and KSP setup | Check Kotest 6 migration guide; verify `io.kotest` Gradle plugin version if used |
| `assertj-swing` 3.17.1 compiled against Java 8 bytecode — may surface warnings under Java 21 strict module access | Run tests with `--info` and address any `InaccessibleObjectException` with `--add-opens` JVM args if needed |
| Some modules may declare JUnit dependencies inline (not via catalog) | Grep all `build.gradle.kts` for hardcoded `junit` strings before starting |

## Migration Plan

1. Create branch `upgrade-junit6` from `main`
2. Audit all `build.gradle.kts` for JUnit/Kotest references (grep for `junit`, `kotest`)
3. Update `gradle/test-libs.versions.toml` — version bumps and artifact renames
4. Update each `build.gradle.kts` — replace `kotest-runner-junit5-jvm` with `kotest-runner-junit6`, add BOM if missing
5. Run `./gradlew :gui:test :server:test :booter:test :bot-api:java:test :runner:test` — fix any failures
6. Update `ADR-0042` — replace dependency block, add AssertJ Swing risk note
7. Run full `./gradlew test` — confirm clean
8. Open PR from `upgrade-junit6` → `main`

**Rollback:** Branch is isolated; `main` is unaffected until PR merge.

## Open Questions

- Does `kotest-datatest` still exist as a separate artifact in Kotest 6.1.11, or is it bundled? (Verify on Maven Central before updating the catalog.)
- Are there any `@CsvSource` / `@CsvFileSource` usages in the codebase that need auditing for the stricter JUnit 6 CSV parser?
