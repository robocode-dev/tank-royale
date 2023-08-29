# Overview

This API is used when creating bots for the Robocode Tank Royale programming game. The API handles communication with a
game server behind the scene, so you can focus on the fun part of controlling the bot.

The Bot API is available here:

- [Bot API for .Net](https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.html)

A good way to get started with Robocode Tank Royale is to head over to the general documentation for Tank Royale to
learn about the basics first:

- [Robocode Tank Royale Docs]

Another good way to get started is to look at the source files for the sample bots.

## The bot classes

The first primary class that you know about first is the [Bot] class and perhaps the [BaseBot]. The [BaseBot] class
provides all the base and minimum functionality of a bot and deals with the communication with the server. The [Bot]
class is based on BaseBot, but provides more convenient methods like e.g. blocking methods for moving and turning the
bot, and firing the gun.

## Code example

Here is an example of a simple bot using the Bot API written in C# and should run as a regular application.

MyFirstBot.cs:

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

The above code describes the behavior of the bot. The Main() is the main entry point for all C# applications to start
running the program. Using the bot API, we need to start the bot by calling the
[IBaseBot.Start()] method of the bot API, which will tell the server that this bot wants to join the battle and also
provide the server with the required bot info.

With the bot´s constructor (_MyFirstBot()_) we call the [BotInfo.fromFile(string)] method provides the bot info for the
server, like e.g. the name of the bot, and its author, etc.

The [Run()] method is called when the bot need to start its real execution to send instructions to the server.

The on-methods (for example, _onScannedBot_ and _onHitByBullet_) are event handlers with code that triggers when a
specific type of event occurs. For example, the event handler [BaseBot.OnScannedBot(ScannedBotEvent)]
triggers whenever an opponent bot is scanned by the radar. The [ScannedBotEvent] contains the event data for the scanned
bot.

## JSON config file

The code in this example is accompanied by a _MyFirstBot.json_, which is a [JSON] file containing the config file for
the bot, and is used by the **booter** to start up the bot on a local machine.

MyFirstBot.json:

```json
{
  "name": "My First Bot",
  "version": "1.0",
  "authors": [
    "Mathew Nelson",
    "Flemming N. Larsen"
  ],
  "description": "A sample bot that is probably the first bot you will learn about.",
  "homepage": "",
  "countryCodes": [
    "us",
    "dk"
  ],
  "platform": ".Net 6.0",
  "programmingLang": "C# 10.0"
}
```

You can read more details about the format of this JSON
file [here](https://robocode-dev.github.io/tank-royale/articles/booter.html#json-config-file.html).


[Bot API for .Net]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.html

[Robocode Tank Royale Docs]: https://robocode-dev.github.io/tank-royale/

[Bot]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.Bot.html

[BaseBot]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.BaseBot.html

[IBaseBot.Start()]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.IBaseBot.html#Robocode_TankRoyale_BotApi_IBaseBot_Start

[BotInfo.FromFile(string)]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.BotInfo.html#Robocode_TankRoyale_BotApi_BotInfo_FromFile_System_String_

[Run()]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.IBot.html#Robocode_TankRoyale_BotApi_IBot_Run

[BaseBot.OnScannedBot(ScannedBotEvent)]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.BaseBot.html#Robocode_TankRoyale_BotApi_BaseBot_OnScannedBot_Robocode_TankRoyale_BotApi_Events_ScannedBotEvent_

[ScannedBotEvent]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.Events.ScannedBotEvent.html

[JSON]: https://fileinfo.com/extension/json