# My First Bot for .Net

This tutorial is meant for the .Net platform and a continuation of [My First Bot](../my-first-bot.md) tutorial.

The programming language used in this tutorial is [C#](https://docs.microsoft.com/en-us/dotnet/csharp/), which is a very
popular programming language for .Net. But other .Net programming languages like
[Visual Basic](https://docs.microsoft.com/en-us/dotnet/visual-basic/) and
[F#](https://docs.microsoft.com/en-us/dotnet/visual-basic/) can be used as well.

Note that this tutorial assumes you are already familiar with basic
[C#]((https://docs.microsoft.com/en-us/dotnet/csharp/)) and [.Net](https://dotnet.microsoft.com/en-us/) programming. But
this tutorial might also suit well for learning how to program for .Net in C#, and making a bot for Robocode might be a
good way for learning how to program for the .Net platform and C# programming language.

## Programming

### .Net API

The documentation of the .Net API is available on [this page](../../api/apis.md).

### Create a source file

Inside your bot directory (`../bots/MyFirstBot`) you need to create a C# source file named `MyFirstBot.cs`. You can edit
this file using a text editor of your choice, or an IDE like e.g. Visual Studio Code or Visual Studio.

### Initial code

The initial skeleton of your robot could look like this:

```csharp
using Robocode.TankRoyale.BotApi;

namespace Example
{
  public class MyFirstBot : Bot
  {
  }
}
```

You can name the *namespace* and *class* to whatever you prefer. The namespace could be your nickname, organization etc.
With this tutorial, we name the class so it will match the name "My First Bot" of course.

The class in this example is inherited from the
[Bot](https://robocode.dev/tankroyale/api/dotnet/api/Robocode.TankRoyale.BotApi.Bot.html) class from the
`Robocode.TankRoyale.BotApi` provides methods for controlling the bot but also makes it possible to receive events from
the game. The API is taking care of the communication with the server behind the scene.

### Startup / Main entry

The next thing we need to do is to declare a
[Main](https://docs.microsoft.com/en-us/dotnet/csharp/fundamentals/program-structure/main-command-line) method for our
bot. The bot will run like an ordinary application, and hence the Main method is the entry point of the bot.

```csharp
    static void Main(string[] args)
    {
      new MyFirstBot().Start();
    }

    MyFirstBot() : base(BotInfo.FromFile("MyFirstBot.json")) { }
```

The Main method in this example is set up to call the *Start()* method of the bot, which will let the robot startup
reading configuration and start communicating with the server.

The bot will attempt to _join_ the server and wait for a signal to engage in a new battle, where one or multiple
instances of this bot must participate.

The constructor of MyFirstBot is set up to call the base constructor, which needs a
[BotInfo](https://robocode.dev/tankroyale/api/dotnet/api/Robocode.TankRoyale.BotApi.BotInfo.html) object containing the
bot configuration. The `BotInfo` class contains a convenient method named `FromFile` which can initialize the `BotInfo`
by reading a JSON file. In this case, it will read the `MyFirstBot.json` file we created earlier, which must be
available within the bot directory (or some other file location accessible for the bot).

Note that it is also possible to provide all the necessary configuration fields programmatically without a file.

### The Run method

When the bot is started by the game, the `Run()` method will be called. Hence, your bot should override this method to
provide the logic for the robot when the game is started. The `Run` method should do all required initializing. After
that, it should enter a loop that runs until the game is ended.

When leaving the `Run` method, the bot will not be able to send new commands each round besides code that runs in event
handlers. Therefore, a loop is used for preventing the `Run` method from exiting. However, we should stop the loop as
soon as the bot is no longer running, and hence need to exit the `Run()` method.

```csharp
    public override void Run()
    {
      while (IsRunning)
      {
        Forward(100);
        TurnGunRight(360);
        Back(100);
        TurnGunRight(360);
      }
    }
```

With the `Run` method above, the bot is running a program in a loop that is running as long as the bot is running.
The `IsRunning` property contains a flag maintained by the API. If the bot is set to stop/terminate its execution, the
`IsRunning` property is set to `false`.

With the code above, the bot will run in a loop, starting by moving forward 100 units. Then it will turn the gun 360°,
move back 100 units and turn the gun 360° again. So the bot will continuously move forward and back all the time and
rotate the gun between moving.

### Event handlers

The Bot API provides a lot of event handlers that are triggered by different types of events. All event handlers in the
Bot API start with the On-prefix On like e.g. `OnScannedBot`. All event handlers are available with the
[IBot](https://robocode.dev/tankroyale/api/dotnet/api/Robocode.TankRoyale.BotApi.IBot.html) interface, which the `Bot`
class implements.

So we can implement an event handler (`OnScannedBot`) to fire the cannon whenever our bot scans an opponent bot:

```csharp
    using Robocode.TankRoyale.BotApi.Events;
    ...

    public override void OnScannedBot(ScannedBotEvent evt)
    {
      Fire(1);
    }
```

We can also implement an event handler (`OnHitByBullet`) to let the bot react on bullet hits to avoid new bullet hits by
turning the bot perpendicular to the bullet direction:

```csharp
    using Robocode.TankRoyale.BotApi.Events;
    ...

    public override void OnHitByBullet(BulletHitBotEvent evt)
    {
      // Calculate the bearing to the direction of the bullet
      double bearing = CalcBearing(evt.Bullet.Direction);

      // Turn 90 degrees to the bullet direction based on the bearing
      TurnLeft(90 - bearing);
    }
```

Note that the Bot API provides helper methods like `CalcBearing` to ease calculating angles and bearings in the game.

### Putting it all together

Okay, so let us put all the parts together in the source file:

```csharp
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

namespace Example
{
  public class MyFirstBot : Bot
  {
    /// Main method starts our bot
    static void Main(string[] args)
    {
      new MyFirstBot().Start();
    }

    /// Constructor, which loads the bot settings file
    MyFirstBot() : base(BotInfo.FromFile("MyFirstBot.json")) { }

    /// This method runs our bot program, where each command is executed one at a time in a loop.
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

    /// Our bot scanned another bot. Fire when we see another bot!
    public override void OnScannedBot(ScannedBotEvent evt)
    {
      Fire(1);
    }

    /// Our bot has been hit by a bullet. Turn perpendicular to the bullet so our seesaw might avoid a future shot.
    public override void OnHitByBullet(BulletHitBotEvent evt)
    {
      // Calculate the bearing to the direction of the bullet
      double bearing = CalcBearing(evt.Bullet.Direction);

      // Turn 90 degrees to the bullet direction based on the bearing
      TurnLeft(90 - bearing);
    }
  }
}
```

## Running the bot

Now we got a JSON configuration file and the program for our bot. The next step is to provide the files for running the
bot application.

We need to set up a C# project source file (.csfile) and a command file (.cmd) that provides the script for starting the
bot.

### C# project source file

So we need to create a `MyFirstBot.csproj` file with the following content:

```xml

<Project Sdk="Microsoft.NET.Sdk">
    <PropertyGroup>
        <RootNamespace>MyFirstBot</RootNamespace>
        <OutputType>Exe</OutputType>
        <TargetFramework>net5.0</TargetFramework>
        <LangVersion>8.0</LangVersion>
    </PropertyGroup>
    <ItemGroup>
        <PackageReference Include="Robocode.TankRoyale.BotApi" Version="0.9.11"/>
    </ItemGroup>
</Project>
```

This C# project source file is a config file used by the `dotnet` command used for compiling and running the bot.

Most parts of this file are static content. But these fields need attention:

- RootNamespace should contain the exact class name of your bot.
- TargetFramework tells what .Net version your bot requires for running.
- LangVersion is the C# version
- Version for the PackageReference for the Robocode.TankRoyale.BotApi must use the version of the API the bot is built
  for.

### API available at Nuget

The Robocode.TankRoyale.BotApi is available on [Nuget](https://www.nuget.org/) here:
https://www.nuget.org/packages/Robocode.TankRoyale.BotApi/

You can install the bot API using the `dotnet` command like this:

```
dotnet add package Robocode.TankRoyale.BotApi
```

And you can install a specific version by adding the `--version`:

```
dotnet add package Robocode.TankRoyale.BotApi --version 0.9.11
```

### Scripts for starting the bot

The remaining part is to supply a script file used for starting up the bot. Robocode will look out for script files when
examining the bot directory and figure out how to run the bot. The script file(s) tell the booter of Robocode how to
start the bot.

For .Net it is possible to run your bot under Windows, macOS, and Linux. Hence, it is a good idea to provide script
files for all these OSes, which mean that we should provide a command file (.cmd) for Windows, and a shell file (.sh)
for macOS and Linux.

We create a command file for Windows named MyFirstBot.cmd and put it into our bot directory:

```
dotnet run >nul
```

So the `dotnet run` part is used for starting the bot standing in the bot directory from a command prompt. The `>nul` is
an important work-around to avoid a Windows-specific issue where the bot becomes unresponsive when started up as a
process with the Robocode UI.

Next, we provide a shell script for macOS and Linux named MyFirstBot.sh and put it into our bot directory:

```
#!/bin/sh
dotnet run 
```

Note that we need to set the file permission to grant read and execute rights of the script for the owner, owner's
group, and everybody else, e.g. set the permissions to `-r-xr-xr-x` (555). We can do this with this command:

```
chown 555 MyFirstBot.sh
```

Now you have everything in place to run your bot with Robocode Tank Royale.

Note that the server must be running locally when running the bot locally. The server can be started using the Robocode
UI.

## Packaging your bot

If you need to package your bot, you can do this by zip-packing the bot directory. The zip archive should contain:

- Source file (.cs, .fs, or .vb)
- Project source file (.csproj, .fsproj, or .vbproj)
- Script files (.cmd and .sh)
- JSON config file (.json)

And then you might want to provide a README to provide some information for other people about your bot. :) 
