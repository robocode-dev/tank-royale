# Sample bots for Robocode Tank Royale for C# / .Net

This directory contains sample bots for Robocode Tank Royale developed for C# programming language and .Net platform.

## Requirements for running the sample bots

1. You need to unpack the archive or copy the directories in the root of your `bots` directory to run these with
   Robocode.
2. .Net 6 (or newer) must be installed on your system. You can download it from here:
   https://dotnet.microsoft.com/download

Note that you need the SDK (Software Development Kit) if you want to develop your own bot.

## Bot directories

Each bot has its own subdirectory (bot directory) that contains:

* A C-sharp source file (.cs) that provides the program logic of the bot.
* A JSON file (.json) that provides information about the bot.
* A C-sharp project source file (.csproj) used for compiling and running the bot with the `dotnet` command.
* Script files (.cmd and .sh) used for starting the bot.

## Running a bot

A script file is used for running the bot, which is `dotnet run` for the sample bots for .Net. The project source file
for C# (.csproj) provide the `dotnet` command with information about how to run the bot, e.g. using the NuGet package
for the bot API. The `dotnet` command will automatically compile the bot and run it afterward as an executable.

You can run a sample bot manually from the command line by going into the bot directory (using the `cd` command) and
writing:

    dotnet run

## Slow boot up the first time

Note that when you run a sample bot for .Net for the first time, it takes some additional time to build before it is
ready to join the battle. Hence, it might take a while before the bot becomes available on the list of Joined Bots on
the GUI. So please be patient.