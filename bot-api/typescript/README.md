# Tank Royale Bot API — TypeScript/JavaScript

[![npm](https://img.shields.io/npm/v/@robocode.dev/tank-royale-bot-api)](https://www.npmjs.com/package/@robocode.dev/tank-royale-bot-api)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/robocode-dev/tank-royale/blob/main/LICENSE)

TypeScript/JavaScript Bot API for [Robocode Tank Royale](https://robocode-dev.github.io/tank-royale/) — the next generation of the classic Robocode programming game.

## Requirements

- **Node.js 22 (LTS) or newer**
- **TypeScript 5+** (for writing bots in TypeScript)

## Installation

```bash
npm install @robocode.dev/tank-royale-bot-api
```

The `ws` package is required at runtime in a Node.js environment:

```bash
npm install ws
```

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

The bot reads its name, version, and authors from a `MyFirstBot.json` file placed alongside the source, or from
environment variables set by the Robocode booter. You can also supply them programmatically via the `BotInfo`
constructor — see the [tutorial](https://robocode-dev.github.io/tank-royale/tutorial/typescript/my-first-bot-for-typescript)
for the full walkthrough.

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

**Team settings (set by the booter for team bots):**

| Variable       | Description         |
|----------------|---------------------|
| `TEAM_ID`      | Numeric team ID     |
| `TEAM_NAME`    | Team name           |
| `TEAM_VERSION` | Team version string |

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

## Links

- 📖 [Documentation](https://robocode-dev.github.io/tank-royale/)
- 🚀 [Getting Started](https://robocode-dev.github.io/tank-royale/tutorial/getting-started)
- 💻 [Source Code](https://github.com/robocode-dev/tank-royale/tree/main/bot-api/typescript)
- 🐛 [Issue Tracker](https://github.com/robocode-dev/tank-royale/issues)

## License

[Apache License 2.0](https://github.com/robocode-dev/tank-royale/blob/main/LICENSE)
