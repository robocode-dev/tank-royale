# Booter JSON config file

All bot directories must contain a [JSON](https://fileinfo.com/extension/json) file, which is basically a description
of the bot.

For example, the bot MyFirstBot is accompanied by a _MyFirstBot.json_ file.

MyFirstBot.json for .Net:
```json
{
  "name": "My First Bot",
  "version": "1.0",
  "gameTypes": "melee, classic, 1v1",
  "authors": "Mathew Nelson, Flemming N. Larsen",
  "description": "A sample bot that is probably the first bot you will learn about.",
  "homepage": "",
  "countryCodes": "us, dk",
  "platform": ".Net 5.0",
  "programmingLang": "C# 8.0"
}
```
These fields are required:

*   name
*   version
*   gameTypes
*   authors

The remaining fields are all optional, but recommended.

Meaning of each field in the JSON file:

* **name:** is the display name of the bot.
* **version:** is the version of the bot, where [SEMVER](https://semver.org/) is the recommended format, but not a
  requirement.
* **gameTypes:** is a comma-separated list of the game types that bot supports (see below).
* **authors:** is a comma-separated list with the name of the bot author(s).
* **description:** is a brief description of the bot.
* **homepage:** is a link to a web page for the bot.
* **countryCodes:** is a comma-separated list of [Alpha-2](https://www.iban.com/country-codes) country codes for the
  bot author(s).
* **platform:** is the platform required for running the bot, e.g. Java 17 or .Net 5.0.
* **programmingLang:** is the programming language used for programming the bot, e.g. C# or Kotlin.

## Game types

Current standard game types are:

| Game type | Arena size  | Min. participants | Max. participants |
|:----------|:-----------:|:-----------------:|:-----------------:|
| classic   |  800 x 600  |         2         |    (unlimited)    |
| melee     | 1000 x 1000 |        10         |    (unlimited)    |
| 1v1       | 1000 x 1000 |         2         |         2         |

In the future, more game types might arrive. And it is also possible to define custom game types.