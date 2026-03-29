# Release

## Release skill

The recommended way to release is the `/release` skill, available in both Copilot CLI and Claude Code. It automates
the full process: pre-flight checks, artifact publishing, GitHub release creation, and documentation upload.

```
/release
```

The skill validates credentials, verifies you are on `main` with no pending changes, and walks through each step with
clear progress reporting. If any step fails, it stops immediately with an error.

For manual publishing or troubleshooting, see the sections below.

---

## Credentials setup

All Gradle properties go in your **user-level** `gradle.properties` (never commit real values to the repo):

- Windows: `%USERPROFILE%\.gradle\gradle.properties`
- macOS/Linux: `~/.gradle/gradle.properties`

### Maven Central (Sonatype)

| Property | Description | Where to get it |
|----------|-------------|-----------------|
| `ossrhUsername` | Sonatype OSSRH username | Register at [central.sonatype.com](https://central.sonatype.com/) |
| `ossrhPassword` | Sonatype OSSRH password | Same account |
| `signingKey` | GPG private key (armored, newlines replaced with `\n`) | `gpg --gen-key`, then `gpg --armor --export-secret-keys KEY_ID` |
| `signingPassword` | GPG key passphrase | Chosen during key generation |

Publish your public key to a key server so Maven Central can verify signatures:

```shell
gpg --keyserver keyserver.ubuntu.com --send-keys KEY_ID
```

### NuGet

| Property | Description | Where to get it |
|----------|-------------|-----------------|
| `nuget-api-key` | NuGet API key | [nuget.org](https://www.nuget.org/) → API Keys → Create (scope: `robocode.tankroyale.*`) |

### PyPI

Create an API token at [pypi.org](https://pypi.org/) → Account Settings → API Tokens (scope: `robocode-tankroyale-botapi`).

Configure via **one** of these (checked in order):

1. **Gradle property**: `pypiToken=pypi-AgEIcH...` in `~/.gradle/gradle.properties`
2. **Environment variable**: `PYPI_TOKEN=pypi-AgEIcH...`
3. **~/.pypirc file**:
   ```ini
   [distutils]
   index-servers =
       pypi

   [pypi]
   repository: https://upload.pypi.org/legacy/
   username: __token__
   password: pypi-AgEIcH...
   ```

### GitHub CLI

Authenticate with `gh auth login`. If `gh` is not installed, the release skill provides a manual fallback URL.

For the local `create-release` Gradle task, a Personal Access Token is needed instead:

| Property | Description | Where to get it |
|----------|-------------|-----------------|
| `tankRoyaleGitHubToken` | GitHub PAT | [github.com/settings/tokens](https://github.com/settings/tokens) (scopes: `repo`, `workflow`) |

### Complete `~/.gradle/gradle.properties` example

```properties
# Maven Central (Sonatype)
ossrhUsername=your-sonatype-username
ossrhPassword=your-sonatype-password

# GPG signing
signingKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n\nlQWGBGBq5...\n-----END PGP PRIVATE KEY BLOCK-----
signingPassword=your-gpg-passphrase

# NuGet
nuget-api-key=oy2abc123...

# PyPI
pypiToken=pypi-AgEIcH...

# GitHub (for local create-release Gradle task)
tankRoyaleGitHubToken=ghp_abc123...
```

---

## Manual publishing commands

### Java (Maven Central)

Publish to staging, close, and release:

```shell
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
```

Or publish to staging only (for manual review before release):

```shell
./gradlew publishToSonatype
```

### .NET (NuGet)

Dry run (shows what would be executed):

```shell
.\scripts\release\publish-nuget.ps1
```

Publish (prompts for confirmation):

```shell
.\scripts\release\publish-nuget.ps1 -Execute
```

Publish without confirmation (for automated workflows):

```shell
.\scripts\release\publish-nuget.ps1 -Execute -Force
```

Manual alternative:

```shell
cd bot-api\dotnet\api\bin\Release
dotnet nuget push robocode.tankroyale.botapi.«version».nupkg --api-key «key» --source https://api.nuget.org/v3/index.json
```

### Python (PyPI)

Build the wheel, then upload:

```shell
./gradlew :bot-api:python:upload-testpypi   # test first
./gradlew :bot-api:python:upload-pypi        # production
```

---

## GitHub release creation

The `create-release` workflow creates a draft pre-release on GitHub with tag `v<version>`, builds the project artifacts
and sample bots, then dispatches a packaging workflow that builds native installers via jpackage for Windows, Linux, and
macOS and attaches them to the draft release.

The release skill triggers this automatically. To run manually:

From GitHub Actions (recommended):

```shell
./gradlew create-release -PtankRoyaleGitHubToken=${{ github.token }}
```

Locally:

```shell
./gradlew create-release -PtankRoyaleGitHubToken=$YOUR_PAT
```

To also trigger native installer packaging:

```shell
./gradlew create-release \
  -PtankRoyaleGitHubToken=$YOUR_PAT \
  -PtriggerPackageReleaseWorkflow=true
```

The release remains a draft until you manually review and publish it on GitHub:

1. Open the draft release on GitHub.
2. Verify that JARs, sample bot zips, and all platform installers are attached.
3. Update the notes if needed.
4. Click **Publish release**.

---

## Troubleshooting

- If packaging fails on a platform, check `build/ci-gradle.log` attached as a workflow artifact.
- Ensure the packaging workflow has permissions: `contents: write` and `actions: read`.
- Ensure you passed `-PtriggerPackageReleaseWorkflow=true` when you want installers created.
