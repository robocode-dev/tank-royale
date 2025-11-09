# Robocode Tank Royale

<img src="gfx/Logo/Robocode-logo.svg" width="220" alt="Robocode logo">

**Build the best – destroy the rest!**

## About

Robocode is a [programming game](https://en.wikipedia.org/wiki/Programming_game) where the goal is to code a bot in the
form of a virtual tank that competes against other bots in a virtual battle arena.

The player writes a program that controls the bot’s movement, scanning, firing and reactions to events during a battle.
All logic lives inside this program – you never control the bot directly.

The name **Robocode** is short for “Robot code” and comes from the original version
[here](https://robocode.sourceforge.io/).  
**Robocode Tank Royale** is the next evolution, where bots can play over the Internet via WebSocket.

This project aims to help you learn programming, improve AI skills in a fast‑running real‑time game,
and have fun while competing.

## Example of a battle

<img src="docs-build/docs/images/robocode-battle-anim.gif" alt="GIF animation of tanks battling each other on a 2D battlefield">

## Documentation

Main page:  
[Robocode Tank Royale Docs](https://robocode-dev.github.io/tank-royale/)

### Try it out

If you are new to Robocode or just need a refresher, start with the
[Getting Started guide](https://robocode-dev.github.io/tank-royale/tutorial/getting-started).  
For hands‑on coding, continue to the
[My First Bot tutorial](https://robocode-dev.github.io/tank-royale/tutorial/my-first-bot.html).

The [Installation guide](https://robocode-dev.github.io/tank-royale/articles/installation.html) covers how to install the GUI,
and includes sample bots for quick demos.

## Supported platforms

Robocode runs on Java 11 or newer and supports Windows, macOS and Linux out of the box.  
Bot APIs are available for:

- **Python**
- **Java (JVM)**
- **.NET**

Bots can be written in any language that can access a WebSocket API and follows the
[protocol](https://github.com/robocode-dev/tank-royale/tree/master/schema/schemas#readme).

The following Bot APIs provide full client implementations:

| Language | API |
|----------|-----|
| **Python** | [Python Bot API](https://robocode-dev.github.io/tank-royale/api/python/) |
| **Java (JVM)** | [Java/JVM Bot API](https://robocode-dev.github.io/tank-royale/api/apis.html#java-jvm) |
| **.NET** | [.NET Bot API](https://robocode-dev.github.io/tank-royale/api/apis.html#net) |

Additional languages supported by the Java/JVM API:

- Java
- Groovy
- Kotlin
- Scala
- Jython
- Clojure

Supported .NET languages:

- C#
- F#
- Visual Basic
- IronPython

## Work in progress

- **Bot API for WebAssembly (Wasm)**
    - JavaScript
    - TypeScript
- **Robocode API bridge for Tank Royale** – see the [robocode-api-bridge](https://github.com/robocode-dev/robocode-api-bridge) project.

## Thanks to the contributors

Huge thanks to every [contributor](https://github.com/robocode-dev/tank-royale/graphs/contributors) — you make this
project shine! 🙌

## Maintainer

[@flemming-n-larsen](https://github.com/flemming-n-larsen)

[![Buy Me A Coffee](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://buymeacoffee.com/flemming.n.larsen)

## License

[Apache License 2.0](LICENSE)

## Copyright

Copyright © 2022 [Flemming N. Larsen](https://github.com/flemming-n-larsen)