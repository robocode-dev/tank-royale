# Change: Replace ProGuard with R8 for CLI/Desktop application JARs

## Why

The `booter`, `server`, `recorder`, and `gui` modules currently use ProGuard for shrinking their distributable JARs.
The current rules are difficult to reason about and ProGuard has known compatibility issues with newer JDK versions
(especially Java 21+ and beyond), which makes future upgrades risky.

## What Changes

- Replace ProGuard-based shrinking with **R8** for these modules:
    - `booter`
    - `server`
    - `recorder`
    - `gui`
- Preserve the current deliverable contract:
    - A “fat” intermediate JAR is produced and then shrunk into the final published/distributed JAR.
    - The existing Gradle task contract that downstream tasks rely on remains stable (to avoid breaking installers).
- Replace the shared rules file (`proguard-common.pro`) with **module-specific** rules tuned for each module.
- Add automated smoke tests that assert:
    - `java -jar <archive>.jar --version` executes successfully for each CLI archive (and `--version` output is
      present).
- Measure and document size deltas:
    - “before” (ProGuard) archive sizes vs “after” (R8) archive sizes.

## Impact

- Affected code:
    - Build scripts and shrink rules in `booter/`, `server/`, `recorder/`, and `gui/`
    - Shared shrink configuration file `proguard-common.pro` (expected to be removed)
    - Root build conventions that depend on the shrink task name (jpackage).
- Affected specs:
    - No user-facing behavior changes are intended.
    - This change adds a build-time quality gate (archive smoke tests) and updates build tooling.

## Baseline Measurements (ProGuard)

These measurements were taken from the current workspace build outputs under `build/libs/`.

| Module   | Artifact                                  | Size (bytes) | Size (MB) |
|----------|-------------------------------------------|--------------|-----------|
| booter   | `robocode-tankroyale-booter-0.34.3.jar`   | 6,083,852    | 5.80      |
| server   | `robocode-tankroyale-server-0.34.3.jar`   | 6,091,289    | 5.81      |
| recorder | `robocode-tankroyale-recorder-0.34.3.jar` | 6,383,226    | 6.09      |
| gui      | `robocode-tankroyale-gui-0.34.3.jar`      | 21,426,158   | 20.43     |

Baseline CLI smoke behavior (ProGuard):

- `java -jar robocode-tankroyale-booter-0.34.3.jar --version` prints a version string and returns exit code 0.
- `java -jar robocode-tankroyale-server-0.34.3.jar --version` prints a version string and returns exit code 0.
- `java -jar robocode-tankroyale-recorder-0.34.3.jar --version` prints a version string and returns exit code 0.
- `java -jar robocode-tankroyale-gui-0.34.3.jar --version` prints a version string and returns exit code 0.

## Success Criteria

- R8 shrunk artifacts are produced for `booter/server/recorder/gui`.
- All module archives still run and (where supported) correctly respond to `--version`.
- Public APIs remain intact, especially Bot API artifacts and sample bot compatibility.
- A size comparison report is produced showing ProGuard vs R8 results.

