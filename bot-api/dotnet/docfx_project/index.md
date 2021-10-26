# Overview

This API is used when creating bots for the Robocode Tank Royale programming game. The API handles communication with a
game server behind the scene so you can focus on the fun part of controlling the bot.

A good way to get started with Robocode Tank Royale is to head over to the general documentation for Tank Royale to
learn about the basics first:

*   [Robocode Tank Royale Docs](https://robocode.dev/tankroyale/docs/)

Another good way to get started is to look at the source files for the sample bots.

## The bot classes

The first primary class that you know about first is the [Bot](./api/Robocode.TankRoyale.BotApi.Bot.html) class and
perhaps the [BaseBot](./api/Robocode.TankRoyale.BotApi.BaseBot.html). The
[BaseBot](./api/Robocode.TankRoyale.BotApi.BaseBot.html) class provides all the base and minimum functionality of a bot
and deals with the communication with the server. The [Bot](./api/Robocode.TankRoyale.BotApi.Bot.html) class is based on
BaseBot, but provides more convenient methods like e.g. blocking methods for moving and turning the bot, and firing the
gun.

### Code example

Here is an example of a simple bot using the Bot API written in C# and should run as a regular application.

MyFirstBot.cs:

```csharp
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Events;

namespace Robocode.TankRoyale.Sample.Bots
{
  public class MyFirstBot : Bot
  {
    // Main entry used for starting the bot
    static void Main(string[] args)
    {
      new MyFirstBot().Start();
    }

    // Constructor, which loads the bot settings file
    MyFirstBot() : base(BotInfo.FromFile("MyFirstBot.json")) { }

    // This method runs our bot program, where each command is executed one at a time in a loop.
    public override void Run()
    {
      // Repeat while bot is running
      while (IsRunning)
      {
        Forward(100);
        TurnGunRight(360);
        Back(100);
        TurnGunRight(360);
      }
    }

    // Our bot scanned another bot. Fire when we see another bot!
    public override void OnScannedBot(ScannedBotEvent evt)
    {
      Fire(1);
    }

    // Our bot has been hit by a bullet. Turn perpendicular to the bullet so our seesaw might avoid a future shot.
    public override void OnHitByBullet(BulletHitBotEvent evt)
    {
      double bearing = CalcBearing(evt.Bullet.Direction);
      TurnLeft(90 - bearing);
    }
  }
}
```

The above code describes the behavior of the robot. The Main() is the main entry point for all C#
applications to start running the program. Using the bot API, we need to start the robot by calling the
[IBaseBot.Start()](./api/Robocode.TankRoyale.BotApi.IBaseBot.html#Robocode_TankRoyale_BotApi_IBaseBot_Start) method of
the bot API, which will tell the server that this bot wants to join the battle and also provide the server with the
required bot info.

With the bot's constructor (_MyFirstBot()_) we call the [BotInfo.fromFile(string)](
./api/Robocode.TankRoyale.BotApi.BotInfo.html#Robocode_TankRoyale_BotApi_BotInfo_FromFile_System_String_)
method provides the bot info for the server, like e.g. the name of the bot, and its author, etc.

The [Run()](./api/Robocode.TankRoyale.BotApi.IBot.html#Robocode_TankRoyale_BotApi_IBot_Run) method is called
when the bot need to start its real execution to send instructions to the server.

The on-methods (for example, _onScannedBot_ and _onHitByBullet_) are event handlers with code that triggers when a
specific type of event occurs. For example, the event handler [BaseBot.OnScannedBot(ScannedBotEvent)](
./api/Robocode.TankRoyale.BotApi.BaseBot.html#Robocode_TankRoyale_BotApi_BaseBot_OnScannedBot_Robocode_TankRoyale_BotApi_Events_ScannedBotEvent_)
triggers whenever an opponent bot is scanned by the radar. The [ScannedBotEvent](
./api/Robocode.TankRoyale.BotApi.Events.ScannedBotEvent.html) contains the event data for the scanned bot.

## JSON config file

The code in this example is accompanied by a _MyFirstBot.json_, which is a [JSON](https://fileinfo.com/extension/json)
file containing the metadata for the bot.

MyFirstBot.json:
```json
{
  "name": "MyFirstBot",
  "version": "1.0",
  "gameTypes": "melee, classic, 1v1",
  "authors": "Mathew Nelson, Flemming N. Larsen",
  "description": "A sample bot that is probably the first bot you will learn about.",
  "homepage": "",
  "countryCodes": "us, dk",
  "platform": ".Net 5",
  "programmingLang": "C# 8.0"
}
```
These fields are required:

*   name
*   version
*   gameTypes
*   authors

The remaining fields are all optional, but recommended.

Meaning of each field in the JSON file:

*   **name:** is the (display) name of the bot.
*   **version:** is the version of the bot. [SEMVER](https://semver.org/) is the recommended format.
*   **gameTypes:** is a comma-separated list of the game type(s) the bot supports (see below).
*   **authors:** is a comma-separated list with the name of the bot author(s).
*   **description:** is a brief description of the bot.
*   **homepage:** is the URL (link) to a web page for the bot.
*   **countryCodes:** is a comma-separated list of [Alpha-2 country codes](https://www.iban.com/country-codes) the matches the country of the authors.
*   **platform:** is the platform required for running the bot, e.g. Java or .Net.
*   **programmingLang:** is the programming language used for programming the bot, e.g. C# or Kotlin.

## Game types

Standard game types are:

| Game type | Arena size  | Min. participants | Max. participants
|:----------|:-----------:|:-----------------:|:----------------:
| classic   | 800 x 600   | 2                 | (unlimited)
| melee     | 1000 x 1000 | 10                | (unlimited)
| 1v1       | 1000 x 1000 | 2                 | 2

In the future, more game types might arrive. And it is also possible to use custom game types.