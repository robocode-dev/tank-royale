# My First Bot for TypeScript / JavaScript

## Introduction

This tutorial is meant for the TypeScript/JavaScript platform and is a continuation of the
[My First Bot](../my-first-bot.md) tutorial.

The programming language used here is [TypeScript], which adds static typing on top of JavaScript and runs on
[Node.js]. Plain JavaScript is equally supported — just omit the type annotations. This tutorial assumes you are
already familiar with basic TypeScript or JavaScript programming.

## Programming

### TypeScript API

The documentation of the TypeScript Bot API for Robocode Tank Royale is available on [this page](../../api/apis).

### Create a source file

Inside your bot directory (`../bots/MyFirstBot`) create a TypeScript source file named `MyFirstBot.ts`. You can edit
that file using a text editor of your choice, or an [IDE] like e.g. [Visual Studio Code] or [WebStorm].

### Initial code

The initial skeleton of your bot could look like this:

```typescript
import { Bot } from "@robocode.dev/tank-royale-bot-api";

class MyFirstBot extends Bot {
}
```

The class in this example (`MyFirstBot`) extends the [Bot] class from the `@robocode.dev/tank-royale-bot-api` package,
which provides methods for controlling the bot and receiving events from the game. The API handles the communication
with the server behind the scenes.

### Startup / Main entry

The next thing we need to do is to add a static `main` method that creates and starts the bot:

```typescript
class MyFirstBot extends Bot {
    // The main method starts our bot
    static main() {
        new MyFirstBot().start();
    }
}

MyFirstBot.main();
```

The `main` method simply creates the bot and calls its [start] method, which will let the bot start up, read
configuration (from a `.json` file or environment variables), and start communicating with the server.

If you don't want to use a `.json` configuration file, you can provide the bot information directly in the constructor:

```typescript
import { Bot, BotInfo } from "@robocode.dev/tank-royale-bot-api";

class MyFirstBot extends Bot {
    constructor() {
        super(new BotInfo(
            "My First Bot",
            "1.0",
            ["Your Name"],
            "My first bot",
            null,
            ["US"],
            ["classic"],
            "Node.js",
            "TypeScript",
            null
        ));
    }
}
```

