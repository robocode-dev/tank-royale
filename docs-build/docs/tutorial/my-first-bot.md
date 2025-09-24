# My First Bot tutorial

## Introduction

This tutorial provides an introduction to creating your first bot for Robocode Tank Royale.

You might also have a look at the provided [sample bots] for Robocode for inspiration. You might also use the sample
bots to provide a starting template containing all the necessary files to create a bot for a specific programming
language and platform.

Note that this tutorial is aimed towards the official [APIs](../api/apis.md) available for Robocode Tank Royale provided on this
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

## Create a JSON file for bot info

A [JSON](https://fileinfo.com/extension/json) file is used for providing the game with information about your bot. You
must create a MyFirstBot.json file and put this into your bot directory, i.e. into
`../bots/MyFirstBot/MyFirstBot.json`.

This is the content of the JSON file, which you can copy and paste into the file:

```json{2-4}
{
  "name": "My First Bot",
  "version": "1.0",
  "authors": [ "«enter your name»" ],
  "description": "My first bot",
  "homepage": "«insert link to a home page for your bot»",
  "countryCodes": [ "«enter your country code, e.g. us»" ],
  "platform": "«enter programming platform, e.g. Java or .Net»",
  "programmingLang": "«enter programming language, e.g. Java or C#»"
}
```
The fields `name`, `version` and `authors` are required, and the rest of the fields are optional.

Note that the `authors` field should contain your full name, nickname, or handle, which identifies you.
The `platform` and `programmingLang` depends on your choice of programming language and platform. For example, the
platform could be Java 17, and the programming Language Kotlin 1.8.20 or Java 17.
Or the platform could be .Net 6.0, and the programming language C# 10.0 or F# 6.0.

This concludes the common part of the tutorial, and the following depends on the platform of your choice.

## Select platform

The rest of the tutorial is split up based on the available APIs for different platforms:

- [.Net](dotnet/my-first-bot-for-dotnet.md)
- [Java / JVM](jvm/my-first-bot-for-jvm.md)

[sample bots]: ../articles/installation#sample-bots "Sample bots"