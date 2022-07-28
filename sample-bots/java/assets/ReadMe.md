# Sample bots for Robocode Tank Royale for Java / JVM

This directory contains sample bots for Robocode Tank Royale developed for the Java programming language and JVM
platform.

## Requirements for running the sample bots

1. You need to unpack the archive or copy the directories in the root of your `bots` directory to run these with
   Robocode.
2. Java 11 (or newer) must be installed on your system. You can download it from here:
   https://www.oracle.com/java/technologies/downloads/.

Note that you need the JDK (Java Development Kit) if you want to develop your own bot.

## Bot directories

Each bot has its own subdirectory (bot directory) that contains:

* A Java source file (.java) that provides the program logic of the bot.
* A JSON file (.json) that provides information about the bot.
* Script files (.cmd and .sh) used for starting the bot.

## Running a bot

A script file is used for running the bot, which is `java -cp ../lib/* <Java source file>` for the sample bots for Java.
The `java` command will automatically compile the bot and run it afterward as an executable.

You can run a sample bot manually from the command line by going into the bot directory (using the `cd` command) and
writing:

    java -cp ../lib/* <Java source file>

Here `<Java source file>` is the name of the source file (.java) you want to run, e.g. MyFirstBot.java.

For example:

    java -cp ../lib/* Corners.java

(assuming you are standing in the `Corners` bot directory)