---
id: CH-093-open-questions
type: open-questions
status: open
links: [CH-093]
title: The Bot API version, where the environment lives, the minimum versions and the change to how bots start
---

## Which Python Bot API version is installed?

The bots in the test environment name no version. The bot repository's templates pin 1.3.1. A Java bot's launcher fetches 1.1.0, and the program ships 1.4.0, the same version as its own server.

Recommendation: install the copy the program ships. It matches the server the bots play on, it needs no choice per bot, and the program already carries it. A bot that needs another version can be handled when one appears.

## Where does the Python environment live?

Recommendation: in the download folder, as a hidden folder of its own. It is then removed together with the downloaded bots, and it never touches the person's own Python or their own bots. The alternative is a folder in the person's home, shared by every download folder, which saves disk but outlives the bots.

## Which minimum versions are checked?

The maintainer pointed out that Rumble bots need Java 11 or 17, not 25. Checked: the Java Bot API is compiled for Java 11 (`options.release.set(11)` in the build, class files of version 55 in the 1.1.0 jar the launchers fetch). The launchers run the bot's source with `java -cp <api jar> <Bot>.java`, which works from Java 11. Of the test catalog's Java bots, nine declare Java 17, five declare Java 25 and two declare nothing, and the template declares Java 25, as does the bot repository's prerequisite table.

Recommendation: Java 17, because most bots declare it and a bot written for 17 can fail to compile on 11. Java 11 is the floor of the Bot API itself and would tick the platform on more computers. .NET SDK 10, Node.js 24 with npm and Python 3.11 follow the bot repository's prerequisites. The program's own Python Bot API accepts Python 3.10, but the Rumble's templates ask for 3.11. The bot repository's prerequisite of Java 25 is stricter than the bots need. Correcting it there is outside this change.

## May the desktop program start bots in that folder with the environment first on their path?

Recommendation: yes, only for bots in a folder that holds the environment the screen created, by putting it first on the bot's path and setting `RUMBLE_PYTHON`. Without it, launchers that call `python` would keep using the Python that lacks the Bot API.
