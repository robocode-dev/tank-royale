# Publish archives

## Java archives (jar files)

### Publish jar archives to Sonatype (Maven Central Repository)

Publish to staging:

```shell
./gradlew publishToSonatype
```

Publish to staging, close, and release:

```shell
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
```

## .Net archives (Nuget)

Enter the release folder:

```shell
cd bot-api\dotnet\api\bin\Release
```

Publish to Nuget:

```
dotnet nuget push robocode.tankroyale.botapi.«version».nupkg --api-key «nuget api key» --source https://api.nuget.org/v3/index.json
```

Note: «version» and «nuget api key» must be prefilled with Robocode version and Nuget API key.

## Python package (pip)

From repository root, build the Python Bot API package (wheel + sdist):

```
./gradlew :bot-api:python:build-dist
```

This will:
- Generate schema sources into bot-api/python/generated
- Generate the VERSION file from gradle.properties
- Build dist/*.whl and dist/*.tar.gz inside bot-api/python/dist

Install the built wheel from anywhere on your system:

```
pip install path\to\tank-royale\bot-api\python\dist\robocode_tank_royale-<version>-py3-none-any.whl
```

Alternatively, to install directly from the project folder without building first (editable for development):

```
cd bot-api\python
pip install -e .
```
