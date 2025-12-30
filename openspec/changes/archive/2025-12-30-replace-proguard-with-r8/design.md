## Context

The `booter`, `server`, `recorder`, and `gui` modules produce "fat" Java/Kotlin application jars and then run ProGuard
to
shrink them. ProGuard rule authoring and troubleshooting is currently difficult, and ProGuard compatibility is a risk
for
newer JDK versions.

## Goals / Non-Goals

### Goals

- Replace ProGuard with R8 for shrinking application jars in `booter/server/recorder/gui`.
- Preserve artifact shape and downstream Gradle task contracts (especially for installer packaging in `gui`).
- Create a repeatable verification gate that the produced jars can execute (at least `--version`).
- Improve maintainability by removing the shared rules file and maintaining module-local rule files.

### Non-Goals

- Changing runtime target (still Java 11+).
- Breaking or reworking bot APIs.
- Introducing obfuscation as part of this change (unless required for meaningful shrinking).

## Decisions

### Decision: Keep the shrink task name stable

Where possible, keep the task name (currently `proguard`) stable to avoid updating all downstream dependencies
(jpackage wiring and `gui` copy tasks). The underlying implementation will switch from ProGuard to R8.

### Decision: Prefer module-local rules

Remove `proguard-common.pro` and keep all rules local to each module to make it easier to understand why a specific keep
rule exists.

### Decision: Verification as a Gradle test task

Add a small suite of tests (or a dedicated Gradle task) that executes the produced archives with `--version` to verify:

- main class is preserved
- CLI wiring still works in the shrunk jar

## Risks / Trade-offs

- Shrinkers can break reflection-heavy code (Kotlinx Serialization, CLI parsing, service loaders).
    - Mitigation: start from a minimal ruleset, run tests, and expand keep rules only where needed.
- R8 vs ProGuard differences tooling-wise may require some rule adjustments.

## Migration Plan

1. Add baseline smoke tests and capture ProGuard size measurements.
2. Replace ProGuard task implementation with R8.
3. Tune keep rules per module until smoke tests and unit tests pass.
4. Re-measure sizes and document deltas.

## Open Questions

- Should the GUI archive support `--version`? If not, what is the smallest automated runtime check we can run for it?

