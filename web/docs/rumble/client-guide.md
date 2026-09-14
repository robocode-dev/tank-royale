# Run ranked Rumble battles

The Rumble has no central machine running every fight. Community members donate computer time by running the Rumble client locally. The client downloads the reviewed bot catalog, chooses a useful matchup, runs the battle, keeps replay evidence on your computer, and submits the result for validation.

You do not need to own a bot to contribute battles. If you do own one, you can ask the client to prefer under-sampled matchups involving it.

## What works today

The client can synchronize, run one ranked battle, and submit its result through the published `ghcr.io/robocode-dev/rumble-client` image or through a native source build. The published image is the default container path: it contains all four bot runtimes and its launcher scripts cover configuration validation, runtime checks, synchronization, ranked battles, and result submission. The native path below and the container path in [Container setup](#container-setup) reach the same client behavior.

The client also accepts a practice-mode configuration: `--sync` and `--run` work without a registered client identity, while `--submit` rejects practice mode because practice results are never journaled or submitted. The client still reads the configured HTTPS bot catalog; it does not accept an arbitrary local bot folder. Use the [Tank Royale GUI](../articles/gui-battle-setup.md) to test an unsubmitted local bot.

## What you need

- A GitHub account.
- Git and a [`rumble-client`](https://github.com/robocode-dev/rumble-client) checkout for the launcher scripts and example configuration.
- Docker Engine/Desktop or Podman for the published image. On Windows, Podman Desktop uses a Linux virtual machine with either WSL2 or Hyper-V as the provider.
- JDK 17, Java 25, .NET 10 SDK, Python 3.14, and Node.js 24 only if you choose the native path. Native builds also need a sibling [`tank-royale`](https://github.com/robocode-dev/tank-royale) checkout.

The published image needs no host language runtimes or Tank Royale checkout. Its runtime check reports exactly what is missing and does not install or change anything.

### Native Python setup

The Docker image contains its own Python environment. For native execution, create a virtual environment and install the Python Bot API before running the client. Keep the environment active, or set the explicit interpreter variable, whenever the client starts Python bots.

On Linux or macOS:

```shell
cd rumble-client
python3.14 -m venv .rumble-python
. .rumble-python/bin/activate
python -m pip install --upgrade pip
python -m pip install "robocode-tank-royale==1.2.0"
export RUMBLE_PYTHON="$(command -v python)"
```

On Windows PowerShell:

```powershell
cd rumble-client
py -3.14 -m venv .rumble-python
.\.rumble-python\Scripts\Activate.ps1
python -m pip install --upgrade pip
python -m pip install "robocode-tank-royale==1.2.0"
$env:RUMBLE_PYTHON = (Get-Command python).Source
```

Use the same shell for the runtime check, synchronization, and ranked run. `RUMBLE_PYTHON` is honored by the catalog's Python launchers and makes the selected venv explicit; putting the venv first on `PATH` also ensures generic `python3` launchers use it. The runtime check verifies the interpreter version, so installing the API into this environment is still required.

### Container setup

The published image uses the current LTS-first Java 25, .NET 10, Python 3.14, and Node.js 24 lanes, with the Tank Royale API installed in an image-owned Python virtual environment. Pull `ghcr.io/robocode-dev/rumble-client:latest` before first use if you want to make the image download explicit; the launchers use it by default and pull it when it is not present. The container does not use your host Python environment, so the native Python setup above is unnecessary when all client commands run in the container. Exact image pins are maintained in the [Rumble client repository](https://github.com/robocode-dev/rumble-client/blob/main/src/main/resources/runtime-versions.properties) and refreshed by reviewed monthly pull requests. Podman Desktop/WSL2 on Windows and rootless Podman on Linux have both been manually verified for this image; neither is part of CI. The launcher scripts fully qualify the image's base images and add `--userns=keep-id` for the Podman engine so the bind-mounted `.rumble-client` state directory stays writable under rootless Podman. Rootless Podman also needs the `cpu`, `memory`, and `pids` cgroup controllers delegated to your user session for the launchers' resource limits to apply; recent systemd delegates all three by default on most current Linux distributions, so this is normally a non-issue, but a cgroup- or resource-limit-related failure instead of an application error is the sign to check delegation.

For normal use, pull the published image:

```shell
docker pull ghcr.io/robocode-dev/rumble-client:latest
```

To build a local development image instead, from the `rumble-client` checkout read the pinned Tank Royale commit and pass it to the Docker build:

```shell
TANK_ROYALE_COMMIT="$(tr -d '[:space:]' < TANK_ROYALE_COMMIT)"
docker build --tag rumble-client:dev --build-arg "TANK_ROYALE_COMMIT=${TANK_ROYALE_COMMIT}" .
podman build --tag rumble-client:dev --build-arg "TANK_ROYALE_COMMIT=${TANK_ROYALE_COMMIT}" .
```

On Windows PowerShell, use:

```powershell
$env:TANK_ROYALE_COMMIT = (Get-Content TANK_ROYALE_COMMIT -Raw).Trim()
docker build --tag rumble-client:dev --build-arg "TANK_ROYALE_COMMIT=$env:TANK_ROYALE_COMMIT" .
```

With Podman, replace `docker` with `podman` in the corresponding build command.

On Windows, create and start a Podman machine once if needed. Choose one provider when initializing it:

```powershell
# WSL2
podman machine init --provider wsl

# Or Hyper-V
podman machine init --provider hyperv

podman machine start
```

Run the supported container phases with the launcher scripts. Docker is the default and uses the published image; select Podman with `CONTAINER_ENGINE=podman` on Unix-like shells or `-Engine podman` in PowerShell:

```shell
./docker/rumble.sh runtimes
CONTAINER_ENGINE=podman ./docker/rumble.sh runtimes
./docker/rumble.sh validate rumble-client.json
CONTAINER_ENGINE=podman ./docker/rumble.sh sync rumble-client.json
./docker/rumble.sh run rumble-client.json
export RUMBLE_CLIENT_TOKEN='<your token>'
./docker/rumble.sh submit rumble-client.json
```

```powershell
.\docker\rumble.ps1 runtimes
.\docker\rumble.ps1 runtimes -Engine podman
.\docker\rumble.ps1 validate rumble-client.json
.\docker\rumble.ps1 sync rumble-client.json
.\docker\rumble.ps1 run rumble-client.json
$env:RUMBLE_CLIENT_TOKEN = '<your token>'
.\docker\rumble.ps1 submit rumble-client.json
```

The launcher mounts the configuration read-only and keeps `.rumble-client` writable because synchronization creates the immutable bot cache and the client stores journals and replay evidence there. `runtimes` runs with no network; `validate`, `sync`, `run`, and `submit` need network access, since `run` re-synchronizes before executing a battle and `submit` reaches the GitHub Issues API. This covers the same ranked and practice-mode commands as the native path in steps 4 through 6 below; use whichever path fits your setup.

To use a locally built `rumble-client:dev` image, pass it as the third argument after the configuration path, for example `./docker/rumble.sh runtimes rumble-client.json rumble-client:dev` or `.\docker\rumble.ps1 runtimes rumble-client.json rumble-client:dev`.

The normal launcher workflow does not run an arbitrary local bot folder. When a specialized containerized Battle Runner harness boots bot archives directly, mount a bot root directory rather than a zip file. Java and Python archives can remain read-only. C# and TypeScript first-run dependency setup may write files and change permissions, so copy those archives into writable container storage before booting them; this avoids `EPERM` errors on Windows bind mounts. If Podman Desktop reports `ssh-keygen` is missing, enable Windows OpenSSH and add `C:\Windows\System32\OpenSSH` to the user `PATH`, then restart the terminal and Podman Desktop.

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

## 2. Build the native client (optional)

The published-image path does not need a Tank Royale checkout or a native toolchain. Skip this step when using the published image; build the client only when you choose native execution or are changing the client itself.

For the native path, clone the two repositories beside each other:

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

Set `gameTypes` to the single format you want to run. During the current source-build phase, list exactly one game type: `--run` picks the alphabetically first entry and runs that one, so listing several formats silently runs only that entry and never the others. To cover another format, change `gameTypes` and run again. Each `--run` invocation runs one battle; the example file's `battlesPerSession` is not used by the current commands.

`workDirectory` holds the bot cache, ranked journal, and replay evidence. Keep that directory private and backed up.

## 4. Check and synchronize

For the native path, every Gradle invocation needs the same two arguments as the build in step 2. `-PtankRoyaleSource` is what makes the client compile and run against your adjacent Tank Royale checkout; without it Gradle tries to download a Battle Runner release that does not exist yet, and the command fails to resolve its dependencies. `--no-configuration-cache` is required because the project enables the configuration cache by default.

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

Accepted results usually reach the [dashboard](https://robocode-dev.github.io/rumble-data/) within minutes. A scheduled ingestion sweep runs twice an hour if the immediate GitHub event is delayed. The [ranking guide](rankings.md) explains which accepted battles are eligible for current APS and why repeated samples improve a matchup without giving it more weight.

For command and implementation details, see the [`rumble-client` README](https://github.com/robocode-dev/rumble-client#tank-royale-rumble-client).
