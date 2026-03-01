# Battle Runner Examples

Runnable Java examples demonstrating the Battle Runner API. These use Java 11+ source-file execution — no
compilation step required.

## Prerequisites

1. **Build the runner JAR** (from the repository root):

   ```sh
   ./gradlew :runner:copyRunnerJar
   ```

   This copies the runner fat JAR to `runner/examples/lib/`.

2. **Build the sample bots** (or prepare your own bots):

   ```sh
   ./gradlew :sample-bots:java:build
   ```

3. **Set `BOTS_DIR`** to the directory containing your bot folders:

   **bash/zsh:**
   ```sh
   export BOTS_DIR=../../sample-bots/java/build/archive
   ```

   **PowerShell:**
   ```powershell
   $env:BOTS_DIR = "../../sample-bots/java/build/archive"
   ```

   Each subdirectory under `BOTS_DIR` should contain a bot (with a matching `.json` config file).

## Running the Examples

From the `runner/examples/` directory:

### RunBattle — Synchronous battle with results

```sh
java -cp lib/* RunBattle.java
```

Runs a 5-round Classic battle between Walls and SpinBot, then prints a results table with rankings and scores.

### AsyncBattle — Asynchronous battle with event streaming

```sh
java -cp lib/* AsyncBattle.java
```

Starts a 3-round battle asynchronously and streams round start/end events in real time.

### RecordBattle — Battle recording

```sh
java -cp lib/* RecordBattle.java
```

Runs a 3-round battle and writes a `.battle.gz` replay file to a `recordings/` directory.

### IntentDiagnosticsBattle — Per-turn bot intent capture

```sh
java -cp lib/* IntentDiagnosticsBattle.java
```

Runs a 1-round battle with intent diagnostics enabled, then prints a turn-by-turn
table of each bot's movement speed, turn rates, and firing decisions.

### ControlBattle — Pause, step, and resume

```sh
java -cp lib/* ControlBattle.java
```

Runs an asynchronous battle, pauses at turn 5, steps through 3 turns manually,
then resumes. Demonstrates `pause()`, `nextTurn()`, and `resume()` on a `BattleHandle`.

## Customizing

Each example reads bot paths from the `BOTS_DIR` environment variable. To use different bots, either:
- Edit the example source to reference different bot names
- Point `BOTS_DIR` to a different directory containing your bots

The examples use the `BattleSetup.classic()` preset by default. See the
[Battle Runner API documentation](https://robocode-dev.github.io/tank-royale/api/battle-runner) for all
configuration options.
