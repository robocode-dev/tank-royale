# Robocode Tankroyale sample bots for Java

This directory contains sample bots for Robocode Tankroyale for Java 11 and newer.

## Requirements for running the .Net sample bots

1. You need to unpack the archive or copy the directories in the root of your `bots` directory to run these with Robocode.
2. Java 11 (or newer) must be installed on your system. You can download it from here:https://www.oracle.com/java/technologies/downloads/. Note that you should download the JDK (Java Developer Kit) if you want to develop bots of your own.

## Bot directories

Each bot has its own subdirectory (bot directory) that contains:
* A Java source file (.java) that provides the program logic of the bot.
* A JSON file (.json) that provides information about the bot.
* Script files (.cmd and .sh) used for starting the bot.

## Bot filename convention

All filenames in a bot directory must share the same name as the directory containing the files, i.e. the filename
without the file extension. Robocode scans for specific file types sharing the same filenames to check if the directory
contains a bot or not, and to figure out how to run it, but also to provide information about the bot.

## Running a bot

A script file is used for running the bot, which is `java -cp ../lib/* <Java source file>` for the sample bots for Java.
The `java` command will automatically compile the bot and run it afterward as an executable.

You can run a sample bot manually from the command line by going into the bot directory (using the `cd` command) and writing:

    java -cp ../lib/* <Java source file>

Here `<Java source file>` is the name of the source file (.java) you want to run, e.g. MyFirstBot.java.