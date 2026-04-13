# Change: Add Gradle Tasks for TypeScript Bot API npm Publishing

## Why

The TypeScript bot API package (`@robocode.dev/tank-royale-bot-api`) is built but cannot be
published to the npm registry via Gradle. Java uses Maven Central and Python uses PyPI — both
driven by Gradle tasks. TypeScript has no equivalent, leaving it unpublishable through the
standard project build workflow.

## What Changes

- Add `npmPack` task to `bot-api/typescript/build.gradle.kts` (also needed by sample bot startup change)
- Add `npmPublishDryRun` task — runs `npm publish --dry-run` to validate the package without uploading
- Add `npmPublish` task — publishes to the npm registry; requires `NPM_TOKEN` environment variable

## Impact

- Affected specs: `typescript-bot-api`
- Affected code:
  - `bot-api/typescript/build.gradle.kts` — three new Gradle tasks
  - `.gitignore` — verify `.npmrc` is excluded (temp credential file written/deleted during publish)
