---
description: Release Tank Royale artifacts to Maven Central, NuGet, and PyPI, then trigger the GitHub Actions create-release workflow. Use when the user runs /release to publish a new version.
argument-hint: ""
allowed-tools: Read, Bash
version: 1.0.0
authors: Flemming N. Larsen (https://github.com/flemming-n-larsen)
---

# Release

You are executing the Tank Royale release workflow. Follow these phases exactly, in order. **If ANY step fails (non-zero exit code), STOP immediately with an ERROR — never continue to the next step, and never downgrade errors to warnings.**

Announce each step clearly before executing it.

## Phase 1 — Pre-flight Checks

### 1.1 — Verify branch

Run `git branch --show-current` and confirm the output is exactly `main`.

- If the branch is **not** `main`: print `"❌ ERROR: Release must be run from the 'main' branch. Current branch: <branch>"` and **STOP**.
- If on `main`: print `"✅ Branch: main"` and continue.

### 1.2 — Verify clean working tree and no unpushed commits

Run `git status --porcelain` and check the output.

- If there is **any output** (uncommitted or untracked changes): print `"❌ ERROR: Working tree is not clean. Please commit or stash all changes before releasing."` followed by the output, and **STOP**.
- If the output is empty: print `"✅ Working tree: clean"`.

Then run `git fetch origin main --quiet` followed by `git rev-list HEAD..origin/main --count` and `git rev-list origin/main..HEAD --count`.

- If the local branch is **behind** remote (first count > 0): print `"❌ ERROR: Local 'main' is behind 'origin/main'. Please pull before releasing."` and **STOP**.
- If the local branch is **ahead** of remote (second count > 0): print `"❌ ERROR: Local 'main' has unpushed commits. Please push before releasing."` and **STOP**.
- If both counts are 0: print `"✅ Branch is up to date with origin/main"`.

### 1.3 — Verify repository root

Confirm that `build.gradle.kts` and `gradle.properties` exist in the current working directory.

- If either is missing: print `"❌ ERROR: Not at the repository root. Please cd to the Tank Royale repository root."` and **STOP**.

### 1.4 — Read and display version

Read `gradle.properties` and extract the `version=X.Y.Z` value. Parse the major, minor, and patch components.

Print:
```
📋 Release version: X.Y.Z
   Major: X | Minor: Y | Patch: Z
```

Determine the release type:
- If **patch is 0** → this is a **major/minor release** (documentation will be uploaded)
- If **patch > 0** → this is a **patch release** (documentation upload will be skipped)

Print the release type:
- `"📋 Release type: Major/Minor release (documentation will be uploaded)"` — or —
- `"📋 Release type: Patch release (documentation upload will be skipped)"`

### 1.5 — Verify version matches CHANGELOG.md

Read `CHANGELOG.md` and find the **first** line matching the pattern `## [X.Y.Z]` (the topmost version heading).

- If the version in that heading does **not** match the version from `gradle.properties`: print `"❌ ERROR: Version mismatch — gradle.properties has X.Y.Z but CHANGELOG.md top entry is A.B.C. Please update CHANGELOG.md or gradle.properties."` and **STOP**.
- If they match: print `"✅ Version X.Y.Z matches CHANGELOG.md"`.

### 1.6 — Detect platform

Determine whether you are running on Windows or Unix/macOS. This affects the Gradle wrapper command:
- **Windows**: use `.\gradlew.bat`
- **Unix/macOS**: use `./gradlew`

Print: `"📋 Platform: Windows"` or `"📋 Platform: Unix/macOS"`

---

## Phase 2 — Publish Artifacts

### Step 1 of 3 — Publish Java artifacts to Maven Central

Print: `"📦 Step 1/3: Publishing Java artifacts to Maven Central..."`

Run the Gradle command (use the platform-appropriate wrapper):
```
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
```

- If the command **succeeds**: print `"✅ Step 1/3: Java artifacts published to Maven Central"`.
- If the command **fails**: print `"❌ ERROR: Step 1/3 failed — Java publish to Maven Central failed"` and **STOP**.

### Step 2 of 3 — Publish .NET package to NuGet

Print: `"📦 Step 2/3: Publishing .NET package to NuGet..."`

Run:
```
.\scripts\release\publish-nuget.ps1 -Execute -Force
```

Note: The `-Force` flag skips the interactive YES confirmation prompt.

- If the command **succeeds**: print `"✅ Step 2/3: .NET package published to NuGet"`.
- If the command **fails**: print `"❌ ERROR: Step 2/3 failed — .NET publish to NuGet failed"` and **STOP**.

### Step 3 of 3 — Publish Python package to PyPI

Print: `"📦 Step 3/3: Publishing Python package to PyPI..."`

Run the Gradle command (use the platform-appropriate wrapper):
```
./gradlew :bot-api:python:upload-pypi
```

- If the command **succeeds**: print `"✅ Step 3/3: Python package published to PyPI"`.
- If the command **fails**: print `"❌ ERROR: Step 3/3 failed — Python publish to PyPI failed"` and **STOP**.

---

## Phase 3 — Create GitHub Release

Print: `"🚀 Step 4: Triggering create-release GitHub Actions workflow on main..."`

First check if the `gh` CLI is available by running `gh --version`.

If `gh` is available, run:
```
gh workflow run create-release.yml --ref main
```

- If the command **succeeds**: print `"✅ Step 4: create-release workflow triggered successfully"`.
- If the command **fails**: print `"❌ ERROR: Step 4 failed — could not trigger create-release workflow"` and **STOP**.

If `gh` is **not** available:
- Print `"⚠️ GitHub CLI (gh) is not installed. Please trigger the workflow manually:"`
- Print `"   https://github.com/robocode-dev/tank-royale/actions/workflows/create-release.yml"`
- Print `"   Click 'Run workflow' → select 'main' branch → click 'Run workflow'"`
- Continue to the next step (this is the only step that allows a manual fallback).

---

## Phase 4 — Upload Documentation (Conditional)

Check the patch version from Phase 1.

### If patch is 0 (major/minor release):

Print: `"📚 Step 5: Uploading documentation (major/minor release — patch is 0)..."`

Run the Gradle command (use the platform-appropriate wrapper):
```
./gradlew upload-docs
```

- If the command **succeeds**: print `"✅ Step 5: Documentation uploaded successfully"`.
- If the command **fails**: print `"❌ ERROR: Step 5 failed — documentation upload failed"` and **STOP**.

### If patch > 0 (patch release):

Print: `"ℹ️ Step 5: Skipping documentation upload (patch release — patch version is Z, not 0)"`

---

## Phase 5 — Release Summary

Print a summary of the release:

```
========================================
🎉 Release X.Y.Z — Complete!
========================================

Published artifacts:
  ✅ Maven Central  — Java artifacts
  ✅ NuGet          — .NET package
  ✅ PyPI           — Python package

GitHub release:
  ✅ create-release workflow triggered (or manual fallback)

Documentation:
  ✅ Uploaded (or ℹ️ Skipped for patch release)

Next steps:
  1. Monitor the create-release workflow: https://github.com/robocode-dev/tank-royale/actions/workflows/create-release.yml
  2. Once the workflow completes, review the draft release on GitHub
  3. Verify platform installers are attached (Windows, Linux, macOS)
  4. Publish the release when ready
========================================
```

---

## Error Handling — Rules

These rules are **non-negotiable** and override any other behavior:

1. **Every command** executed must have its exit code checked
2. **Non-zero exit code = ERROR** — print the error and STOP immediately
3. **Never continue** to the next step after a failure
4. **Never downgrade** an error to a warning
5. **Always print which step failed** with its step number and description
6. **Partial releases are expected** — if step 2 fails, step 1 artifacts are already published. This is normal and acceptable. Do not attempt rollback.
