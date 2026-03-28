## ADDED Requirements

### Requirement: npm Package Publishing via Gradle

The TypeScript bot API Gradle build SHALL provide tasks for publishing the
`@robocode.dev/tank-royale-bot-api` package to the npm registry, following the same pattern as
the Python bot API's PyPI publishing tasks.

The following tasks SHALL be available:

- `npmPack` — runs `npm pack` to produce a local `.tgz` tarball for verification and local use
- `npmPublishDryRun` — validates the package with `npm publish --dry-run` without uploading
- `npmPublish` — publishes to the npm registry; requires `NPM_TOKEN` environment variable

#### Scenario: npmPublishDryRun succeeds without credentials

- **WHEN** `./gradlew :bot-api:typescript:npmPublishDryRun` is run
- **THEN** the task succeeds and prints the files that would be published
- **AND** no package is uploaded to the registry

#### Scenario: npmPublish requires NPM_TOKEN

- **WHEN** `./gradlew :bot-api:typescript:npmPublish` is run without `NPM_TOKEN` set
- **THEN** the build fails with a clear error message indicating the token is missing

#### Scenario: npmPublish succeeds with valid token

- **WHEN** `./gradlew :bot-api:typescript:npmPublish` is run with a valid `NPM_TOKEN`
- **THEN** the package is uploaded to `https://registry.npmjs.org`
- **AND** the temporary `.npmrc` credential file is deleted after the task completes

#### Scenario: npmrc is not committed to source control

- **WHEN** the `npmPublish` task writes a temporary `.npmrc` file
- **THEN** `.npmrc` is listed in `.gitignore` and cannot be accidentally committed
