HUSK: Turn time limit

# Tank Royale vs orig. Robocode

**Robocode Tank Royale** is a new version of the [original version](https://robocode.sourceforge.io/) of Robocode.
Tank Royale has very similar rules as the original Robocode game. However, there are differences, and the goal of
Tank Royale is not to be compatible the old version. But instead, it is intended to be a better and improved version
of the original game and  be able to run Robocode on "any" programming language and via the Internet as well.
Note that *any* programming language can be used as long as it is able to use
[WebSocket](https://en.wikipedia.org/wiki/WebSocket).

Tank Royale is written from scratch as it is based on an entire different architecture and technology stack.
The game is now using a protocol on top of [WebSocket](https://en.wikipedia.org/wiki/WebSocket) for communication
between a server, the robots, and observers/controllers like e.g. a UI for displaying and controlling battles.

The game components has been written for the [Java platform](https://www.oracle.com/java/) with the
[Kotlin](https://kotlinlang.org/) programming language. But each component can be replaced with other platform and
programming language. This allows components to be replaced by any developer(s) to make variants or e.g. improved UI's
for the game etc., and it is possible to develop and *plug-in* observers e.g. for analysing or recording battles.

Tank Royale is following the same basic rules as the original game. However, game physics have been changed to fit
normal mathematics for angles and coordinate system.

Another goal of the new version Robocode is to make it much easier to develop and maintain as a platform.
It should also be possible to make a much more advanced version of Tank Royale in the future (version 2?), e.g. with
obstacles and items on the arena.

## Game components

The game has been split into separate components:

- Server, which is used for running games and enforce the rules.
- Graphical User Interface (UI) for starting, viewing, and controlling battles.
- Booter for booting up bots from a local machine.

Currently, these components require Java to be pre-installed.

## WebSocket

Bots can be run from anywhere as long as they have access to a [WebSocket](https://en.wikipedia.org/wiki/WebSocket) and
a server. Note that bots are not running on the server like with the original Robocode game. The server is only taking
care of hosting the game and its state. The server receives **intents** from bots, and sends out the current game state
for bots, observers, and controllers. Each bot is running in a process somewhere else besides the server. This could
be on the same local machine, but it could also be running within a browser session, a cloud server, or another user's
machine or device.

## Turn-based

Tank Royale is turn-based like the original version of Robocode. However, each turn in Tank Royale is
deterministic due to the fact, that bots are no longer running in independent threads inside a Java VM, where commands
from different robots for a turn was executed at "random".
Instead, Tank Royale evaluates the commands (intents) from all bots for a specific turn and outputs a new game state.
Hence, the game is stateful and deterministic, and no bot will get an advantage over other bots if a bot thread gets
more CPU or memory resources that its competitors on Java VM serving the bot.
This also makes test more stable if robots behave are truly deterministic for every turn.

## Bot intents

For each turn, bots will receive the state of the game. Note that the bots will only see the part of the world which
they are scanning using their radar. So bots will not be able to see _all_ other bots on the arena—only the bots they
have just scanned.

Bot commands are expressed by an **intent** containing these properties:

- **Turn rate** of the body in degrees per turn.
- **Gun turn rate** in degrees per turn.
- **Radar turn rate** in degrees per turn.
- **Target speed** in units per turn.
- **Firepower** used for firing the gun if there is no gun heat.

Setting one of these properties on a bot intent for a new turn means that the property should be changed. If the
property is not set (is omitted) it means that the last value for the property should be used for the next turn, i.e.
no change.

The turn rates can be both positive and negative, where a positive value means _turn to the left_ and a negative value
means _turn to the right_. The target speed of the bot can be positive and negative as well, where a positive speed
means _move forward_ and a negative speed means _move backward_.

## Cannot limit bot resources

Due to the fact that bots are running in their own processes independent of the server, the Tank Royale game will not be
able to constrain how much CPU, RAM, disk space etc. the bot is allowed to use. This was somewhat possible to some
extent with the original Robocode game, as all bots were running within the same Java VM.

## No Robot Packages are available

The original Robocode provided a Robot Packager, where Java robots could be packaged into robot JAR files as Robot
Packages, and .Net bots could be packaged into DLL files. This does not make sense the same way for Tank Royale, as bots
could be written for any language, platform, OS etc. But a way to package your bot could be to use
[container technology](https://www.docker.com/resources/what-container) and put your bot into container, e.g. using
[Docker](https://www.docker.com/).
