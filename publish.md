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
cd bot-api\dotnet\Robocode.TankRoyale.BotApi\bin\Release
```

Publish to Nuget:

Note:  «version» and «nuget api key» must be prefilled with Robocode version and Nuget API key.

```
dotnet nuget push robocode.tankroyale.botapi.«version».nupkg --api-key «nuget api key» --source https://api.nuget.org/v3/index.json
```