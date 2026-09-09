# Python Bot API for Robocode Tank Royale

Note: This README is for developers—not PyPi. The README for PyPi is [here](README-PyPI.md)

This directory contains the Bot API for developing bots for Robocode Tank Royale with Python.

The Bot API is provided via a pip package.

## Build Commands (Local Build)

### From the current directory:

Create and activate the project virtual environment first. The setup scripts install the dependencies from `requirements.txt` into `.venv`.

On macOS/Linux:

```shell
bash scripts/create-venv.sh
. .venv/bin/activate
```

On Windows PowerShell:

```powershell
.\scripts\create-venv.ps1
.\.venv\Scripts\Activate.ps1
```

Then generate schemas and install the local package in editable mode:

```shell
python scripts/update_version.py
python scripts/schema_to_python.py -d ../../schema/schemas -o generated/robocode_tank_royale/schema
python -m pip install -e .
```

On macOS/Linux, `bash prepare.sh` can be used instead when type-stub generation is also needed; it performs the version update, schema generation, and stub generation using the active environment.

### From the root folder using Gradle:

#### Clean:

```shell
../../gradlew :bot-api:python:clean --info
```

#### Build:

```shell
../../gradlew :bot-api:python:build --info
```

The artifacts are located in `/bot-api/python/dist`.

#### Test:

```shell
../../gradlew :bot-api:python:test --info
```

## Usage

```py
import robocode_tank_royale.bot_api
```
