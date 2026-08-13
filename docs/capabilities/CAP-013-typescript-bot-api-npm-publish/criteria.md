---
id: CAP-013-criteria
type: criteria
status: draft
links: [CAP-013]
title: Acceptance criteria for CAP-013 (typescript-bot-api-npm-publish)
ac-prefix: TNP
provenance: verified
---

```gherkin
Feature: typescript-bot-api-npm-publish — TypeScript Bot API npm publishing

  # Requirement: npm Package Publishing via Gradle
  # The TypeScript bot API Gradle build SHALL provide tasks for publishing the
  # `@robocode.dev/tank-royale-bot-api` package to the npm registry, following the same pattern as
  # the Python bot API's PyPI publishing tasks.
  # The following tasks SHALL be available:
  # - `npmPack` — runs `npm pack` to produce a local `.tgz` tarball for verification and local use
  # - `npmPublish` — publishes to the npm registry; requires the `npmjs-api-key` Gradle property

  # TNP-001 retired (CH-002): the `npmPublishDryRun` task it verified was never built and
  # is not needed — `npmPack` already produces the `.tgz` and shows what would be published
  # without uploading. No replacement minted.
  @TNP-001 @retired
  Scenario: npmPublishDryRun succeeds without credentials
    When `./gradlew :bot-api:typescript:npmPublishDryRun` is run
    Then the task succeeds and prints the files that would be published
    And no package is uploaded to the registry

  # TNP-002 retired (CH-002): the shipped task uses the `npmjs-api-key` Gradle property, not
  # an `NPM_TOKEN` environment variable. Reminted as TNP-005.
  @TNP-002 @retired
  Scenario: npmPublish requires NPM_TOKEN
    When `./gradlew :bot-api:typescript:npmPublish` is run without `NPM_TOKEN` set
    Then the build fails with a clear error message indicating the token is missing

  # TNP-003 retired (CH-002): credential carrier changed from `NPM_TOKEN` to the
  # `npmjs-api-key` Gradle property. Reminted as TNP-006.
  @TNP-003 @retired
  Scenario: npmPublish succeeds with valid token
    When `./gradlew :bot-api:typescript:npmPublish` is run with a valid `NPM_TOKEN`
    Then the package is uploaded to `https://registry.npmjs.org`
    And the temporary `.npmrc` credential file is deleted after the task completes

  @TNP-004
  Scenario: npmrc is not committed to source control
    When the `npmPublish` task writes a temporary `.npmrc` file
    Then `.npmrc` is listed in `.gitignore` and cannot be accidentally committed

  @TNP-005
  Scenario: npmPublish requires the npmjs-api-key Gradle property
    When `./gradlew :bot-api:typescript:npmPublish` is run without `npmjs-api-key` set
    Then the build fails with a clear error message: "npmjs-api-key is not set in gradle.properties"

  @TNP-006
  Scenario: npmPublish succeeds with a valid npmjs-api-key
    When `./gradlew :bot-api:typescript:npmPublish` is run with a valid `npmjs-api-key` Gradle property
    Then the package is published to `https://registry.npmjs.org` with public access
    And the temporary `.npmrc` credential file is deleted after the task completes
```
