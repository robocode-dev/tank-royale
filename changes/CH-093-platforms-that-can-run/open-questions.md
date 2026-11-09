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

Recommendation: the bot repository's own prerequisites, which are Java 25, .NET SDK 10, Node.js 24 with npm, and Python 3.11. The program's own Python Bot API accepts Python 3.10, but the Rumble's templates ask for 3.11.

## May the desktop program start bots in that folder with the environment first on their path?

Recommendation: yes, only for bots in a folder that holds the environment the screen created, by putting it first on the bot's path and setting `RUMBLE_PYTHON`. Without it, launchers that call `python` would keep using the Python that lacks the Bot API.
