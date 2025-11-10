# Publish archives

## Java archives (jar files)

Publish jar archives to Sonatype (Maven Central Repository)

Make sure you have a `.gradle/gradle.properties` file in your home folder (`%USERHOME%` on Windows, and `~` on Linux and macOS):

```properties
signingKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n\nlQWGBGBq5...-----END PGP PRIVATE KEY BLOCK-----
signingPassword=...

ossrhUsername=...
ossrhPassword=JfRES...

tankRoyaleGitHubToken=ghp_lDygeO...
```

Publish to staging:

```shell
./gradlew publishToSonatype
```

Publish to staging, close, and release in one go:

```shell
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
```

## .NET archives (Nuget)

Enter the release folder:

```shell
cd bot-api\dotnet\api\bin\Release
```

Publish to Nuget:

```
dotnet nuget push robocode.tankroyale.botapi.«version».nupkg --api-key «nuget api key» --source https://api.nuget.org/v3/index.json
```

Note: «version» and «nuget api key» must be prefilled with Robocode version and Nuget API key.

## Python package

Make sure you have a `.pypyrc` file in your home folder (`%USERHOME%` on Windows, and `~` on Linux and macOS):

```toml
[distutils]
index-servers =
testpypi
pypi

[testpypi]
repository: https://test.pypi.org/legacy/
username: __token__
password: pypi-AgENdG...

[pypi]
repository: https://upload.pypi.org/legacy/
username: __token__
password: pypi-AgEIcH...
```

You need to create an API key for both TestPyPI and PyPI, and copy and paste each one into the password field for
`testpypi` and `pypi`, respectively.

From repository root, build the Python Bot API package (wheel):

```shell
./gradlew :bot-api:python:build-dist
```

Upload the TestPyPI first:

```shell
./gradlew :bot-api:python:upload-testpypi
```

Upload the PyPI first:

```shell
./gradlew :bot-api:python:upload-pypi
```