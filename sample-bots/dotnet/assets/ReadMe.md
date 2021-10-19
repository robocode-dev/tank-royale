# Robocode Tankroyale sample bots for .Net

This directory contains sample bots for Robocode Tankroyale for .Net 5 and newer.

## Requirements for running the .Net sample bots

1. You need to unpack the archive or copy the directories in the root of your `bots` directory to run these with Robocode.
2. Net 5 (or newer) must be installed on your system. You can download it from here: https://dotnet.microsoft.com/download
3. You need to install the NuGet package containing the Robocode Tankroyale Bot API for .Net named `robocode.tankroyale.botapi.x.y.z.nupkg`, where `x.y.z` is the version, e.g. 1.0.0.

## Bot directories

Each bot has its own subdirectory (bot directory) that contains:
* A C-sharp source file (.cs) that provides the program logic of the bot.
* A JSON file (.json) that provides information about the bot.
* A C-sharp programming project file (.csproj) used for running the bot with the `dotnet` command.
* Script files (.cmd, .ps1, .sh) used for starting the bot.

## Bot filename convention

All filenames in a bot directory must share the same name as the directory containing the files, i.e. the filename
without the file extension. Robocode scans for specific file types sharing the same filenames to check if the directory
contains a bot or not, and to figure out how to run it, but also to provide information about the bot.

## Running a bot

A script file is used for running the bot, which is `dotnet run` for the sample bots for .Net.
The project file for C# (.csproj) provide the `dotnet` command with information about how to run the bot, e.g. using the
NuGet package for the bot API.
The `dotnet` command will automatically compile the bot and run it afterward as an executable.

You can run a sample bot manually from the command line by going into the bot directory (using the `cd` command) and writing:

    dotnet run
