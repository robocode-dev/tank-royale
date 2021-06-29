# Tank Royale—a new era

## About Tank Royale

Robocode Tank Royale is a new version of the original version of Robocode where the new version has almost the same rules as the original Robocode game. However, there are differences, and the two versions are not compatible.

Tank Royale is written from scratch and instead of basic it on the source code for the original game. This was necessary, as the new version required an entirely different architecture.

There has been no intention for keeping Tank Royale backwards compatible with the original version. Instead, Tank Royale is following the same rules as the original game. However, game physics have been changed to better fit normal math.

The goal of the new version is to make it much easier to develop and maintain. It should also be possible to make a much more advanced version of Tank Royale in the future (version 2?), e.g. with obstacles and items on the arena.

## Protocol based

With the original version of Robocode bots are run as a Java program on a Java Runtime Environment (JRE). This was also the case when running .Net based bots using the .NET plugin available for the game. The commands from .Net bots were mapped into equivalent commands on the Java side using a _JNI to .Net_ bridge named [jni4net](http://jni4net.com/). The jni4net bridge was developed by Pavel Savara, who has been a big contributor on the original game.

With Tank Royale, Robocode has been changed to be protocol-based using [WebSocket](https://en.wikipedia.org/wiki/WebSocket) as the communication channel. A protocol has been defined for Tank Royale for bots as well as observers/controllers. Hence, bots can be written in any language and platform supporting WebSocket. The same is the case for the game itself.

Using [WebSocket](https://en.wikipedia.org/wiki/WebSocket) also makes it possible to run bots from a web browser using, e.g. JavaScript.

## Modularized

The game has been split into separate components:

- Server, which is used for running games and enforce the rules.
- Graphical User Interface (UI) for starting, viewing, and controlling battles.
- Booter for booting up bots from a local machine.

Currently, these components have been developed using Kotlin and Java as programming languages and do require Java to be installed. But it is possible to develop these components in other programming languages and platforms. So the ones provided with Tank Royale serves as a [reference implementation](https://en.wikipedia.org/wiki/Reference_implementation). Other developers may create their own variant of one or more components, as long as they stay compatible with the original Tank Royale game.

## Hosting bots

Bots can be run from everywhere as long as they have access to a [WebSocket](https://en.wikipedia.org/wiki/WebSocket) and has access to a server. Note that **bots are not running on the server**. The server is only hosting the game state, receives intents from bots, and sends out the current game state for bots, observers, and controllers. The bot is running in a process somewhere else besides the server. This could be on the same local machine, but it could also be running within a browser session, a cloud server, or another user's machine.

## Deterministic turns

Tank Royale is turn-based like the original version of Robocode. Each turn in Tank Royale is deterministic due to the fact, that bots are no longer running in independent threads inside a Java VM. Instead, Tank Royale evaluates the commands from all bots for a specific turn and outputs a new game state. Hence, the game is stateful and deterministic, and no bot will get an advantage over other bots if a bot thread gets more CPU power on the host serving the bot.

## Bot intents

For each turn, bots will receive the state of the game. Note that the bots will only see the part of the world which they are scanning using their radar. So bots will not be able to see _all_ other bots on the arena—only the bots that have just scanned.

Bot commands are expressed by an **intent** containing these properties:

- **Turn rate** of the body in degrees per turn.
- **Gun turn rate** in degrees per turn.
- **Radar turn rate** in degrees per turn.
- **Target speed** in units per turn.
- **Firepower** used for firing the gun if there is no gun heat.

Setting one of these properties on a bot intent for a new turn means that the property should be changed. If the property is not set (is omitted) it means that the last value for the property should be used for the next turn, i.e. no change.

The turn rates can be positive and negative, where a positive value means _turn to the left_ and a negative value means _turn to the right_. The target speed of the bot can be positive and negative as well, where a positive speed means _move forward_ and a negative speed means _move backward_.

## Not possible to limit bot resources

Due to the fact that bots are running in their own processes, the Tank Royale game will not be able to tell how much CPU, RAM, disk space etc. the bot is using. This was somewhat possible with the original Robocode game, as all bots were running within the same Java VM.

## Not possible to provide a packager

The original Robocode provided a Robot Packager, where Java robots could be packaged into robot JAR files, and .Net bots could be packaged into DLL files. This does not make sense for Tank Royale, as bots could be written for any language, platform, OS etc. However, one recommendation is to package your bot into a container using e.g. [Docker](https://www.docker.com/).
