# My First Bot for Python

## Introduction

This tutorial is meant for the Python platform and a continuation of [My First Bot](../my-first-bot.md) tutorial.

The programming language used in this tutorial is [Python], which is a very popular language for beginners and pros
alike. This tutorial assumes you are already familiar with basic [Python] programming. It should suit well for
practicing your Python skills by making a bot for Robocode Tank Royale.

## Programming

### Python API

The documentation of the Python API for Robocode Tank Royale is available on [this page](../../api/apis.md).

### Create a source file

Inside your bot directory (`../bots/MyFirstBot`) create a Python source file named `MyFirstBot.py`. You can edit that
file using a text editor of your choice, or an [IDE] like e.g. [PyCharm] or [Visual Studio Code].

### Initial code

The initial skeleton of your bot could look like this:

```python
import asyncio
from robocode_tank_royale.bot_api import Bot


class MyFirstBot(Bot):
    async def run(self) -> None:
        pass


async def main() -> None:
    bot = MyFirstBot()
    await bot.start()


if __name__ == "__main__":
    asyncio.run(main())
```

The class in this example (`MyFirstBot`) is inherited from the [Bot] class from the `robocode_tank_royale.bot_api`
package, which provides methods for controlling the bot and receiving events from the game. The API handles the
communication with the server behind the scenes.

### Startup / Main entry

The next thing we need to do is to implement the `main` entry point for our bot. The bot will run like an ordinary
Python application, typically started with `python MyFirstBot.py`.

```python
import asyncio

# The main function starts our bot
async def main() -> None:
    await MyFirstBot().start()


if __name__ == "__main__":
    asyncio.run(main())
```

The `main` function in this example simply creates the bot and calls its [start] method, which will let the bot start up
reading configuration and start communicating with the server.

The bot will attempt to join the server and wait for a signal to engage in a new battle, where one or multiple instances
of this bot may participate.

Note that it is also possible to provide all the necessary configuration fields programmatically without a file.

### The run method

When the game starts the bot, the [run] method will be called. Hence, your bot should override this method to provide
the logic for the bot when the game is started. The [run] method should do all required initializing. After that, it
should enter a loop that runs until the game is ended.

```python
class MyFirstBot(Bot):
    # Called when a new round is started -> initialize and do some movement
    async def run(self) -> None:
        # Repeat while the bot is running
        while self.is_running():
            await self.forward(100)
            await self.turn_gun_left(360)
            await self.back(100)
            await self.turn_gun_left(360)
```

With the code above, the bot will run in a loop, starting by moving forward 100 units. Then it will turn the gun 360°,
move back 100 units, and turn the gun 360° again. So the bot will continuously move forward and back all the time and
rotate the gun between moving.

When leaving the [run] method, the bot will not be able to send new commands each round besides code that runs in event
handlers. Therefore, a loop is used for preventing the [run] method from exiting. However, we should stop the loop as
soon as the bot is no longer running, and hence need to exit the [run] method when `is_running()` returns `False`.

The [is_running] method returns a flag maintained by the API. When the bot is told to stop/terminate its
execution, [is_running] will automatically return `False`.

### Event handlers

The [Bot API] provides a lot of event handlers (`on<Something>`) that are triggered by different types of events. All
event handlers in the Bot API start with the `on` prefix like e.g. [on_scanned_bot]. All event handlers are available
with the base interfaces that the [Bot] class implements.

Talking about the common [on_scanned_bot] event handler, we can implement this handler to fire the cannon whenever our
bot scans an opponent bot:

```python
from robocode_tank_royale.bot_api.events import ScannedBotEvent

class MyFirstBot(Bot):
    ...
    # We saw another bot -> fire!
    async def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        await self.fire(1)
```

We can also implement another event handler [on_hit_by_bullet] to let the bot attempt to avoid new bullet hits by
turning the bot perpendicular to the bullet direction:

```python
from robocode_tank_royale.bot_api.events import HitByBulletEvent

class MyFirstBot(Bot):
    ...
    # We were hit by a bullet -> turn perpendicular to the bullet
    async def on_hit_by_bullet(self, e: HitByBulletEvent) -> None:
        # Calculate the bearing to the direction of the bullet
        bearing = self.calc_bearing(e.bullet.direction)

        # Turn 90 degrees to the bullet direction based on the bearing
        await self.turn_right(90 - bearing)
```

Note that the [Bot API] provides helper methods like `calc_bearing` to ease calculating angles and bearings in the game.

### Putting it all together

Okay, let us put all the parts together in a single source file:

```python
import asyncio

from robocode_tank_royale.bot_api import Bot
from robocode_tank_royale.bot_api.events import ScannedBotEvent, HitByBulletEvent


class MyFirstBot(Bot):
    # Called when a new round is started -> initialize and do some movement
    async def run(self) -> None:
        # Repeat while the bot is running
        while self.is_running():
            await self.forward(100)
            await self.turn_gun_left(360)
            await self.back(100)
            await self.turn_gun_left(360)

    # We saw another bot -> fire!
    async def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        del e
        await self.fire(1)

    # We were hit by a bullet -> turn perpendicular to the bullet
    async def on_hit_by_bullet(self, e: HitByBulletEvent) -> None:
        # Calculate the bearing to the direction of the bullet
        bearing = self.calc_bearing(e.bullet.direction)

        # Turn 90 degrees to the bullet direction based on the bearing
        await self.turn_right(90 - bearing)


async def main() -> None:
    await MyFirstBot().start()


if __name__ == "__main__":
    asyncio.run(main())
```

## Running the bot

