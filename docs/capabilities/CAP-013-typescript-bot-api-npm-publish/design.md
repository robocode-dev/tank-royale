---
id: CAP-013-design
type: design
status: draft
links: [CAP-013, P-002]
title: Design notes for CAP-013 (typescript-bot-api-npm-publish)
provenance: verified
---

# CAP-013 design

Originated as the in-flight OpenSpec change `add-typescript-bot-api-npm-publish`, carried into the corpus at CH-001 (tracked as [P-002](../../plans/P-002-typescript-bot-api-npm.md), door M-004) and reconciled to the shipped implementation at CH-002.

The TypeScript Bot API package (`@robocode.dev/tank-royale-bot-api`) builds but originally could not be published to the npm registry through Gradle, while Java (Maven Central) and Python (PyPI) both publish via Gradle tasks. As shipped in `bot-api/typescript/build.gradle.kts`:

- `npmPack` task — runs `npm pack` to produce a local `.tgz` tarball for verification and local use. This also covers pre-publish inspection: it shows exactly what would be published without uploading, so a separate `npm publish --dry-run` task was deemed redundant and not built.
- `npmPublish` task — publishes to npm with `--access public`; requires the `npmjs-api-key` Gradle property (looked up from the user or project `gradle.properties`), consistent with the Sonatype/PyPI credential pattern and the `/release` skill. It writes a temporary `.npmrc` with the token and deletes it afterwards.

Impact beyond the build script: `.gitignore` excludes `/bot-api/typescript/.npmrc` (the temporary credential file written and deleted during publish).

## Credential setup

`npmPublish` reads the npm token from the `npmjs-api-key` Gradle property. Keep it out of the repo by putting it in your **user** Gradle properties file, never the project one:

- Unix/macOS: `~/.gradle/gradle.properties`
- Windows: `%USERPROFILE%\.gradle\gradle.properties`

```properties
# npm token (Automation type) from https://www.npmjs.com/settings/<user>/tokens
npmjs-api-key=npm_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

The token must have publish rights to the `@robocode.dev` scope. If it is missing, `npmPublish` fails with `npmjs-api-key is not set in gradle.properties` (criterion TNP-005). This mirrors the other publish credentials, which live in the same file: `ossrhUsername` / `ossrhPassword` (Maven Central), `nuget-api-key` (NuGet), `pypiToken` (PyPI) — see the `/release` skill for the full set.
