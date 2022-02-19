# Bot API for .Net

This directory contains the Bot API for developing bots for Robocode Tank Royale with the .Net platform.

The Bot API is provided as a [Nuget] package and build for [.Net 5.0].

## Directory structure

- `src` contains all the source files for the API.
- `docfx_project` contains the [DocFX] files for providing the [API documentation].
- `docs` contains the document files used for the Nuget package.
    - ***Remember to update*** the `/docs/Releases-notes.txt` before pushing NuGet package to nuget.org

## Build commands

#### Delete build directories:

    gradle clean

#### Build Nuget package:

    gradle build

Artifact is output to the `/bin/Release/` directory named `Robocode.TankRoyale.BotApi.x.y.z.nupkg`.

### Build API documentation

    gradle docfx

[DocFX] documentation will be generated to the /docfx_project/_site directory.

#### Viewing the API documentation

You run and view the documentation by running the [docfx] command (must be pre-installed) standing in
the `docfx_project` directory:

    docfx serve _site

You can then view the documentation with the browser on http://localhost:8080

### Push Nuget package to local repository:

    gradle pushLocal

This will push the nuget package to `%USERPROFILE%/.nuget/packages` (Windows) or `~/.nuget/packages` (macOS and Linux).

Note that this command make use of the [dotnet] command, which must be pre-installed.

In order to use the local Nuget package (in a bot project) you need to update the [NuGet.config] file and add this key
inside the `<packageSources>` section:

Windows:

    <add key="local" value="%USERPROFILE%\.nuget\packages"/>

macOS and Linux:

    <add key="local" value="~/.nuget/packages"/>


[.Net 5.0]: https://dotnet.microsoft.com/en-us/download/dotnet/5.0 "Download .NET 5.0"

[Nuget]: https://www.nuget.org/ "Nuget homepage"

[DocFX]: https://dotnet.github.io/docfx/ "DocFX site"

[docfx]: https://github.com/dotnet/docfx/releases "docfx command"

[dotnet]: https://docs.microsoft.com/en-us/dotnet/core/tools/dotnet "dotnet command"

[API documentation]: https://robocode.dev/tankroyale/api/dotnet/ "API documentation"

[NuGet.config]: https://docs.microsoft.com/en-us/nuget/consume-packages/configuring-nuget-behavior "NuGet configuration"