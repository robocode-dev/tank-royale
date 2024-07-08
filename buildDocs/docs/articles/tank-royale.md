# Tank Royale vs original Robocode

## Same, but different

**Robocode Tank Royale** is a new version of the [original version](https://robocode.sourceforge.io/) of Robocode. Tank
Royale has very similar rules as the original Robocode game. However, there are differences and the goal of Tank Royale
is not to be compatible with the old version. But instead, it is intended to be a better and improved version of the
original game and be able to run Robocode on "any" programming language and via the Internet as well. Note that *any*
programming language can be used as long as it can use
[WebSocket](https://en.wikipedia.org/wiki/WebSocket).

## WebSocket based

Tank Royale is written from scratch as it is based on an entirely different architecture and technology stack. The game
is now using a protocol on top of [WebSocket](https://en.wikipedia.org/wiki/WebSocket) for communication between a
server, the bots, and observers/controllers like e.g. a UI for displaying and controlling battles.

## Modularized

The game components have been written for the [Java platform](https://www.oracle.com/java/) with the
[Kotlin](https://kotlinlang.org/) programming language. But each component can be replaced with another platform and
programming language. This allows components to be replaced by any developer(s) to make variants or e.g. improved UI's
for the game etc., and it is possible to develop and *plug-in* observers e.g. for analyzing or recording battles.

## Use "normal" maths

Tank Royale is following the same basic rules as the original game. However, game physics have been changed to fit
normal mathematics for angles and coordinate systems.

Another goal of the new version Robocode is to make it much easier to develop and maintain as a platform. It should also
be possible to make a much more advanced version of Tank Royale in the future (version 2?), e.g. with obstacles and
items on the arena.

## Game components

The game has been split into separate components:

- [Server] used for running games and enforcing the rules.
- [GUI] (Graphical User Interface) for starting, viewing, and controlling battles.
- [Booter] for booting up bots from a local machine.

## Run bots from anywhere

Bots can be run from anywhere as long as they have access to a [WebSocket] and a server. Note that bots are not running
on the server like with the original Robocode game. The server is only taking care of hosting the game and its state.
The server receives **intents** from bots and sends out the current game state for bots, observers, and controllers.

Each bot is running in a process somewhere else besides the server. This could be on the same local machine, but it
could also be running within a browser session, a cloud server, or another user's machine or device.

## Turn-based

Tank Royale is turn-based like the original version of Robocode. However, each turn in Tank Royale is deterministic due
to the fact, that bots are no longer running in independent threads inside a Java VM, where commands from different
bots for a turn were executed at "random". Instead, Tank Royale evaluates the commands (intents) from all bots for a
specific turn and outputs a new game state.

Hence, the game is stateful and deterministic, and no bot will get an advantage over other bots if a bot thread gets
more CPU or memory resources than its competitors on Java VM serving the bot. This also makes the test more stable if
bots behave in a truly deterministic manner for every turn.

## Bot intents

For each turn, bots will receive the state of the game. Note that the bots will only see the part of the world that they
are scanning using their radar. So bots will not be able to see _all_ other bots on the arena—only the bots they have
just scanned.

Bot commands are expressed by an **intent** containing these properties:

- **Turn rate** of the body in degrees per turn.
- **Gun turn rate** in degrees per turn.
- **Radar turn rate** in degrees per turn.
- **Target speed** in units per turn.
- **Firepower** is used for firing the gun if there is no gun heat.

Setting one of these properties on a bot intent for a new turn means that the property should be changed. If the
property is not set (is omitted) it means that the last value for the property should be used for the next turn, i.e. no
change.

The turn rates can be both positive and negative, where a positive value means _turn to the left_ and a negative value
means _turn to the right_. The target speed of the bot can be positive and negative as well, where a positive speed
means _move forward_ and a negative speed mean _move backward_.

## Time limit for a turn

With Tank Royale, it is possible to specify the turn time. The turn time is specified in microseconds (μs) and can be
configured for a battle. If a bot exceeds the turn time, it will be punished with "a skipped turn", and its intention
for the turn will not be executed until the next or a later turn depending on how much time the bot spends before
sending its intent to the server.

## Cannot limit bot resources

Since bots are running in their processes independent of the server, the Tank Royale game will not be able to constrain
how much CPU, RAM, disk space, etc. the bot is allowed to use. This was somewhat possible to some extent with the
original Robocode game, as all bots were running within the same Java VM sharing the same resources.

## No built-in editor or compiler

The GUI for Tank Royale does not provide a source code editor and a compiler like the original Robocode. It is up to the
bot developers to find a suitable editor, and the compiler depends on which programming language will be used for
developing a bot. With the original Robocode, Java was the primary programming language and platform.

### Run bots directly from the source code

Note that the sample bots that come with Robocode Tank Royale for Java and .Net contain no binary files—only the
source files. However, both the `java` and `dotnet` commands used for running the bots can compile and run the bots
directly from the source code. And hence, a compiler is not needed as long as a Java Runtime Environment (JRE) (version
11 or newer) or .Net Core 6.0 or newer is installed.

## No Robot Packager is available

The original Robocode provided a Robot Packager, where Java robots are packaged into robot Java archive files as
_Robot Packages_ and .Net bots were packaged into DLL files.

With Robocode Tank Royale, you should consider packaging all required files for a bot into a zipped file and provide
scripts for starting up your bot the same way as for the provided sample bots. You can read more about this with
the [booter].


[WebSocket]: https://en.wikipedia.org/wiki/WebSocket "WebSocket"

[Server]: https://github.com/robocode-dev/tank-royale/tree/master/server#readme "Server"

[booter]: ../articles/booter.md "Booter"

[GUI]: ../articles/gui.md "GUI application"
