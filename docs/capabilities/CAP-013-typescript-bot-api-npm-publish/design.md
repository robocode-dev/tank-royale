---
id: CAP-013-design
type: design
status: draft
links: [CAP-013, P-002]
title: Design notes for CAP-013 (typescript-bot-api-npm-publish)
provenance: inferred
---

# CAP-013 design

Carried over from the in-flight OpenSpec change `add-typescript-bot-api-npm-publish` at CH-001 (tracked as [P-002](../../plans/P-002-typescript-bot-api-npm.md), door M-004).

The TypeScript Bot API package (`@robocode.dev/tank-royale-bot-api`) builds but cannot be published to the npm registry through Gradle, while Java (Maven Central) and Python (PyPI) both publish via Gradle tasks. The intended shape:

- `npmPack` task in `bot-api/typescript/build.gradle.kts` (also needed by sample-bot startup work)
- `npmPublishDryRun` task — `npm publish --dry-run` validates the package without uploading
- `npmPublish` task — publishes to npm; requires the `NPM_TOKEN` environment variable

Impact beyond the build script: `.gitignore` must exclude `.npmrc` (a temporary credential file is written and deleted during publish).
