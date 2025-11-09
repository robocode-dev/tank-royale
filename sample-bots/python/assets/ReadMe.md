# Sample bots for Robocode Tank Royale for Python

This directory contains sample bots for Robocode Tank Royale developed for the Python programming language.

## Requirements for running the sample bots

1. Python 3.10 (or newer) must be installed on your system. You can download it from here:
   https://www.python.org/downloads/
2. You need to unpack the archive or copy the directories in the root of your `bots` directory to run these with
   Robocode.

Note that you need Python (and pip) if you want to develop your own bot.

## Bot directories

Each bot has its own subdirectory (bot directory) that contains:

* A Python source file (.py) that provides the program logic of the bot.
* A JSON file (.json) that provides information about the bot.
* Script files (.cmd and .sh) used for starting the bot.

## Running a bot

A script file is used for running the bot, which is `python <Python source file>` for the sample bots for Python.

You can run a sample bot manually from the command line by going into the bot directory (using the `cd` command) and
writing:

    python <Python source file>

For example:

    python SpinBot.py

(assuming you are standing in the `SpinBot` bot directory)

## Slow boot up the first time

Note that when you run a sample bot for Python for the first time, dependencies might need to be installed (using the
provided `install-dependencies` scripts), which can take some additional time before it is ready to join the battle.
Hence, it might take a while before the bot becomes available on the list of Joined Bots on the GUI. So please be patient.
