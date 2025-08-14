# Bot API for Wasm

This directory contains the Bot API for developing bots for Robocode Tank Royale using Wasm (WebAssembly).

This project is build with Kotlin/Wasm and uses Node.js as runtime for testing.

## Build commands

#### Clean build files

```shell
./gradlew bot-api:wasm:clean
```

#### Build/compile

```shell
./gradlew bot-api:wasm:build
```

#### Runs the compiled Wasm with Node.js

```shell
./gradlew bot-api:wasm:run
```

#### Run tests

```shell
./gradlew bot-api:wasm:test
```
