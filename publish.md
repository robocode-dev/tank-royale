# Publish archives

## Configure credentials in ~/.gradle/gradle.properties

To avoid environment variables, keep all publishing secrets in your user Gradle properties file (`%USERPROFILE%\ .gradle\gradle.properties` on Windows, `~/.gradle/gradle.properties` on Linux/macOS):

```properties
# Java signing + Sonatype (Maven Central)
signingKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n...\n-----END PGP PRIVATE KEY BLOCK-----
signingPassword=your-signing-passphrase
ossrhUsername=your-ossrh-username
ossrhPassword=your-ossrh-password

# GitHub token used by create-release
tankRoyaleGitHubToken=ghp_...

# NuGet (.NET Bot API)
nugetApiKey=your-nuget-api-key

# PyPI/TestPyPI (Python Bot API) — tokens only
pypiToken=pypi-xxxxxxxxxxxxxxxxxxxxxxxxxxxx
testpypiToken=pypi-xxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

## Java archives (jar files)

Publish jar archives to Sonatype (Maven Central Repository):

```shell
./gradlew publishToSonatype
```

Publish to staging, close, and release in one go:

```shell
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
```

Or using the convenience task from the root project:

```shell
./gradlew publish-maven-central
```

## .NET archives (NuGet)

From the repository root, create and publish the .NET Bot API package to NuGet.org (uses `nugetApiKey` from your Gradle properties):

```shell
./gradlew publish-nuget
```

## Python package

From repository root, build the Python Bot API package (wheel):

```shell
./gradlew :bot-api:python:build-dist
```

Upload to TestPyPI (tokens-only: uses `testpypiToken` from Gradle properties):

```shell
./gradlew publish-testpypi
```

Upload to PyPI (tokens-only: uses `pypiToken` from Gradle properties):

```shell
./gradlew publish-pypi
```