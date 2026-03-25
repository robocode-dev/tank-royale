# Tank Royale Bot API — TypeScript/JavaScript

[![npm](https://img.shields.io/npm/v/@robocode.dev/tank-royale-bot-api)](https://www.npmjs.com/package/@robocode.dev/tank-royale-bot-api)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://github.com/robocode-dev/tank-royale/blob/main/LICENSE)

TypeScript/JavaScript Bot API for [Robocode Tank Royale](https://robocode-dev.github.io/tank-royale/) — the next generation of the classic Robocode programming game.

## Requirements

- **Node.js 18+** (for running bots via WebSocket)
- **TypeScript 5+** (for writing bots in TypeScript)

## Installation

```bash
npm install @robocode.dev/tank-royale-bot-api
```

The `ws` package is required at runtime when running bots in a Node.js environment:

```bash
npm install ws
```

## Quick Start

```typescript
import { Bot, BotInfo } from "@robocode.dev/tank-royale-bot-api";

const bot = new Bot(
  new BotInfo(
    "MyFirstBot",
    "1.0",
    ["Author Name"],
    "My first bot",
    "en",
    [],
    [],
    "3"
  )
);

// Called at the start of each round
bot.run = () => {
  while (bot.isRunning()) {
    bot.forward(100);
    bot.turnGunRight(360);
    bot.back(100);
    bot.turnGunRight(360);
  }
};

// Called when the bot scans another bot
bot.onScannedBot = (event) => {
  bot.fire(1);
};

bot.start();
```

## API Overview

The API closely mirrors the Java and .NET Bot APIs. Key classes and interfaces:

| Export | Description |
|--------|-------------|
| `Bot` | Full bot with independent gun/radar movement |
| `BaseBot` | Low-level bot with event-driven control |
| `IBot` | Interface for `Bot` |
| `IBaseBot` | Interface for `BaseBot` |
| `Droid` | Marker interface for team droid bots |
| `BotInfo` / `BotInfoBuilder` | Bot metadata for handshake |
| `Constants` | Game constants (max speed, rates, etc.) |
| `GameSetup` | Arena and game configuration |
| `BotState` | Snapshot of bot state during a tick |
| `BulletState` | Snapshot of bullet state during a tick |
| `BotResults` | Final round/battle results |
| `Color` / `ColorUtil` | Color representation utilities |
| `IGraphics` / `SvgGraphics` | Graphics API for painting overlays |
| `DefaultEventPriority` | Default priorities for bot events |
| `EnvVars` | Environment variable names for configuration |

## Configuration via Environment Variables

The bot reads connection settings from environment variables (matching the Java/Python/.NET APIs):

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_URL` | `ws://localhost:7654` | WebSocket server URL |
| `BOT_SECRET` | _(none)_ | Secret for server authentication |

Set them before starting:

```bash
SERVER_URL=ws://localhost:7654 BOT_SECRET=abc123 node MyFirstBot.js
```

## Runtime Targets

| Target | Notes |
|--------|-------|
| **Node.js 18+** | Full support. Requires `ws` peer dependency. |
| **Browser** | Supported via native `WebSocket`. No `ws` needed. |
| **Deno / Bun** | Should work with Node.js-compatible WebSocket. |

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

