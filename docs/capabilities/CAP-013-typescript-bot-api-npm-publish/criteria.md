---
id: CAP-013-criteria
type: criteria
status: draft
links: [CAP-013]
title: Acceptance criteria for CAP-013 (typescript-bot-api-npm-publish)
ac-prefix: TNP
provenance: inferred
---

```gherkin
Feature: typescript-bot-api-npm-publish — TypeScript Bot API npm publishing

  # Requirement: npm Package Publishing via Gradle
  # The TypeScript bot API Gradle build SHALL provide tasks for publishing the
  # `@robocode.dev/tank-royale-bot-api` package to the npm registry, following the same pattern as
  # the Python bot API's PyPI publishing tasks.
  # The following tasks SHALL be available:
  # - `npmPack` — runs `npm pack` to produce a local `.tgz` tarball for verification and local use
  # - `npmPublishDryRun` — validates the package with `npm publish --dry-run` without uploading
  # - `npmPublish` — publishes to the npm registry; requires `NPM_TOKEN` environment variable

  @TNP-001
  Scenario: npmPublishDryRun succeeds without credentials
    When `./gradlew :bot-api:typescript:npmPublishDryRun` is run
    Then the task succeeds and prints the files that would be published
    And no package is uploaded to the registry

  @TNP-002
  Scenario: npmPublish requires NPM_TOKEN
    When `./gradlew :bot-api:typescript:npmPublish` is run without `NPM_TOKEN` set
    Then the build fails with a clear error message indicating the token is missing

  @TNP-003
  Scenario: npmPublish succeeds with valid token
    When `./gradlew :bot-api:typescript:npmPublish` is run with a valid `NPM_TOKEN`
    Then the package is uploaded to `https://registry.npmjs.org`
    And the temporary `.npmrc` credential file is deleted after the task completes

  @TNP-004
  Scenario: npmrc is not committed to source control
    When the `npmPublish` task writes a temporary `.npmrc` file
    Then `.npmrc` is listed in `.gitignore` and cannot be accidentally committed
```