When providing `BotInfo` in the constructor, the `.json` file is not required. The booter will use
**heuristic platform detection** together with the script files (see [Running the bot](#running-the-bot)) to
locate and start your TypeScript bot.

> **Tip:** The simplest approach is to always provide a `MyFirstBot.json` file alongside your source — it gives
> the booter everything it needs and lets you skip writing a constructor.

### The run method

When the game starts the bot, the [run] method will be called. Override this method to provide the logic for the
bot when the game is started. It should do all required initialization, then enter a loop that runs until the game ends.

```typescript
class MyFirstBot extends Bot {
    // Called when a new round is started -> initialize and do some movement
    override run() {
        // Repeat while the bot is running
        while (this.isRunning()) {
            this.forward(100);
            this.turnGunLeft(360);
            this.back(100);
            this.turnGunLeft(360);
        }
    }
}
```

With the code above, the bot will run in a loop: moving forward 100 units, turning the gun 360°, moving back 100
units, and turning the gun 360° again. The loop continues as long as [isRunning] returns `true`. The API sets this
flag to `false` automatically when the bot is told to stop.

### Event handlers

The [Bot API] provides many event handlers (`on<SomeEvent>` methods) triggered by different types of game events.
All event handlers start with the `on` prefix.

The [onScannedBot] event handler fires the cannon whenever the bot scans an opponent:

```typescript
import { Bot, ScannedBotEvent } from "@robocode.dev/tank-royale-bot-api";

class MyFirstBot extends Bot {
    // We saw another bot -> fire!
    override onScannedBot(e: ScannedBotEvent) {
        this.fire(1);
    }
}
```

The [onHitByBullet] event handler turns the bot perpendicular to the bullet to avoid further hits:

```typescript
import { Bot, HitByBulletEvent } from "@robocode.dev/tank-royale-bot-api";

class MyFirstBot extends Bot {
    // We were hit by a bullet -> turn perpendicular to the bullet
    override onHitByBullet(e: HitByBulletEvent) {
        // Calculate the bearing to the direction of the bullet
        const bearing = this.calcBearing(e.bullet.direction);
        // Turn 90 degrees to the bullet direction based on the bearing
        this.turnRight(90 - bearing);
    }
}
```

The [Bot API] provides helper methods like [calcBearing] to ease calculating angles and bearings in the game.

### Putting it all together

::: code-group

```typescript [TypeScript]
import { Bot, HitByBulletEvent, ScannedBotEvent } from "@robocode.dev/tank-royale-bot-api";

class MyFirstBot extends Bot {
    // The main method starts our bot
    static main() {
        new MyFirstBot().start();
    }

    // Called when a new round is started -> initialize and do some movement
    override run() {
        // Repeat while the bot is running
        while (this.isRunning()) {
            this.forward(100);
            this.turnGunLeft(360);
            this.back(100);
            this.turnGunLeft(360);
        }
    }

    // We saw another bot -> fire!
    override onScannedBot(e: ScannedBotEvent) {
        this.fire(1);
    }

    // We were hit by a bullet -> turn perpendicular to the bullet
    override onHitByBullet(e: HitByBulletEvent) {
        // Calculate the bearing to the direction of the bullet
        const bearing = this.calcBearing(e.bullet.direction);
        // Turn 90 degrees to the bullet direction based on the bearing
        this.turnRight(90 - bearing);
    }
}

MyFirstBot.main();
```

```javascript [JavaScript]
import { Bot } from "@robocode.dev/tank-royale-bot-api";

class MyFirstBot extends Bot {
    // The main method starts our bot
    static main() {
        new MyFirstBot().start();
    }

    // Called when a new round is started -> initialize and do some movement
    run() {
        // Repeat while the bot is running
        while (this.isRunning()) {
            this.forward(100);
            this.turnGunLeft(360);
            this.back(100);
            this.turnGunLeft(360);
        }
    }

    // We saw another bot -> fire!
    onScannedBot(e) {
        this.fire(1);
    }

    // We were hit by a bullet -> turn perpendicular to the bullet
    onHitByBullet(e) {
        // Calculate the bearing to the direction of the bullet
        const bearing = this.calcBearing(e.bullet.direction);
        // Turn 90 degrees to the bullet direction based on the bearing
        this.turnRight(90 - bearing);
    }
}

MyFirstBot.main();
```

:::

> **JavaScript differences:** Drop all type annotations and the `override` keyword. Name your source file
> `MyFirstBot.js` and add `"type": "module"` to your `package.json` (required for ES module imports).
> Use `node MyFirstBot.js` directly in your scripts instead of `tsx MyFirstBot.ts`.

## Running the bot

Now we have a JSON configuration file and the source file for our bot. The next step is to set up the dependencies
and the scripts for running the bot application.

### Install the bot API library

The TypeScript Bot API is published as an npm package. Install it along with [tsx] (a fast TypeScript runner) and
the WebSocket library:

```bash
npm install @robocode.dev/tank-royale-bot-api tsx ws
```

If you downloaded the source from this repository, the TypeScript package is located under `bot-api/typescript`
and you can reference it locally via a file path in `package.json`.

### Scripts for starting the bot

Provide script files so both the command line and the Robocode booter can start your bot.

We create a command file for Windows named `MyFirstBot.cmd` and put it into our bot directory:

```cmd
@echo off
cd /d "%~dp0"
set NODE_OPTIONS=--disable-warning=ExperimentalWarning
..\node_modules\.bin\tsx MyFirstBot.ts
```

Next, we create a shell script for macOS and Linux named `MyFirstBot.sh` and put it into our bot directory:

```sh
#!/bin/sh
set -e
cd -- "$(dirname -- "$0")"
export NODE_OPTIONS="--disable-warning=ExperimentalWarning"
exec "../node_modules/.bin/tsx" "MyFirstBot.ts"
```

Make the shell script executable:

```bash
chmod 755 MyFirstBot.sh
```

Now you have everything in place to run your bot with Robocode Tank Royale.

> **Note:** The server must be running locally when you attempt to run the bot locally; otherwise the bot will fail
> with an error because it cannot reach the server. The server can be started using the Robocode UI.

## Packaging your bot

To package your bot for distribution, zip the bot directory. The archive should contain:

- Source file (`.ts` or `.js`)
- JSON config file (`.json`)
- Script files (`.cmd` and `.sh`) — *Optional due to template-based booting*
- `package.json` and `node_modules/` (or a `package-lock.json` so users can run `npm install`)

You can download the `sample-bots-typescript-x.y.z.zip` file from any [release], which provides a good example of
how to package one or multiple TypeScript bots.

## Bot Secrets

When you run your bot outside the GUI, you only need to supply bot secrets if the server is configured to require
them. Look for the `bots-secrets` field in the server's `server.properties` file:

```
bots-secrets=zDuQrkCLQU5VQgytofkNrQ
```

Set the `BOT_SECRETS` environment variable before running your bot:

Mac/Linux bash/shell:

```bash
export BOT_SECRETS=zDuQrkCLQU5VQgytofkNrQ
```

Windows command line:

```cmd
set BOT_SECRETS=zDuQrkCLQU5VQgytofkNrQ
```

Windows PowerShell:

```powershell
$Env:BOT_SECRETS = "zDuQrkCLQU5VQgytofkNrQ"
```

It is also possible to provide the server secret and URL programmatically via the `Bot` constructor.


[TypeScript]: https://www.typescriptlang.org/ "TypeScript language"

[Node.js]: https://nodejs.org/ "Node.js runtime"

[IDE]: https://en.wikipedia.org/wiki/Integrated_development_environment "Integrated development environment"

[Visual Studio Code]: https://code.visualstudio.com/ "Visual Studio Code homepage"

[WebStorm]: https://www.jetbrains.com/webstorm/ "WebStorm IDE"

[tsx]: https://tsx.is/ "tsx — TypeScript Execute"

[Bot]: https://robocode-dev.github.io/tank-royale/api/typescript/classes/Bot.html "Bot class"

[start]: https://robocode-dev.github.io/tank-royale/api/typescript/classes/BaseBot.html#start "BaseBot.start()"

[BotInfo]: https://robocode-dev.github.io/tank-royale/api/typescript/classes/BotInfo.html "BotInfo class"

[run]: https://robocode-dev.github.io/tank-royale/api/typescript/classes/Bot.html#run "Bot.run() method"

[isRunning]: https://robocode-dev.github.io/tank-royale/api/typescript/classes/Bot.html#isRunning "Bot.isRunning() method"

[Bot API]: https://robocode-dev.github.io/tank-royale/api/typescript/ "TypeScript Bot API"

[onScannedBot]: https://robocode-dev.github.io/tank-royale/api/typescript/interfaces/IBaseBot.html#onScannedBot "IBaseBot.onScannedBot event handler"

[onHitByBullet]: https://robocode-dev.github.io/tank-royale/api/typescript/interfaces/IBaseBot.html#onHitByBullet "IBaseBot.onHitByBullet event handler"

[calcBearing]: https://robocode-dev.github.io/tank-royale/api/typescript/interfaces/IBaseBot.html#calcBearing "IBaseBot.calcBearing() method"

[booter]: ../../articles/booter.md "Robocode booter"

[release]: https://github.com/robocode-dev/tank-royale/releases "Releases"
