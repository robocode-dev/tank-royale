# README for Nuget package

## About

This package contains the Bot API for developing bots for Robocode Tank Royale with the .Net platform.
This package has been built for [.Net 6.0](https://dotnet.microsoft.com/en-us/download/dotnet/6.0)

You read about the Bot API [here](https://robocode-dev.github.io/tank-royale/api/dotnet/).

## How to use

You need to add this package to your .Net based bot project:

```shell
dotnet add package Robocode.TankRoyale.BotApi
```

This installs the newest available version of the Bot API for Robocode Tank Royale. You can install a specific version
by adding the `--version` option with the specific version:

```shell
dotnet add package Robocode.TankRoyale.BotApi --version @VERSION@
```

The [My First Bot](https://robocode-dev.github.io/tank-royale/tutorial/dotnet/my-first-bot-for-dotnet.html) tutorial
shows how to create a bot and how this package is being used.