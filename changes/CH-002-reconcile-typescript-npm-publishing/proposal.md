---
id: CH-002
type: change
status: open
links: [P-002, M-004, CAP-013]
title: Reconcile the corpus with the shipped TypeScript npm-publishing work
---

# CH-002 — Reconcile the corpus with the shipped TypeScript npm-publishing work

## What

Digest the already-shipped TypeScript Bot API npm-publishing feature into the corpus.
The feature is built and live; the corpus still describes the pre-implementation
OpenSpec intent, which diverged from what shipped. This is a **docs-only** change that
reconciles P-002/M-004 and CAP-013 to reality.

## Why

The npm-publishing work landed before the Cliewen extraction (CH-001) and was never
digested. Evidence it shipped:

- `npmPack` and `npmPublish` Gradle tasks exist in `bot-api/typescript/build.gradle.kts`.
- The release skill (`.agents/skills/release/SKILL.md`) invokes
  `./gradlew :bot-api:typescript:npmPublish` and checks for `npmjs-api-key`.
- `@robocode.dev/tank-royale-bot-api@1.0.2` is published on npm (last modified
  2026-05-18).
- `.npmrc` is git-ignored (`.gitignore:28`).

`clue validate` is green because the drift is semantic, not structural — a milestone
claiming `todo` for done work, and acceptance criteria describing a task and credential
mechanism that were never built. Left unreconciled, the corpus lies about the system.

## Divergences being reconciled (decided with the maintainer)

1. **`M-004` claims `todo`** but the tasks shipped and the package is live → `done`.
2. **`M-004` exit criterion references `CAP-014`**, which does not exist → `CAP-013`.
3. **`npmPublishDryRun` task** required by the spec (`TNP-001`, `M-004`, design) was
   **never built and is not needed**: `npmPack` already produces the `.tgz` and shows
   what would be published without uploading, so a dry-run task is redundant. Removed
   from the spec.
4. **Credential mechanism**: the spec requires an `NPM_TOKEN` env var; the shipped code
   uses a `npmjs-api-key` Gradle property (consistent with the release skill and the
   Java/Sonatype + Python/PyPI publishing pattern). The spec is reconciled to reality.

## Scope boundary

- **No code changes.** The shipped `build.gradle.kts` is the source of truth we
  reconcile to.
- **CAP-013 stays `status: draft`.** Promotion to `active` requires wired TNP tests,
  which is the M-002 door (extracted criteria are exempt from the AC↔test wall while
  draft). Flipping it now would be out of scope. This is intentional, not a remaining
  loose end.

## AC changes

- Retire `TNP-001` (npmPublishDryRun) as a tombstone — the task it verifies was
  dropped. Draft + inferred, never verified, no tests, no dependents.
- `TNP-002` / `TNP-003` describe an `NPM_TOKEN` env var that does not match the shipped
  `npmjs-api-key` Gradle property. The credential carrier is part of what these
  scenarios assert, so per clue-delta they are **retired and reminted** (`TNP-005` /
  `TNP-006`) rather than redefined in place.
- `TNP-004` (`.npmrc` git-ignored) is unchanged — already true.
