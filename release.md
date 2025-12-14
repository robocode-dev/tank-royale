### Releasing Robocode Tank Royale

This document explains how we create a draft pre-release on GitHub and attach platform installers produced by jpackage.
The process keeps developers’ machines simple — all packaging and asset uploads happen in GitHub Actions.

#### What the automated release does

- Builds the project artifacts and sample bots.
- Creates a draft pre-release on GitHub with tag `v<version>`.
- Optionally dispatches a matrix packaging workflow that:
    - Builds native installers via jpackage for Windows, Linux, and macOS.
    - Attaches those installers directly to the same draft release.

The release remains a draft until you manually review and publish it on GitHub.

---

### Prerequisites

- Version is defined in `gradle.properties` as `version=<x.y.z>`.
- You can run from GitHub Actions (recommended) or locally:
    - GitHub Actions: uses `GITHUB_TOKEN` with `contents: write` and `actions: write`.
    - Local (optional): a Personal Access Token with `repo` and `workflow` scopes.

Tip:

- If you run these commands often, it’s easier to store Gradle properties in your user Gradle properties file instead of
  passing them each time with `-P`.
    - macOS/Linux: `~/.gradle/gradle.properties`
    - Windows: `%USERPROFILE%\.gradle\gradle.properties`
    - Example entries:
      ```
      tankRoyaleGitHubToken=ghp_your_personal_access_token
      triggerPackageReleaseWorkflow=true
      ```
    - Then you can omit the corresponding `-P...` flags on the command line. This applies to any property you would
      normally pass with `-P`.
    - Do not commit secrets. The user `gradle.properties` lives outside the repo in your home directory.

---

### 1) Create the draft pre-release

Run the Gradle release task. This creates a draft pre-release on GitHub with tag `v<version>` and uploads the core
artifacts (JARs and sample bots zips):

From GitHub Actions (recommended):

```bash
./gradlew create-release -PtankRoyaleGitHubToken=${{ github.token }}
```

Locally (optional):

```bash
./gradlew create-release -PtankRoyaleGitHubToken=$YOUR_PAT
```

Notes:

- The release is created as `draft: true` and `prerelease: true`.
- The tag `v<version>` is created automatically if it does not already exist.

---

### 2) Trigger packaging and upload installers to the draft release

To build native installers via jpackage and attach them to the same draft release, run the same task with the packaging
trigger flag. The Gradle task will dispatch the packaging workflow and pass the release version to it.

From GitHub Actions:

```bash
./gradlew create-release \
  -PtankRoyaleGitHubToken=${{ github.token }} \
  -PtriggerPackageReleaseWorkflow=true
```

Locally (optional):

```bash
./gradlew create-release \
  -PtankRoyaleGitHubToken=$YOUR_PAT \
  -PtriggerPackageReleaseWorkflow=true
```

What happens:

- The workflow `.github/workflows/package-release.yml` runs on Windows, Linux, and macOS runners.
- Each job builds installers and then uses `softprops/action-gh-release` to upload files from `build/dist/**/*` to the
  release with tag `v<version>`.
- The workflow also uploads the same files as workflow artifacts for debugging.

---

### 3) Review and publish the release

1. Open the draft release on GitHub.
2. Verify that JARs, sample bot zips, and all platform installers are attached.
3. Update the notes if needed.
4. Click Publish release when you are ready.

---

### Troubleshooting

- If packaging fails on a platform, check the `build/ci-gradle.log` attached as a workflow artifact and as a release
  asset for that job.
- Ensure the packaging workflow has permissions:
    - `permissions: contents: write` (to upload assets)
    - `permissions: actions: read`
- Ensure you passed `-PtriggerPackageReleaseWorkflow=true` when you want installers to be created and attached.
