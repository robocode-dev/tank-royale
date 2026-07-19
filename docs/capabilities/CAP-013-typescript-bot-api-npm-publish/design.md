---
id: CAP-013-design
type: design
status: draft
links: [CAP-013, P-002]
title: Design notes for CAP-013 (typescript-bot-api-npm-publish)
provenance: inferred
---

# CAP-013 design

Originated as the in-flight OpenSpec change `add-typescript-bot-api-npm-publish`, carried into the corpus at CH-001 (tracked as [P-002](../../plans/P-002-typescript-bot-api-npm.md), door M-004) and reconciled to the shipped implementation at CH-002.

The TypeScript Bot API package (`@robocode.dev/tank-royale-bot-api`) builds but originally could not be published to the npm registry through Gradle, while Java (Maven Central) and Python (PyPI) both publish via Gradle tasks. As shipped in `bot-api/typescript/build.gradle.kts`:

- `npmPack` task — runs `npm pack` to produce a local `.tgz` tarball for verification and local use. This also covers pre-publish inspection: it shows exactly what would be published without uploading, so a separate `npm publish --dry-run` task was deemed redundant and not built.
- `npmPublish` task — publishes to npm with `--access public`; requires the `npmjs-api-key` Gradle property (looked up from the user or project `gradle.properties`), consistent with the Sonatype/PyPI credential pattern and the `/release` skill. It writes a temporary `.npmrc` with the token and deletes it afterwards.

Impact beyond the build script: `.gitignore` excludes `/bot-api/typescript/.npmrc` (the temporary credential file written and deleted during publish).
