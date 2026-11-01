---
id: CH-073-open-questions
type: open-questions
status: resolved
links: [CH-073]
title: Which files to take, the line endings and where the content hash lives
---

## Which files go into the copy?

The first proposal listed folders to leave out. The maintainer pointed out that it is simpler and safer to take only what a bot is made of: its configuration file, launcher scripts and source files. Looking at the 16 published bots and the starter templates confirmed it: they hold the configuration file, the two launchers, the source files and, for the starters, the dependency declaration file of the language (`.csproj`, `requirements.txt`, or `package.json` with `package-lock.json`), and nothing else.

Resolved by the maintainer on 2026-11-01: take only the configuration file, the launchers and the source files, plus the declaration files and an optional license file that the language needs. Everything else is listed as not included.

## Should the line endings be changed in the copy?

A Windows author's files often have Windows line endings, and a launcher script with them cannot be run by a Unix shell; the failure looks like a mystery. The first proposal converted only the two launchers. The maintainer's rule is that Tank Royale and the Rumble use Unix line endings, not Windows ones, so the files should be converted when a bot is uploaded. A check of the published bots and starters found Unix line endings in every file.

Resolved by the maintainer on 2026-11-01: convert every included file to Unix line endings in the copy and list the conversions. For a bot sent without the program, see the section on submissions made without the program: the bot repository needs its own rule, which is a separate change.

## Should the content hash move into the bot project library?

Staging must report the hash the bot repository will record, and the rule for it is today in the Rumble library. The recommendation is to move it into the bot project library, which the Rumble library already is planned to use, so there is one implementation and the dependency diagram holds. The alternative is to keep a second copy in the bot project library, which risks the two drifting apart.

Resolved by the maintainer on 2026-11-01: the content hash is computed in the bot project library.
