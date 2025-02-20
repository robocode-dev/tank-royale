# Python Bot API for Robocode Tank Royale

This directory contains the Bot API for developing bots for Robocode Tank Royale with Python.

The Bot API is provided via a pip package.

## Build Commands (Local Build)

### From the current directory:

```shell
pip install -e .
```

### From the root folder using Gradle:

#### Clean:

```shell
./gradlew :bot-api:python:clean
```

#### Build:

```shell
./gradlew :bot-api:python:build
```

## Usage

```py
import tank_royale.bot_api
```
