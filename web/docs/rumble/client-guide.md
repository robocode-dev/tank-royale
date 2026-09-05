# Run ranked Rumble battles

The Rumble has no central machine running every fight. Community members donate computer time by running the Rumble client locally. The client downloads the reviewed bot catalog, chooses a useful matchup, runs the battle, keeps replay evidence on your computer, and submits the result for validation.

You do not need to own a bot to contribute battles. If you do own one, you can ask the client to prefer under-sampled matchups involving it.

## What works today

The native client can synchronize, run one ranked battle, and submit its result. You currently build it from source because there is no published production client image. The development Docker image contains all four bot runtimes, but its launcher scripts currently expose only configuration validation, runtime checks, and synchronization. Use the native path below for ranked `run` and `submit` commands.

The client accepts a practice-mode configuration, but `--run` currently executes ranked battles only. Use the [Tank Royale GUI](../articles/gui-battle-setup.md) for local practice battles.

## What you need

- A GitHub account.
- Git and JDK 17.
- Java 17, .NET 8 SDK, Python 3.12, and Node.js 22 if you run natively. The client may select bots from any supported language.
- Two sibling source checkouts: [`tank-royale`](https://github.com/robocode-dev/tank-royale) and [`rumble-client`](https://github.com/robocode-dev/rumble-client).

The runtime check reports exactly what is missing and does not install or change anything.

## 1. Register your client

Each battle contributor registers once so result submissions can be tied to a GitHub account. Fork [`robocode-dev/rumble-data`](https://github.com/robocode-dev/rumble-data), then add `clients/<your-github-account>.json`:

```json
{
  "schemaVersion": 1,
  "account": "your-github-account",
  "clientIds": ["your-github-account-desktop-01"]
}
```

The filename and `account` must match your GitHub account exactly. Choose a stable `clientId` for each computer you plan to use. Open a PR with this file; a moderator reviews and merges it. Ranked submissions from that account and client ID are accepted after the registration reaches `main`.

## 2. Build the client

Clone the two repositories beside each other:

```text
work/
├── tank-royale/
└── rumble-client/
```

From `rumble-client`, build against the adjacent Tank Royale checkout.

On Linux or macOS:

```shell
./gradlew --no-configuration-cache -PtankRoyaleSource=../tank-royale build
```

On PowerShell:

```powershell
.\gradlew.bat --no-configuration-cache "-PtankRoyaleSource=../tank-royale" build
```

## 3. Create your configuration

Copy `rumble-client.example.json` to `rumble-client.json`, then edit the copy. Do not commit it.

```json
{
  "schemaVersion": 1,
  "botsRepo": "https://github.com/robocode-dev/rumble-bots",
  "dataRepo": "https://github.com/robocode-dev/rumble-data",
  "clientId": "your-github-account-desktop-01",
  "myBots": ["MyBot"],
  "gameTypes": ["1v1"],
  "battlesPerSession": 50,
  "mode": "ranked",
  "workDirectory": ".rumble-client"
}
```

Use the registered ID from step 1. `myBots` may be empty; otherwise, list the names of your active bots or teams without version numbers. The client prioritizes useful matchups involving them.

Set `gameTypes` to the single format you want to run. During the current source-build phase, list exactly one game type: `--run` picks the alphabetically first entry and runs that one, so listing several formats silently runs only `1v1` and never the others. To cover another format, change `gameTypes` and run again. Each `--run` invocation runs one battle; the example file's `battlesPerSession` is not used by the current commands.

`workDirectory` holds the bot cache, ranked journal, and replay evidence. Keep that directory private and backed up.

## 4. Check and synchronize

Every Gradle invocation needs the same two arguments as the build in step 2. `-PtankRoyaleSource` is what makes the client compile and run against your adjacent Tank Royale checkout; without it Gradle tries to download a Battle Runner release that does not exist yet, and the command fails to resolve its dependencies. `--no-configuration-cache` is required because the project enables the configuration cache by default.

On Linux or macOS:

```shell
./gradlew --no-configuration-cache -PtankRoyaleSource=../tank-royale run --args="--check-runtimes"
./gradlew --no-configuration-cache -PtankRoyaleSource=../tank-royale run --args="--validate-config"
./gradlew --no-configuration-cache -PtankRoyaleSource=../tank-royale run --args="--sync"
```

On PowerShell:

```powershell
.\gradlew.bat --no-configuration-cache "-PtankRoyaleSource=../tank-royale" run --args="--check-runtimes"
.\gradlew.bat --no-configuration-cache "-PtankRoyaleSource=../tank-royale" run --args="--validate-config"
.\gradlew.bat --no-configuration-cache "-PtankRoyaleSource=../tank-royale" run --args="--sync"
```

Synchronization verifies your registration, the current engine behavior version, the catalog source hashes, and the matchmaking advice. It prepares an immutable local cache of the exact bot sources used for ranked battles. If synchronization refuses to continue, follow its diagnostic instead of bypassing the check.

## 5. Run a ranked battle

On Linux or macOS:

```shell
./gradlew --no-configuration-cache -PtankRoyaleSource=../tank-royale run --args="--run"
```

On PowerShell:

```powershell
.\gradlew.bat --no-configuration-cache "-PtankRoyaleSource=../tank-royale" run --args="--run"
```

The command chooses one valid matchup, runs all rounds for that game type, and appends the completed result to the local journal. An aborted, incomplete, or incompatible battle is not submittable. Replay evidence stays under `.rumble-client/evidence`; it is never uploaded automatically.

## 6. Submit completed results

Create a fine-grained GitHub personal access token limited to the `robocode-dev/rumble-data` repository with read and write access to Issues. It must not have permission to change repository contents, branches, releases, packages, or Pages.

Supply the token only to the submission process. On Linux or macOS:

```shell
export RUMBLE_CLIENT_TOKEN='<your-token>'
./gradlew --no-configuration-cache -PtankRoyaleSource=../tank-royale run --args="--submit"
unset RUMBLE_CLIENT_TOKEN
```

On PowerShell:

```powershell
$env:RUMBLE_CLIENT_TOKEN = '<your-token>'
.\gradlew.bat --no-configuration-cache "-PtankRoyaleSource=../tank-royale" run --args="--submit"
Remove-Item Env:RUMBLE_CLIENT_TOKEN
```

The client posts pending journal records through the `rumble-data` issue inbox. The ingestion workflow validates each result, publishes accepted facts, and replies with receipts. Records remain retryable until the client observes their successful receipts, so an interrupted submission does not lose them.

Never put the token in `rumble-client.json`, a shell script, Git, an issue, or a log.

## Keep contributing

Repeat `--run` to produce more battles and `--submit` to send pending results. You can change `gameTypes` between sessions. The client uses published matchmaking advice to cover new and under-sampled matchups; that advice is guidance rather than a reservation, so two clients may safely run the same matchup.

Accepted results usually reach the [dashboard](https://robocode-dev.github.io/rumble-data/) within minutes. A scheduled ingestion sweep runs twice an hour if the immediate GitHub event is delayed.

For command and implementation details, see the [`rumble-client` README](https://github.com/robocode-dev/rumble-client#tank-royale-rumble-client).
