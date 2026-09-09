# Sample bots for Robocode Tank Royale for Python

This directory contains sample bots for Robocode Tank Royale developed for the Python programming language.

## Requirements for running the sample bots

1. Python 3.10 (or newer), including the `venv` module, must be installed on your system. You can download it from here:
   https://www.python.org/downloads/
   On Debian or Ubuntu, install the matching `python3-venv` package if the `venv` module is not included.
2. You need to unpack the archive or copy the directories in the root of your `bots` directory to run these with Robocode.

Each archive creates a `deps/venv` virtual environment on its first run and installs the bot dependencies there. No global Python package installation is required.

## Bot directories

Each bot has its own subdirectory (bot directory) that contains:

* A Python source file (.py) that provides the program logic of the bot.
* A JSON file (.json) that provides information about the bot.
* Script files (.cmd and .sh) are optional for starting the bot.

## Running a bot

The bot can be run by Robocode without any script file by using the information in the bot's JSON file.
By default, the booter assumes that the base Python script has the same name as the bot directory.
If the base script has a different name, it must be specified using the `base` property in the JSON file.
However, a script file can still be used for running the bot. The generated `.sh` and `.cmd` scripts install dependencies into `deps/venv` and use that interpreter.

If a bot directory contains a generated launcher script, you can start it by going into its directory and running the script. For example:

    ./MyFirstTeam.sh

On Windows, run `MyFirstTeam.cmd` instead. If you run the source file directly, install the dependencies first and use the virtual-environment interpreter:

    ../deps/venv/bin/python SpinBot.py

(assuming you are standing in the `SpinBot` bot directory; on Windows, use `..\deps\venv\Scripts\python.exe SpinBot.py`)

## Slow boot up the first time

When you run a sample bot for Python for the first time, its dependency installer creates the virtual environment and installs the dependencies. This can take some additional time before it is ready to join the battle, so it might take a while before the bot becomes available in the GUI.
