---
description: Check for dependency updates, upgrade versions in TOML catalogs, upgrade the Gradle wrapper, and verify the build. Use when the user runs /update-deps to update project dependencies.
argument-hint: ""
allowed-tools: Read, Edit, Bash
version: 1.0.0
authors: Flemming N. Larsen (https://github.com/flemming-n-larsen)
---

# Update Dependencies

You are executing the Tank Royale dependency update workflow. Follow these phases exactly, in order. **If ANY step fails (non-zero exit code) unless explicitly noted, STOP immediately with an ERROR — never continue to the next step, and never downgrade errors to warnings.**

Announce each step clearly before executing it.

## Phase 1 — Pre-flight Checks

### 1.1 — Verify repository root

Confirm that `build.gradle.kts`, `gradle.properties`, `gradle/libs.versions.toml`, and `gradle/test-libs.versions.toml` exist in the current working directory.

- If any is missing: print `"❌ ERROR: Not at the repository root or version catalog files are missing. Please cd to the Tank Royale repository root."` and **STOP**.

### 1.2 — Detect platform

Determine whether you are running on Windows or Unix/macOS. This affects the Gradle wrapper command:
- **Windows**: use `.\gradlew.bat`
- **Unix/macOS**: use `./gradlew`

Print: `"📋 Platform: Windows"` or `"📋 Platform: Unix/macOS"`

### 1.3 — Display current versions

Read `gradle.properties` and extract the `version=X.Y.Z` value.
Read `gradle/wrapper/gradle-wrapper.properties` and extract the Gradle version from the `distributionUrl`.

Print:
```
📋 Project version: X.Y.Z
📋 Gradle wrapper version: A.B.C
```

---

## Phase 2 — Check Available Dependency Updates

### 2.1 — Run dependency update check

Print: `"🔍 Checking for dependency updates (release versions only)..."`

Run the Gradle command (use the platform-appropriate wrapper):
```
./gradlew dependencyUpdates -Drevision=release
```

- If the command **fails**: print `"❌ ERROR: dependencyUpdates task failed"` and **STOP**.
- If the command **succeeds**: continue to parse the output.

### 2.2 — Parse and display available updates

Parse the `dependencyUpdates` output. It reports dependencies in sections:
- **"The following dependencies have later release versions"** — these are the upgradable dependencies
- Each line shows: `group:artifact [current -> latest]`

Read the version catalog files to match dependencies to their TOML entries:
- Read `gradle/libs.versions.toml` for production dependency versions
- Read `gradle/test-libs.versions.toml` for test dependency versions

Display a summary table of all available updates, grouped by catalog file:

```
📦 Available dependency updates:

  gradle/libs.versions.toml:
    kotlin              2.3.20  →  2.4.0
    gson                2.13.2  →  2.14.0
    ...

  gradle/test-libs.versions.toml:
    junit               5.14.1  →  5.15.0
    kotest              5.9.1   →  6.0.0
    ...

  Gradle plugins:
    shadow              9.4.0   →  9.5.0
    ...
```

If a version entry in the TOML file has an **inline comment** (e.g., `# 5.1.0 gives problems with R8f`), display a ⚠️ warning next to that dependency:

```
    clikt               5.0.3   →  5.1.0   ⚠️ Comment: "5.1.0 gives problems with R8f"
```

If **no updates** are available: print `"✅ All dependencies are up to date."` and skip to Phase 3.

### 2.3 — User confirmation

**Ask the user** which updates to apply. Present the options:
1. **All** — apply all available updates
2. **Select** — let the user pick which updates to apply (list them and ask)
3. **Skip** — skip dependency updates entirely and proceed to Phase 3

Wait for the user's response before continuing. If the user selects specific updates, note which ones were chosen.

---

## Phase 3 — Check Gradle Wrapper Update

### 3.1 — Determine latest Gradle version

Run the following command to check for the latest Gradle release version:
```
./gradlew wrapper --gradle-version latest --dry-run
```

If the `--dry-run` flag is not supported, use an alternative approach: read the current version from `gradle/wrapper/gradle-wrapper.properties` and look up the latest version using:
```
curl -s https://services.gradle.org/versions/current
```
Parse the JSON response to extract the `version` field.

If neither approach works (e.g., no internet access), print `"⚠️ Could not determine latest Gradle version. Skipping wrapper upgrade."` and proceed to Phase 4.

### 3.2 — Compare versions

Compare the current Gradle wrapper version (from Phase 1.3) with the latest available version.

- If they are the **same**: print `"✅ Gradle wrapper is already at the latest version (A.B.C)."` and skip to Phase 4.
- If a **newer version** is available: print `"📋 Gradle wrapper update available: A.B.C → X.Y.Z"` and ask the user whether to upgrade.

### 3.3 — Upgrade Gradle wrapper (if user confirms)

If the user confirms the upgrade, run:
```
./gradlew wrapper --gradle-version X.Y.Z
```

Then run the wrapper task a second time to ensure the wrapper itself is updated consistently:
```
./gradlew wrapper --gradle-version X.Y.Z
```

- If the command **succeeds**: print `"✅ Gradle wrapper upgraded to X.Y.Z"`.
- If the command **fails**: print `"❌ ERROR: Gradle wrapper upgrade failed"` and **STOP**.

---

## Phase 4 — Apply Dependency Updates

If the user skipped all dependency updates in Phase 2.3 and no Gradle wrapper upgrade was performed, print `"ℹ️ No changes to apply."` and skip to Phase 6.

### 4.1 — Edit version catalog files

For each selected dependency update, edit the corresponding TOML version catalog file:

- **`gradle/libs.versions.toml`** — for production dependencies and plugins
- **`gradle/test-libs.versions.toml`** — for test dependencies

For each version entry, change the version string from the old value to the new value. For example:
```
# Before
kotlin = "2.3.20"

# After
kotlin = "2.4.0"
```

**Important rules for editing:**
1. **Preserve inline comments** — if a line has a comment, keep it (e.g., `clikt = "5.1.0"  # was 5.0.3, previous comment: 5.1.0 gives problems with R8f`)
2. Actually: **replace the old comment** with a note about the old version if the comment was a warning about the new version. For example, if `clikt = "5.0.3"  # 5.1.0 gives problems with R8f` is upgraded to 5.1.0, change it to: `clikt = "5.1.0"  # ⚠️ Previously noted: 5.1.0 gives problems with R8f`
3. **Do not reorder** entries in the TOML file
4. **Do not modify** entries that were not selected for update

### 4.2 — Display changes

Print a summary of all changes made:

```
📝 Changes applied:

  gradle/libs.versions.toml:
    kotlin              2.3.20  →  2.4.0
    gson                2.13.2  →  2.14.0

  gradle/test-libs.versions.toml:
    junit               5.14.1  →  5.15.0

  Gradle wrapper:
    gradle              9.4.1   →  9.5.0  (or "unchanged")
```

---

## Phase 5 — Build Verification

### 5.1 — First build attempt

Print: `"🔨 Running clean build to verify updates..."`

Run the Gradle command (use the platform-appropriate wrapper):
```
./gradlew clean build
```

- If the command **succeeds**: print `"✅ Build passed on first attempt."` and proceed to Phase 6.
- If the command **fails**: continue to step 5.2.

### 5.2 — Analyze failure

Examine the build output to determine the failure type:

- **Compilation failure** (e.g., `Compilation failed`, `error:`, `Unresolved reference`, `cannot find symbol`): print `"❌ ERROR: Compilation failed. The dependency update introduced breaking changes."` — show the relevant error output and **STOP**. Do **not** retry compilation failures.
- **Test failure** (e.g., `Test failed`, `FAILED`, test report paths): continue to step 5.3.
- **Other failure** (e.g., dependency resolution, configuration errors): print the error and **STOP**. Do **not** retry.

### 5.3 — Retry for flaky tests

Print: `"⚠️ Build failed due to test failure. Retrying to check for flaky tests..."`

Run the build again:
```
./gradlew clean build
```

- If the **retry succeeds**: print `"✅ Build passed on retry. The first failure was likely a flaky test."` and proceed to Phase 6.
- If the **retry fails**: print `"❌ ERROR: Build failed on retry. The test failure is consistent — the dependency update broke something."` — show the relevant test failure output and **STOP**.

---

## Phase 6 — Summary

Print a summary of the update session:

```
========================================
📦 Dependency Update — Complete!
========================================

Updated dependencies:
  kotlin              2.3.20  →  2.4.0
  gson                2.13.2  →  2.14.0
  junit               5.14.1  →  5.15.0
  ...

Gradle wrapper:
  gradle              9.4.1   →  9.5.0  (or "unchanged")

Build verification:
  ✅ Passed (first attempt)  — or —
  ✅ Passed (retry — first attempt had flaky test)

⚠️ Changes are NOT committed.
Review the changes and commit when ready.
========================================
```

If no updates were applied (user skipped everything), print:

```
========================================
ℹ️ Dependency Update — No changes made
========================================

All dependencies were either up to date or skipped by user.
========================================
```

---

## Error Handling — Rules

These rules are **non-negotiable** and override any other behavior:

1. **Every command** executed must have its exit code checked
2. **Non-zero exit code = ERROR** — print the error and STOP immediately (unless it is a test failure eligible for retry in Phase 5)
3. **Never continue** to the next step after a failure (except the single retry in Phase 5.3)
4. **Never downgrade** an error to a warning
5. **Always print which phase/step failed** with its number and description
6. **Never commit changes** — leave all changes for the user to review
7. **Maximum one retry** — only for test failures in Phase 5, never for compilation or configuration errors
