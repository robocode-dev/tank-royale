# Battle contributor quickstart

The Rumble client runs local ranked battles against the published [Rumble](index.md) catalog and submits every completed result automatically. This guide covers the [`rumble-client`](https://github.com/robocode-dev/rumble-client) repository as it stands today: there is no published container image or native release yet, so running it means building it from source.

## 1. Register once

Before a client can submit ranked results, its forge account must be registered. Open a pull request against [`rumble-data`](https://github.com/robocode-dev/rumble-data) adding `clients/<your-forge-account>.json`, whose `account` field matches the filename and whose `clientIds` lists the stable client identifier(s) you'll use. A moderator reviews this once; after it's merged, issues from your account and client ID are accepted.

## 2. Build the client

You need JDK 17 and a local Tank Royale checkout containing Runner support (BR-049) alongside your `rumble-client` checkout, then:

```shell
./gradlew --no-configuration-cache -PtankRoyaleSource=../tank-royale build
```

On PowerShell, quote the property: `.\gradlew.bat --no-configuration-cache "-PtankRoyaleSource=../tank-royale" build`.

Alternatively, build the development Docker image: `docker build --tag rumble-client:dev .`, then drive it with `docker/rumble.sh` (or `docker/rumble.ps1`). The Docker launchers expose only your configuration file and state directory to the container, with a read-only root filesystem, dropped capabilities, resource limits, and no network for the runtime check — this is the isolation boundary for running bot code you didn't write, including your ranked opponents' bots.

Run `./gradlew run --args="--check-runtimes"` to confirm you have Java 17, .NET 8 SDK, Python 3.12, and Node.js 22 available; it never installs anything for you.

## 3. Configure

Copy `rumble-client.example.json` to `rumble-client.json`. Ranked mode requires your registered `clientId`; practice mode may omit it. The optional `workDirectory` sets where the local cache, journal, and replay evidence live (default `.rumble-client` next to the config file). Never commit `rumble-client.json` or any token.

## 4. Sync, run, submit

```shell
./gradlew run --args="--validate-config"   # check local settings
./gradlew run --args="--sync"              # resolve the catalog, engine pin, and matchmaking advice
./gradlew run --args="--run"               # run one pinned ranked battle, keep local replay evidence
./gradlew run --args="--submit"            # post pending results to the rumble-data issue inbox
```

`--sync` validates the engine pin and your client registration and prepares an immutable, hash-verified bot cache at the catalog's exact source commit. `--run` selects a battle using a recorded random seed, prioritizing under-sampled pairings involving your own bots. `--submit` reads a `RUMBLE_CLIENT_TOKEN` environment variable at runtime — use a GitHub fine-grained personal access token scoped to read/write Issues on `rumble-data` only, nothing else. The client tracks posted batches locally and only drops them once their receipt comment appears on the closed issue; an identical retry is acknowledged idempotently rather than double-submitted.

## Practice vs. ranked

Practice runs never create a ranked record or submission — use them freely while developing. Ranked runs require your registered `clientId` and always attempt submission.

## What happens after you submit

Your batch becomes a GitHub issue on `rumble-data` labelled `result-submission`. An automated workflow validates each result independently, commits accepted ones as immutable JSON facts, regenerates the leaderboard and pairings, comments a receipt on the issue, and closes it. The full contract — envelope shape, per-participant fields, and rejection reasons — is in [`rumble-data`'s `CONTRIBUTING.md`](https://github.com/robocode-dev/rumble-data/blob/main/CONTRIBUTING.md). Back up your `workDirectory`'s replay evidence yourself; `rumble-data` never stores replays.

## Next steps

Back your battle contributions with your own bots — see [Submit a bot](bot-author-guide.md) — and watch results land on the [dashboard](https://robocode-dev.github.io/rumble-data/).
