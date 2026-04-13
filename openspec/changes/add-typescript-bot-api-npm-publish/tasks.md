## 1. npm Pack Task

- [ ] 1.1 Add `npmPack` Gradle task to `bot-api/typescript/build.gradle.kts` (skip if already added by `add-typescript-sample-bot-startup-deps`)

## 2. Publish Tasks

- [ ] 2.1 Add `npmPublishDryRun` task that runs `npm publish --dry-run --access public` (no credentials needed)
- [ ] 2.2 Add `npmPublish` task that:
  - Reads `NPM_TOKEN` env var; throws `GradleException` if unset
  - Writes a temporary `.npmrc` file with the auth token before publishing
  - Runs `npm publish --access public`
  - Deletes the temporary `.npmrc` file in `doLast` (and on failure via `finalizedBy`)

## 3. Credential Safety

- [ ] 3.1 Verify `.npmrc` is listed in `.gitignore` at the `bot-api/typescript/` level or repo root
- [ ] 3.2 Add it if missing
