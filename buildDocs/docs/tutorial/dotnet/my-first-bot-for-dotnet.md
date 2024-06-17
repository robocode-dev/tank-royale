# My First Bot for .Net

## Introduction

This tutorial is meant for the .Net platform and a continuation of [My First Bot](../my-first-bot.md) tutorial.

The programming language used in this tutorial is [C#], which is a very popular programming language for .Net. But other
.Net programming languages like [Visual Basic] and [F#] can be used as well.

This tutorial assumes you are already familiar with basic [C#] and [.Net] programming. But this tutorial should suit
well for practising your skills programming for .Net in C# by making a bot for Robocode.

## Programming

### .Net API

The documentation of the .Net API for Robocode Tank Royale is available on [this page](../../api/apis.md).

### Create a source file

Inside your bot directory (`../bots/MyFirstBot`) you need to create a C# source file named `MyFirstBot.cs`. You can edit
that file using a text editor of your choice, or an [IDE] like e.g. [Visual Studio Code] or [Visual Studio].

### Initial code

The initial skeleton of your bot could look like this:

```csharp
using Robocode.TankRoyale.BotApi;

public class MyFirstBot : Bot
{
}
```

The class in this example (`MyFirstBot`) is inherited from the [Bot] class from the `Robocode.TankRoyale.BotApi`
namespace provides methods for controlling the bot but also makes it possible to receive events from the game. The API
is taking care of the communication with the server behind the scene.

### Startup / Main entry

The next thing we need to do is to declare a [Main] method for our bot. The bot will run like an ordinary application,
and hence the [Main] method is the entry point of the bot.

```csharp
    // The main method starts our bot
    static void Main(string[] args)
    {
        new MyFirstBot().Start();
    }

    // Constructor, which loads the bot config file
    MyFirstBot() : base(BotInfo.FromFile("MyFirstBot.json")) { }
```

The [Main] method in this example simply calls the [Start] method of the bot, which will let the bot startup reading
configuration and start communicating with the server.

The bot will attempt to _join_ the server and wait for a signal to engage in a new battle, where one or multiple
instances of this bot must participate.

The constructor of `MyFirstBot` is set up to call the base constructor, which needs an [BotInfo] object containing the
bot configuration. The [BotInfo] class contains a convenient method named [FromFile] which can initialize the [BotInfo]
by reading a JSON file. In this case, it will read the `MyFirstBot.json` file we created earlier, which must be
available within the bot directory (or some other file location accessible for the bot).

Note that it is also possible to provide all the necessary configuration fields programmatically without a file.

### The Run method

When the bot is started by the game, the [Run] method will be called. Hence, your bot should override this method to
provide the logic for the bot when the game is started. The [Run] method should do all required initializing. After
that, it should enter a loop that runs until the game is ended.

```csharp
    // Called when a new round is started -> initialize and do movement
    public override void Run()
    {
        // Repeat while the bot is running
        while (IsRunning)
        {
            Forward(100);
            TurnGunRight(360);
            Back(100);
            TurnGunRight(360);
        }
    }
```

With the code above, the bot will run in a loop, starting by moving forward 100 units. Then it will turn the gun 360°,
move back 100 units and turn the gun 360° again. So the bot will continuously move forward and back all the time and
rotate the gun between moving.

When leaving the [Run] method, the bot will not be able to send new commands each round besides code that runs in event
handlers. Therefore, a loop is used for preventing the [Run] method from exiting. However, we should stop the loop as
soon as the bot is no longer running, and hence need to exit the [Run] method when [IsRunning] becomes `false`.

The [IsRunning] property is a flag maintained by the API. When the bot is told to stop/terminate its execution, the
[IsRunning] property will automatically be set to `false` by the API.

### Event handlers

The [Bot API] provides a lot of event handlers (On<em>"SomeEvent"</em> methods) that are triggered by different types of
events. All event handlers in the Bot API start with the *On*-prefix like e.g. [OnScannedBot]. All event handlers are
available with the [IBot] interface, which the [Bot] class implements.

Talking about the common [OnScannedBot] event handler, we can implement this handler to fire the cannon whenever our bot
scans an opponent bot:

```csharp
    using Robocode.TankRoyale.BotApi.Events;
    ...

    // We saw another bot -> fire!
    public override void OnScannedBot(ScannedBotEvent evt)
    {
        Fire(1);
    }
```

We can also implement another event handler [OnHitByBullet] to let the bot attempt to avoid new bullet hits by turning
the bot perpendicular to the bullet direction:

```csharp
    using Robocode.TankRoyale.BotApi.Events;
    ...

    // We were hit by a bullet -> turn perpendicular to the bullet
    public override void OnHitByBullet(HitByBulletEvent evt)
    {
        // Calculate the bearing to the direction of the bullet
        double bearing = CalcBearing(evt.Bullet.Direction);

        // Turn 90 degrees to the bullet direction based on the bearing
        TurnLeft(90 - bearing);
    }
```

Note that the [Bot API] provides helper methods like [CalcBearing] to ease calculating angles and bearings in the game.

### Putting it all together

Okay, let us put all the parts together in a single source file:

```csharp
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

public class MyFirstBot : Bot
{
    // The main method starts our bot
    static void Main(string[] args)
    {
        new MyFirstBot().Start();
    }

    // Constructor, which loads the bot config file
    MyFirstBot() : base(BotInfo.FromFile("MyFirstBot.json")) { }

    // Called when a new round is started -> initialize and do some movement
    public override void Run()
    {
        // Repeat while the bot is running
        while (IsRunning)
        {
            Forward(100);
            TurnGunRight(360);
            Back(100);
            TurnGunRight(360);
        }
    }

    // We saw another bot -> fire!
    public override void OnScannedBot(ScannedBotEvent evt)
    {
        Fire(1);
    }

    // We were hit by a bullet -> turn perpendicular to the bullet
    public override void OnHitByBullet(HitByBulletEvent evt)
    {
        // Calculate the bearing to the direction of the bullet
        double bearing = CalcBearing(evt.Bullet.Direction);

        // Turn 90 degrees to the bullet direction based on the bearing
        TurnLeft(90 - bearing);
    }
}
```

## Running the bot

Now we got a JSON configuration file and the program for our bot. The next step is to provide the files for running the
bot application. We need to set up a C# project source file ([.csproj]) and scripts for starting the bot.

### C# project source file

So we need to create a `MyFirstBot.csproj` file with the following content:

```xml

<Project Sdk="Microsoft.NET.Sdk">
    <PropertyGroup>
        <RootNamespace>MyFirstBot</RootNamespace>
        <OutputType>Exe</OutputType>
        <TargetFramework>net6.0</TargetFramework>
        <LangVersion>8.0</LangVersion>
    </PropertyGroup>
    <ItemGroup>
        <PackageReference Include="Robocode.TankRoyale.BotApi" Version="0.10.0"/>
    </ItemGroup>
</Project>
```

This [C# project source file][.csproj] is a config file used by the [dotnet] command used for compiling and running the
bot.

Most parts of this file are static content. But these fields need attention:

- `RootNamespace` should contain the exact class name of your bot.
- `TargetFramework` tells what .Net version your bot requires for running.
- `LangVersion` is the C# version
- `Version` for the `PackageReference` for the *Robocode.TankRoyale.BotApi* must use the version of the API the bot is
  built for.

### API available at Nuget

The *Robocode.TankRoyale.BotApi* is available on [Nuget] here:
[https://www.nuget.org/packages/Robocode.TankRoyale.BotApi/](https://www.nuget.org/packages/Robocode.TankRoyale.BotApi/)

You can install the bot API using the [dotnet] command like this:

```
dotnet add package Robocode.TankRoyale.BotApi
```

This installs the newest available version of the Bot API for Robocode Tank Royale. You can install a specific version
by adding the `--version` option with the specific version:

```
dotnet add package Robocode.TankRoyale.BotApi --version 0.10.0
```

### Scripts for starting the bot

The remaining part is to supply some script files for starting up the bot. This will ease starting up the bot from the
command line. But those files are also necessary for booting up the bot from Robocode, which will look out for script
files when examining the bot directory and figure out how to run the bot. The script files tell the [booter] of Robocode
how to start the bot, which is different for each programming language, platform and OS.

With .Net it is possible to run your bot under Windows, macOS, and Linux. Hence, it is a good idea to provide script
files for all these OSes, which mean that we should provide a [command file][.cmd] for Windows, and
a [shell script][.sh] for macOS and Linux.

We create a command file for Windows named `MyFirstBot.cmd` and put it into our bot directory:

```
dotnet run >nul
```

So the `dotnet run` part is used for starting the bot standing in the bot directory from a command prompt.

***IMPORTANT NOTE:*** The `>nul` is a work-around necessary to avoid a Windows-specific quirk where the bot becomes
unresponsive when started up as a process with the Robocode. [^cmd-quirk]

Next, we provide a shell script for macOS and Linux named `MyFirstBot.sh` and put it into our bot directory:

```
#!/bin/sh
dotnet run 
```

Note that we need to set the file permission to grant *read*, *write*, and especially the *execute* right of the script
for the owner and owner´s group, e.g. set the permissions to `775` (`-rwxrwxr-x`). We can do this with this command:

```
chown 775 MyFirstBot.sh
```

Note that the `5` (*read* and *execute*) is set as everybody else than the owner and owner´s group does should not have
the *write* permission per default unless it is explicitly granted by you. ;)

Now you have everything in place to run your bot with Robocode Tank Royale.

Note that the server must be running _locally_ (on your system) when attempting to run the bot locally; otherwise your
bot will fail with an error because it cannot find the server. The server can be started using the Robocode UI.

## Packaging your bot

If you need to package your bot for distribution, you can do this by zip-packing the bot directory. The zip archive
should contain:

- Source file (.cs, .fs, or .vb)
- Project source file (.csproj, .fsproj, or .vbproj)
- Script files (.cmd and .sh)
- JSON config file (.json)

And then you might want to provide a [README] file to provide some information for other people about your bot. :)

You can download the `sample-bots-csharp-x.y.z.zip` file from any [release], which provides a good example of how to
package one to multiple bots into a zip archive.

## Bot Secrets

When you want to run your bot outside the GUI application from a terminal/shell, you will have to supply `bot secrets`
to the bot. The `bot secrets` is one to several keys that is used by the server to allow the bot to access the server.

A server will automatically create a `server.properties` file when it is running. This file will contain generated keys
that must be used by "external" bots and controller for accessing the server. Inside the properties file, you will find
the field `bot-secrets` like this example:

```
bots-secrets=zDuQrkCLQU5VQgytofkNrQ
```

Here the key of `bot-secrets` is `zDuQrkCLQU5VQgytofkNrQ`.

A simple way to set the bot secret for the Java and .Net bot APIs is to set the environment variable `BOT_SECRETS` in
the shell before running the bot:

Mac/Linux bash/shell:

```bash
export VARIABLE_NAME=value
```

Windows command line

```cmd
set BOT_SECRETS=zDuQrkCLQU5VQgytofkNrQ
```

Windows PowerShell:

```powershell
$Env:BOT_SECRETS = zDuQrkCLQU5VQgytofkNrQ
```

Note that it is also possible to provide the server secret and URL programmatically with the Bot APIs with the `Bot`
and `BaseBot` constructors.


[^cmd-quirk]: ? "Note that the unresponsiveness of a Windows process running a bot is not observed when running the
bot directly from the command line with or without the script; only when using a Java process for running the script
inside the Robocode booter."

[C#]: https://docs.microsoft.com/en-us/dotnet/csharp/ "C# documentation"

[F#]: https://docs.microsoft.com/en-us/dotnet/fsharp/ "F# documentation"

[Visual Basic]: https://docs.microsoft.com/en-us/dotnet/visual-basic/ "Visual Basic documentation"

[.Net]: https://dotnet.microsoft.com/en-us/ ".Net homepage"

[IDE]: https://en.wikipedia.org/wiki/Integrated_development_environment "Integrated development environment"

[Visual Studio Code]: https://code.visualstudio.com/ "Visual Studio Code homepage"

[Visual Studio]: https://visualstudio.microsoft.com/ "Visual Studio homepage"

[Bot]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.Bot.html "Bot class"

[Main]: https://docs.microsoft.com/en-us/dotnet/csharp/fundamentals/program-structure/main-command-line "Main() method"

[Start]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.BaseBot.html#Robocode_TankRoyale_BotApi_BaseBot_Start "Start() method"

[BotInfo]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.BotInfo.html "BotInfo class"

[FromFile]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.BotInfo.html#Robocode_TankRoyale_BotApi_BotInfo_FromFile_System_String_ "BotInfo.FromFile() method"

[Run]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.Bot.html#Robocode_TankRoyale_BotApi_Bot_Run "Bot.Run() method"

[IsRunning]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.IBot.html#Robocode_TankRoyale_BotApi_IBot_IsRunning "IBot.IsRunning property/flag"

[Bot API]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.Bot.html "Bot API"

[IBot]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.IBot.html "IBot interface"

[OnScannedBot]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.BaseBot.html#Robocode_TankRoyale_BotApi_BaseBot_OnScannedBot_Robocode_TankRoyale_BotApi_Events_ScannedBotEvent_ "BaseBot.OnScannedBot(ScannedBotEvent) event handler"

[OnHitByBullet]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.BaseBot.html#Robocode_TankRoyale_BotApi_BaseBot_OnHitByBullet_Robocode_TankRoyale_BotApi_Events_BulletHitBotEvent_ "BaseBot.OnHitByBullet(HitByBulletEvent) event handler"

[CalcBearing]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.Bot.html#Robocode_TankRoyale_BotApi_Bot_CalcBearing_System_Double_ "Bot.CalcBearing(Double) method"

[dotnet]: https://docs.microsoft.com/en-us/dotnet/core/tools/dotnet "dotnet command"

[booter]: ../../articles/booter.md "Robocode booter"

[.csproj]: https://fileinfo.com/extension/csproj "C# project source file"

[.cmd]: https://fileinfo.com/extension/cmd "Windows Command File"

[.sh]: https://fileinfo.com/extension/sh "Bash Shell Script"

[Nuget]: https://www.nuget.org/ "Nuget homepage"

[README]: https://fileinfo.com/extension/readme "Readme File"

[release]: https://github.com/robocode-dev/tank-royale/releases "Releases"