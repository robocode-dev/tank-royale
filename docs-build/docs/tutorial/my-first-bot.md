# My First Bot tutorial

## Introduction

This tutorial provides an introduction to creating your first bot for Robocode Tank Royale.

You might also have a look at the provided [sample bots] for Robocode for inspiration. You might also use the sample
bots to provide a starting template containing all the necessary files to create a bot for a specific programming
language and platform.

Note that this tutorial is aimed towards the official [APIs](../api/apis) available for Robocode Tank Royale provided on this
site.

## Initial setup

The first part of this tutorial is about the initial setup which is common for all bots that must be *booted* by the
game regardless of which programming language is used when developing the bot.

I recommend that you read about the [booter](../articles/booter.md) first before continuing on this tutorial as the
following assumes you are somewhat familiar with the file name conventions, and the concept of *bot directories*,
and *root directories*.

## Prepare a root directory

Robocode needs to locate your bot, which must be stored into its own *bot directory* under a *root directory*. The
purpose of the root directory is to contain one to many bot directories.

So the first step is to prepare a root directory which we name *bots*. Under Windows, you might create this folder
under `C:\bots` or `%userprofile%\bots`, and for macOS or Linux you might create a folder under `~/bots`.

If you use the UI for Robocode, you will need to add this root directory in the Bot Root Configuration so Robocode will
be able to locate your bot(s).

## Prepare a bot directory

Next, you should create a bot directory inside the *bots* directory for your first bot, which we name *MyFirstBot*, so
it will be located under `../bots/MyFirstBot`. All your bot files must be put into this folder and share the same file
name as the bot directory (more info in the [booter](../articles/booter.md) article).

## Create a JSON file or set properties in code

To provide the game with information about your bot (like its name and version), you have two options:

1. **Create a JSON file**: Create a `MyFirstBot.json` file in your bot directory (`../bots/MyFirstBot/MyFirstBot.json`).
2. **Set properties in code**: You can skip the JSON file and set these properties directly in your bot's source code.

### Using a JSON file

This is the recommended approach for beginners. Copy the following into `MyFirstBot.json`:

```json{2-4}
{
  "name": "My First Bot",
  "version": "1.0",
  "authors": [ "«enter your name»" ],
  "description": "My first bot",
  "homepage": "«insert link to a home page for your bot»",
  "countryCodes": [ "«enter your country code, e.g. us»" ],
  "platform": "«enter programming platform, e.g. Java or .NET»",
  "programmingLang": "«enter programming language, e.g. Java or C#»"
}
```
The fields `name`, `version` and `authors` are required.

### Setting properties in code

If you prefer to keep everything in your source code, you can skip the JSON file. However, you must then ensure that your
bot directory contains a file that the booter can recognize (like `MyFirstBot.jar`, `MyFirstBot.py`, or `MyFirstBot.csproj`)
so it knows how to start your bot.

Regardless of which method you choose, the fields `name`, `version`, and `authors` **must** be provided, or your bot will
fail to connect.

This concludes the common part of the tutorial, and the following depends on the platform of your choice.

## Select platform

The rest of the tutorial is split up based on the available APIs for different platforms:

- [.NET](dotnet/my-first-bot-for-dotnet.md)
- [Java / JVM](jvm/my-first-bot-for-jvm.md)
- [Python](python/my-first-bot-for-python.md)
- [TypeScript / JavaScript](typescript/my-first-bot-for-typescript.md)

[sample bots]: ../articles/installation#sample-bots "Sample bots"