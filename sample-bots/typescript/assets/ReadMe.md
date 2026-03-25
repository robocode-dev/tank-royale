# Sample bots for Robocode Tank Royale for TypeScript

This directory contains sample bots for Robocode Tank Royale developed for the TypeScript programming language
running on Node.js.

## Requirements for running the sample bots

1. Node.js 18 (or newer) must be installed on your system. You can download it from here:
   https://nodejs.org/en/download/

2. You need to unpack the archive or copy the directories in the root of your `bots` directory to run these with
   Robocode.

## Bot directories

Each bot has its own subdirectory (bot directory) that contains:
* A TypeScript source file (.ts) that provides the program logic of the bot.
* A JSON file (.json) that provides information about the bot.
* Script files (.cmd and .sh) used for starting the bot.

## Running a bot

A script file is used for running the bot, which runs `node <bot source file>` for the sample bots for TypeScript.

You can run a sample bot manually from the command line by going into the bot directory (using the `cd` command) and
writing:

    node <TypeScript source file>

Note: The bots are pre-compiled to JavaScript. If you want to modify and recompile them, you need to have TypeScript
installed (`npm install -g typescript`) and run `tsc` in the bot directory.
