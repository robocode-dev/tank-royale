# Sample bots for Robocode Tank Royale for TypeScript

This directory contains sample bots for Robocode Tank Royale developed for the TypeScript programming language
running on Node.js.

## Requirements for running the sample bots

Node.js 18 (or newer) must be installed on your system. You can download it from here:
https://nodejs.org/en/download/

## Bot directories

Each bot has its own subdirectory (bot directory) that contains:
* A TypeScript source file (.ts) that provides the program logic of the bot.
* A JSON file (.json) that provides information about the bot.
* Script files (.cmd and .sh) used for starting the bot.

A shared `deps/` folder at the same level as the bot directories contains the bot API and runtime
dependencies. The first time a bot is started, the script will automatically install these
dependencies using `npm install`. Subsequent launches skip the install step.

## Running a bot

Use the provided script file to run a bot. From a terminal, go into the bot directory and run:

- **Windows:** `MyFirstBot.cmd`
- **macOS/Linux:** `./MyFirstBot.sh`

The script installs dependencies on first run, then starts the bot directly from its TypeScript
source using `tsx`.

You can also start the bot manually once dependencies are installed:

    ../node_modules/.bin/tsx MyFirstBot.ts
