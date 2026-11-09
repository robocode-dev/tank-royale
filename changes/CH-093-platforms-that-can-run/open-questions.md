---
id: CH-093-open-questions
type: open-questions
status: resolved
links: [CH-093]
title: The Bot API version, where the environment lives, the minimum versions and the change to how bots start
---

## Which Python Bot API version is installed?

The bots in the test environment name no version. The bot repository's templates pin 1.3.1. A Java bot's launcher fetches 1.1.0, and the program ships 1.4.0, the same version as its own server.

Recommendation: install the copy the program ships. It matches the server the bots play on, it needs no choice per bot, and the program already carries it. A bot that needs another version can be handled when one appears.

Decided by the maintainer: install the copy the program ships. Checked: the desktop program's jar carries the Python Bot API as a wheel of the program's own version, which needs Python 3.10 or newer.

## Where does the Python environment live?

Decided by the maintainer: in the download folder, as a hidden folder of its own, and the screen sets it up again when it no longer starts after a Python upgrade or after the folder was moved. The recommendation was: in the download folder, as a hidden folder of its own. It is then removed together with the downloaded bots, and it never touches the person's own Python or their own bots. The alternative is a folder in the person's home, shared by every download folder, which saves disk but outlives the bots.

## Which minimum versions are checked?

The maintainer pointed out that Rumble bots need Java 11 or 17, not 25, and that the supported versions must be stated in one central place, not spread out, so an upgrade changes one place. Checked: the Java Bot API is compiled for Java 11, and the launchers run a bot's source with `java` directly, which works from Java 11. But the Rumble already has one list: the Rumble client's runtime file, from which its container image is built and its README block is refreshed. Moderation runs every submitted bot in that image, so the bots in the catalog have run on exactly those versions, today Java 25, .NET 10, Python 3.14 and Node.js 24, and on nothing older. A bot's `programmingLang` field is free text that nothing checks.

Decided by the maintainer: the Rumble follows the newest LTS of each platform, and players must upgrade when the Rumble moves to a new LTS, because otherwise nobody can promise that the bots run on their computer. The copies spread over the other repositories are cleaned up to point to the Rumble client's list.

Decided by the maintainer: the screen reads that file and checks at least its major versions, and this program names no version itself. The file is read from the Rumble client's latest release, because that is what players run; its main branch may hold a refresh that is not released yet. The environment's engine pin names no client version, so the release is the one place left. The Rumble moves to a new Python version only at its first bug-fix release, x.y.1, which belongs to the Rumble client's refresh and not to this change. Whether the Rumble should promise a lower floor instead, so that players need not upgrade every time the image does, is a decision for the Rumble's own policy and its moderation, outside this change. The copies of the versions that are spread over the bot repository's checks, its templates and the client's own build checks are cleaned up separately in those repositories.

## May the desktop program start bots in that folder with the environment first on their path?

Decided by the maintainer: yes. The recommendation was: yes, only for bots in a folder that holds the environment the screen created, by putting it first on the bot's path and setting `RUMBLE_PYTHON`. Without it, launchers that call `python` would keep using the Python that lacks the Bot API.