Now we have a JSON configuration file and the program for our bot. The next step is to provide the files and
dependencies for running the bot application.

### Install the bot API library

Install the Python package for the bot API using pip. You can do this globally, in a virtual environment, or in a
project-specific environment.

```bash
# Optionally create and activate a virtual environment
python -m venv .venv
# macOS/Linux
source .venv/bin/activate
# Windows PowerShell
# .venv\Scripts\Activate.ps1

# Install the API
pip install robocode-tankroyale-bot-api
```

Alternatively, if you downloaded the source of this repository, the Python package is located under `bot-api/python` and
can be installed locally with:

```bash
pip install -e ./bot-api/python
```

### Scripts for starting the bot

The remaining part is to supply some script files for starting up the bot. This will ease starting up the bot from the
command line. But those files are also necessary for booting up the bot from Robocode, which will look out for script
files when examining the bot directory and figure out how to run the bot. The script files tell the [booter] of Robocode
how to start the bot, which can differ per platform and OS.

With Python it is possible to run your bot under Windows, macOS, and Linux. Hence, it is a good idea to provide script
files for all these OSes, which means that we should provide a [command file][.cmd] for Windows, and
a [shell script][.sh] for macOS and Linux.

We create a command file for Windows named `MyFirstBot.cmd` and put it into our bot directory:

```
@echo off
python MyFirstBot.py %*
```

So the `python MyFirstBot.py` part is used for starting the bot standing in the bot directory from a command prompt. You
may want to use `python3` if your environment requires it.

Next, we create a shell script for macOS and Linux named `MyFirstBot.sh` and put it into our bot directory:

```sh
#!/usr/bin/env sh
python3 "$(dirname "$0")/MyFirstBot.py" "$@"
```

Make the script executable on Unix-like systems:

```bash
chmod 755 MyFirstBot.sh
```

Now you have everything in place to run your bot with Robocode Tank Royale.

Note that the server must be running locally (on your system) when attempting to run the bot locally; otherwise your bot
will fail with an error because it cannot find the server. The server can be started using the Robocode UI.

## Packaging your bot

If you need to package your bot for distribution, you can zip the bot directory. The zip archive should contain:

- Source file (.py)
- Script files (.cmd and .sh)
- JSON config file (.json)
- Optional: A `requirements.txt` or instructions for installing `robocode-tankroyale-bot-api`

And then you might want to provide a [README] file to provide some information for other people about your bot. :)

You can download the `sample-bots-python-x.y.z.zip` file from any [release], which provides a good example of how to
package one or multiple bots into a zip archive.

## Bot Secrets

When you run your bot outside the GUI from a terminal/shell, you only need to supply bot secrets if (and only if) the
server is configured to require bots to present a secret to connect. The bot secret is one or more keys used by the
server to authorize access.

When a server is running, it will automatically create a `server.properties` file. If bot secrets are required, that
file will contain generated keys to be used by external bots and controllers. In that case, look for the `bot-secrets`
field like this example:

```
bots-secrets=zDuQrkCLQU5VQgytofkNrQ
```

Here, the value of `bot-secrets` (e.g., `zDuQrkCLQU5VQgytofkNrQ`) is the secret your bot must supply.

A simple way to set the bot secret for the Python bot API is to set the environment variable `BOT_SECRETS` in the shell
before running the bot:

Mac/Linux bash/shell:

```bash
export BOT_SECRETS=zDuQrkCLQU5VQgytofkNrQ
```

Windows command line

```cmd
set BOT_SECRETS=zDuQrkCLQU5VQgytofkNrQ
```

Windows PowerShell:

```powershell
$Env:BOT_SECRETS = "zDuQrkCLQU5VQgytofkNrQ"
```

Note that it is also possible to provide the server secret and URL programmatically with the Bot APIs via the `Bot`
constructor.


[Python]: https://www.python.org/doc/ "Python documentation"

[IDE]: https://en.wikipedia.org/wiki/Integrated_development_environment "Integrated development environment"

[PyCharm]: https://www.jetbrains.com/pycharm/ "PyCharm homepage"

[Visual Studio Code]: https://code.visualstudio.com/ "Visual Studio Code homepage"

[Bot]: https://robocode-dev.github.io/tank-royale/api/python/api/bot_api.html#bot_api.Bot "Bot class"

[start]: https://robocode-dev.github.io/tank-royale/api/python/api/bot_api.html#bot_api.Bot.start "start() method"

[run]: https://robocode-dev.github.io/tank-royale/api/python/api/bot_api.html#bot_api.bot.Bot.run "run() method"

[is_running]: https://robocode-dev.github.io/tank-royale/api/python/api/bot_api.html#bot_api.bot.Bot.is_running "is_running() method"

[on_scanned_bot]: https://robocode-dev.github.io/tank-royale/api/python/api/bot_api.html#bot_api.base_bot.BaseBot.on_scanned_bot "on_scanned_bot event handler"

[on_hit_by_bullet]: https://robocode-dev.github.io/tank-royale/api/python/api/bot_api.html#bot_api.base_bot.BaseBot.on_hit_by_bullet "on_hit_by_bullet event handler"

[Bot API]: https://robocode-dev.github.io/tank-royale/api/python/index.html "Bot API"

[booter]: ../../articles/booter.md "Robocode booter"

[.cmd]: https://fileinfo.com/extension/cmd "Windows Command File"

[.sh]: https://fileinfo.com/extension/sh "Bash Shell Script"

[README]: https://fileinfo.com/extension/readme "Readme File"

[release]: https://github.com/robocode-dev/tank-royale/releases "Releases"