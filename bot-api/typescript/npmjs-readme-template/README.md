# Robocode Tank Royale — TypeScript/JavaScript Bot API

<img src="https://raw.githubusercontent.com/robocode-dev/tank-royale/refs/heads/main/gfx/Tank/Tank-logo.svg" width="150" alt="Robocode logo">

**Build the best — destroy the rest!**

The **TypeScript/JavaScript Bot API** for [Robocode Tank Royale](https://robocode-dev.github.io/tank-royale/) — the next evolution of the classic Robocode programming game where you code virtual tanks (bots) to battle in a virtual arena.

## About Robocode Tank Royale

Robocode is a programming game where your task is to code a bot (a virtual tank) that competes against other bots in a real-time battle arena. You are the programmer — the bot acts entirely on your code.

**Tank Royale** brings Robocode to the next level with:
- Network-based battles via WebSocket connections
- Multiple programming languages and platforms
- Real-time battles with multiple bots

## Installation

Install the TypeScript/JavaScript Bot API using npm:

```bash
npm install @robocode.dev/tank-royale-bot-api
```

To install a specific version:

```bash
npm install @robocode.dev/tank-royale-bot-api@@VERSION@
```

The `ws` package is required at runtime in a Node.js environment:

```bash
npm install ws
```

## Requirements

- **Node.js 22 (LTS) or higher**
- **TypeScript 5+** (for writing bots in TypeScript)

## Quick Start

```typescript
import { Bot, HitByBulletEvent, ScannedBotEvent } from "@robocode.dev/tank-royale-bot-api";

class MyFirstBot extends Bot {
    static main() {
        new MyFirstBot().start();
    }

    override run() {
        while (this.isRunning()) {
            this.forward(100);
            this.turnGunRight(360);
            this.back(100);
            this.turnGunRight(360);
        }
    }

    override onScannedBot(e: ScannedBotEvent) {
        this.fire(1);
    }

    override onHitByBullet(e: HitByBulletEvent) {
        const bearing = this.calcBearing(e.bullet.direction);
        this.turnRight(90 - bearing);
    }
}

MyFirstBot.main();
```

The bot reads its name, version, and authors from a `MyFirstBot.json` file placed alongside the source,
or from environment variables set by the Robocode booter.

## Getting Started

1. **Install the package**: `npm install @robocode.dev/tank-royale-bot-api`
2. **Download Robocode Tank Royale**: Get the game GUI and server from the [official releases](https://github.com/robocode-dev/tank-royale/releases)
3. **Create your bot**: Follow the [TypeScript tutorial](https://robocode-dev.github.io/tank-royale/tutorial/typescript/my-first-bot-for-typescript) or copy the [MyFirstBot sample](https://github.com/robocode-dev/tank-royale/tree/main/sample-bots/typescript/MyFirstBot)
4. **Run battles**: Start the GUI, add your bot process, and watch the fight

## Features

The TypeScript/JavaScript Bot API provides:

- **Full Bot Control**: Move your tank, rotate gun and radar, fire bullets
- **Event Handling**: Respond to hits, bot scanned, bullet impacts, and more
- **Battle Information**: Access to battle state, opponent positions, and game rules
- **Synchronous API**: Sequential, blocking style — no `async`/`await` in bot logic
- **Dual Module Formats**: Ships both ESM and CommonJS outputs with TypeScript declarations
- **Debugging Support**: Built-in debugging and logging capabilities

## API Overview

The API closely mirrors the Java, .NET, and Python Bot APIs. Key classes and interfaces:

| Export                       | Description                                  |
|------------------------------|----------------------------------------------|
| `Bot`                        | Full bot with independent gun/radar movement |
| `BaseBot`                    | Low-level bot with event-driven control      |
| `IBot`                       | Interface for `Bot`                          |
| `IBaseBot`                   | Interface for `BaseBot`                      |
| `Droid`                      | Marker interface for team droid bots         |
| `BotInfo` / `BotInfoBuilder` | Bot metadata for handshake                   |
| `Constants`                  | Game constants (max speed, rates, etc.)      |
| `GameSetup`                  | Arena and game configuration                 |
| `BotState`                   | Snapshot of bot state during a tick          |
| `BulletState`                | Snapshot of bullet state during a tick       |
| `BotResults`                 | Final round/battle results                   |
| `Color` / `ColorUtil`        | Color representation utilities               |
| `IGraphics` / `SvgGraphics`  | Graphics API for painting overlays           |
| `DefaultEventPriority`       | Default priorities for bot events            |
| `EnvVars`                    | Environment variable names for configuration |

## Synchronous API Model

Bot code is written in a **synchronous, sequential style** — identical to the Java, C#, and Python Bot APIs.
There is no `async`/`await` in bot code:

```typescript
// ✅ Correct — sequential, blocking
override run() {
    while (this.isRunning()) {
        this.forward(100);   // blocks until bot has moved 100 units
        this.turnRight(90);  // blocks until turn is complete
        this.fire(1);
    }
}

// ❌ Wrong — do NOT use async/await in bot code
async run() {
    await this.forward(100); // incorrect — forward() is not a Promise
}
```

**How it works internally:** The API uses a Worker thread (`worker_threads` in Node.js) with
`SharedArrayBuffer` + `Atomics.wait()` to provide true blocking. The WebSocket runs on the main thread;
bot logic runs on the worker thread. When a tick arrives, the main thread calls `Atomics.notify()` to
wake the bot, just like Java's `notifyAll()`. This means `forward(100)` genuinely blocks the bot thread —
no Promises, no callbacks, no generator yields.

## Configuration via Environment Variables

The bot reads all settings from environment variables (matching the Java, .NET, and Python APIs):

**Connection settings:**

| Variable        | Default               | Description                      |
|-----------------|-----------------------|----------------------------------|
| `SERVER_URL`    | `ws://localhost:7654` | WebSocket server URL             |
| `SERVER_SECRET` | _(none)_              | Secret for server authentication |

**Bot identity (set by the booter; override manually for testing):**

| Variable            | Required | Description                                      |
|---------------------|----------|--------------------------------------------------|
| `BOT_NAME`          | ✅        | Bot name                                         |
| `BOT_VERSION`       | ✅        | Bot version string                               |
| `BOT_AUTHORS`       | ✅        | Comma-separated list of authors                  |
| `BOT_DESCRIPTION`   | —        | Short description                                |
| `BOT_HOMEPAGE`      | —        | Homepage URL                                     |
| `BOT_COUNTRY_CODES` | —        | Comma-separated ISO 3166-1 alpha-2 country codes |
| `BOT_GAME_TYPES`    | —        | Comma-separated supported game types             |
| `BOT_PLATFORM`      | —        | Platform name (e.g. `Node.js`)                   |
| `BOT_PROG_LANG`     | —        | Programming language (e.g. `TypeScript`)         |
| `BOT_INITIAL_POS`   | —        | Initial position override (`x,y,direction`)      |

Set them before starting:

```bash
SERVER_URL=ws://localhost:7654 SERVER_SECRET=abc123 node MyFirstBot.js
```

## Module Formats

The package ships both **ESM** and **CommonJS** outputs:

```js
// ESM (recommended)
import { Bot } from "@robocode.dev/tank-royale-bot-api";

// CommonJS
const { Bot } = require("@robocode.dev/tank-royale-bot-api");
```

TypeScript declarations (`.d.ts`) are included for both formats.

## Documentation & Resources

- **📖 Official Documentation**: [robocode-dev.github.io/tank-royale](https://robocode-dev.github.io/tank-royale/)
- **TypeScript API Reference**: https://robocode-dev.github.io/tank-royale/api/typescript/
- **Getting Started**: https://robocode-dev.github.io/tank-royale/tutorial/getting-started.html
- **🤖 My First Bot (TypeScript)**: https://robocode-dev.github.io/tank-royale/tutorial/typescript/my-first-bot-for-typescript
- **Sample Bots**: https://github.com/robocode-dev/tank-royale/tree/main/sample-bots/typescript
- **Source Code**: https://github.com/robocode-dev/tank-royale/tree/main/bot-api/typescript

## Supported Platforms

Robocode Tank Royale runs on:

- **Windows**
- **macOS**
- **Linux**

The TypeScript/JavaScript Bot API works with **Node.js 22 (LTS)** or higher on all supported platforms.

## Community & Support

- **GitHub Issues**: [Report bugs and request features](https://github.com/robocode-dev/tank-royale/issues)
- **Discussions**: [Community discussions and help](https://github.com/robocode-dev/tank-royale/discussions)
- **Contributing**: Check out our [Contributing Guide](https://github.com/robocode-dev/tank-royale/blob/master/CONTRIBUTING.md)

## Development Status

🚧 **Work in Progress**: The TypeScript/JavaScript Bot API is currently under active development.
Features and APIs may change before the stable release.

## License

Licensed under the [Apache License 2.0](https://github.com/robocode-dev/tank-royale/blob/main/LICENSE)

## Copyright

Copyright © 2022 [Flemming N. Larsen](https://github.com/flemming-n-larsen)

---

Ready to build the best tank and destroy the rest? Start coding your bot today! 🚀🎯
