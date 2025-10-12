# Robocode Tank Royale - Python Bot API

<img src="https://raw.githubusercontent.com/robocode-dev/tank-royale/refs/heads/main/gfx/Tank/Tank-logo.svg" width="150" alt="Robocode logo">

**Build the best - destroy the rest!**

The **Python Bot API** for [Robocode Tank Royale](https://robocode-dev.github.io/tank-royale/) - the next evolution of
the classic Robocode programming game where you code virtual tanks to battle against each other in a virtual arena.

## About Robocode Tank Royale

Robocode is a programming game where the goal is to code a bot in the form of a virtual tank to compete against other
bots in a virtual battle arena. The player is the programmer of a bot, who will have no direct influence on the game.
Instead, you must write a program with the logic for the brain of the bot containing instructions about how it should
behave during battles.

**Tank Royale** is the next generation of Robocode that supports:

- Network-based battles via WebSocket connections
- Multiple programming languages and platforms
- Real-time battles with multiple bots

## Installation

Install the Python Bot API using pip:

```shell
pip install robocode-tank-royale
```

### Requirements

- **Python 3.10 or higher**
- WebSocket support (automatically handled by dependencies)

## Quick Start

Check out the complete **MyFirstBot** example on GitHub:

**📁 [MyFirstBot.py](https://github.com/robocode-dev/tank-royale/blob/main/sample-bots/python/MyFirstBot/MyFirstBot.py)**: 

Or start with this minimal example you can run right away:

```python
import asyncio

from robocode_tank_royale.bot_api.bot import Bot
from robocode_tank_royale.bot_api.events import ScannedBotEvent, HitByBulletEvent

class MyFirstBot(Bot):
    async def run(self) -> None:
        while self.is_running():
            await self.forward(100)
            await self.turn_gun_left(360)
            await self.back(100)
            await self.turn_gun_left(360)

    async def on_scanned_bot(self, e: ScannedBotEvent) -> None:
        del e
        await self.fire(1)

    async def on_hit_by_bullet(self, e: HitByBulletEvent) -> None:
        bearing = self.calc_bearing(e.bullet.direction)
        await self.turn_right(90 - bearing)

async def main() -> None:
    bot = MyFirstBot()
    await bot.start()

if __name__ == "__main__":
    asyncio.run(main())
```

How to run:
- Install and start the Robocode Tank Royale GUI from the releases page (it starts the game server).
- Save the code above as MyFirstBot.py and run: `python MyFirstBot.py`
- In the GUI, add your bot process and start a battle.

## Features

The Python Bot API provides:

- **Full Bot Control**: Move your tank, rotate gun and radar, fire bullets
- **Event Handling**: Respond to hits, bot scanned, bullet impacts, and more
- **Battle Information**: Access to battle state, opponent positions, and game rules
- **Debugging Support**: Built-in debugging and logging capabilities
- **Modern Python**: Type hints and modern Python features supported

## Getting Started

1. **Install the package**: `pip install robocode-tank-royale`

2. **Download Robocode Tank Royale**: Get the game GUI and server from
   the [official releases](https://github.com/robocode-dev/tank-royale/releases)

3. **Create your bot**: Download and
   study [MyFirstBot.py](https://github.com/robocode-dev/tank-royale/blob/master/sample-bots/python/MyFirstBot/MyFirstBot.py)
   or check the documentation

4. **Run battles**: Start the GUI, add your bot, and watch the battles unfold!

## Documentation & Resources

- **📖 Official Documentation**: [robocode-dev.github.io/tank-royale](https://robocode-dev.github.io/tank-royale/)
- **🚀 Getting Started:** [Tutorial](https://robocode-dev.github.io/tank-royale/tutorial/getting-started.html)
- **🤖 My First Bot:** [Create Your First Bot](https://robocode-dev.github.io/tank-royale/tutorial/my-first-bot.html)
- **📚 API Reference**: [Python API Documentation](https://robocode-dev.github.io/tank-royale/api/apis.html)
- **🤖 Sample Bots**: [Python Examples](https://github.com/robocode-dev/tank-royale/tree/main/sample-bots/python)
- **💾 Source Code**: [GitHub Repository](https://github.com/robocode-dev/tank-royale/tree/main/bot-api/python)

## Supported Platforms

Robocode Tank Royale runs on:

- **Windows**
- **macOS**
- **Linux**

The Python Bot API works with **Python 3.10+** on all supported platforms.

## Community & Support

- **GitHub Issues**: [Report bugs and request features](https://github.com/robocode-dev/tank-royale/issues)
- **Discussions**: [Community discussions and help](https://github.com/robocode-dev/tank-royale/discussions)
- **Contributing**: Check out our [Contributing Guide](https://github.com/robocode-dev/tank-royale/blob/master/CONTRIBUTING.md)

## Development Status

🚧 **Work in Progress**: The Python Bot API is currently under active development. Features and APIs may change before
the stable release.

## License

Licensed under the [Apache License 2.0](https://github.com/robocode-dev/tank-royale/blob/main/LICENSE)

## Copyright

Copyright © 2022 [Flemming N. Larsen](https://github.com/flemming-n-larsen)

---

**Ready to build the best tank and destroy the rest?** Start coding your bot today! 🚀🎯