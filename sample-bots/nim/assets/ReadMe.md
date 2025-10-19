# Sample bots for Robocode Tank Royale for Nim

This directory contains sample bots for Robocode Tank Royale developed for the Nim programming language.

## Requirements for running the sample bots

1. Nim must be installed on your system. You can download it from here:
   https://nim-lang.org/install.html
2. You need to unpack the archive or copy the directories in the root of your `bots` directory to run these with
   Robocode.

Note that you need the Nim compiler if you want to develop your own bot.

## Bot directories

Each bot has its own subdirectory (bot directory) that contains:

* A Nim source file (.nim) that provides the program logic of the bot.
* A JSON file (.json) that provides information about the bot.
* Script files (.cmd and .sh) used for starting the bot.

## Running a bot

A script file is used for running the bot, which is `nim c --run --path:../lib <Nim source file>` for the sample bots for Nim.
The `nim` command will automatically compile the bot and run it afterward as an executable.

You can run a sample bot manually from the command line by going into the bot directory (using the `cd` command) and
writing:

    nim c --run --path:../lib <Nim source file>

Here `<Nim source file>` is the name of the source file (.nim) you want to run, e.g. MyFirstBot.nim.

For example:

    nim c --run --path:../lib Corners.nim

(assuming you are standing in the `Corners` bot directory)