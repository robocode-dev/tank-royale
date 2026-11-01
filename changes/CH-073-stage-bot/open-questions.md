---
id: CH-073-open-questions
type: open-questions
status: open
links: [CH-073]
title: Build folders, launcher line endings and where the content hash lives
---

## Which files go into the copy?

The first proposal listed folders to leave out. The maintainer pointed out that it is simpler and safer to take only what a bot is made of: its configuration file, launcher scripts and source files. Looking at the 16 published bots and the starter templates confirmed it: they hold the configuration file, the two launchers, the source files and, for the starters, the dependency declaration file of the language (`.csproj`, `requirements.txt`, or `package.json` with `package-lock.json`), and nothing else.

Resolved by the maintainer on 2026-11-01: take only the configuration file, the launchers and the source files, plus the declaration files and an optional license file that the language needs. Everything else is listed as not included.

## Should the launcher script's line endings be changed in the copy?

A Windows author's `.sh` launcher often has Windows line endings, which a Unix shell cannot run, and the failure looks like a mystery. The recommendation is to convert the two launcher scripts named after the bot to Unix line endings in the staged copy only, to list the conversion in the description, and to leave every other file alone. The alternative is to report the line endings as a problem and make the author fix them, which they may not know how to do on Windows.

## Should the content hash move into the bot project library?

Staging must report the hash the bot repository will record, and the rule for it is today in the Rumble library. The recommendation is to move it into the bot project library, which the Rumble library already is planned to use, so there is one implementation and the dependency diagram holds. The alternative is to keep a second copy in the bot project library, which risks the two drifting apart.
