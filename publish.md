# Publish archives

## Java archives (jar files)

Publish jar archives to Sonatype (Maven Central Repository)

Make sure you have a `.gradle/gradle.properties` file in your home folder (`%USERHOME%` on Windows, and `~` on Linux and macOS):

```properties
signingKey=-----BEGIN PGP PRIVATE KEY BLOCK-----\n\nlQWGBGBq5...-----END PGP PRIVATE KEY BLOCK-----
signingPassword=...

ossrhUsername=...
ossrhPassword=JfRES...
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

### Using the automated script (recommended)

The script reads the version from `gradle.properties` and the API key from `nuget-api-key` property.

To preview the publish command (dry run):

```shell
.\scripts\release\publish-nuget.ps1
```

To actually publish to NuGet (requires confirmation):

```shell
.\scripts\release\publish-nuget.ps1 -Execute
```

Note: Make sure to set the real `nuget-api-key` in `~/.gradle/gradle.properties` before publishing.

### Manual publishing

Alternatively, you can publish manually:

Enter the release folder:

```shell
cd bot-api\dotnet\api\bin\Release
```

Publish to Nuget:

```shell
dotnet nuget push robocode.tankroyale.botapi.«version».nupkg --api-key «nuget api key» --source https://api.nuget.org/v3/index.json
```

Note: Replace «version» and «nuget api key» with the actual Robocode version and Nuget API key.

## Python package

Make sure you have a `.pypyrc` file in your home folder (`%USERHOME%` on Windows, and `~` on Linux and macOS):

```ini
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

Upload to the real PyPI:

```shell
./gradlew :bot-api:python:upload-pypi
```
