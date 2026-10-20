# Run ranked Rumble battles

Contributors run the Rumble client on their own computer to play ranked battles with reviewed bots. The client records each battle locally and prepares a result for review; you post that result through GitHub.

You do not need to own a bot. The client can select useful matchups from the published catalog, or prioritize your own active bots if you list them in the configuration.

## Requirements and limits

The recommended path uses Docker Desktop or Docker Engine on a Linux host, a GitHub account, and Git. You do not need a GitHub token or host language runtimes. The published image is Linux/amd64; ARM64 and Apple Silicon execution are unverified. This walkthrough was exercised on Windows 11 with Docker Desktop using its Linux/amd64 engine. Linux Docker Engine, macOS Docker Desktop, and Podman were not exercised for this walkthrough.

Each container command is read-only except for its mounted state directory. The launcher limits a container to 4 CPUs, 8 GiB memory, 512 processes, and a 1 GiB temporary filesystem; it drops Linux capabilities and blocks privilege escalation. `runtimes` has no network access. `sync`, `run`, and `submit` need outbound HTTPS access to GitHub; `validate` checks the local configuration.

The native path is optional. It uses JDK 17 to build, plus Java 25, .NET 10, Python 3.14, and Node.js 24 to run catalog bots. Native execution is CI-tested on Linux; Windows and macOS native execution are unverified. The container is the verified path on a Windows host.

## 1. Clone the client

Open a terminal where you want to keep the client checkout:

```shell
git clone https://github.com/robocode-dev/rumble-client.git
cd rumble-client
```

```powershell
git clone https://github.com/robocode-dev/rumble-client.git
Set-Location rumble-client
```

## 2. Register your client once

Fork [`robocode-dev/rumble-data`](https://github.com/robocode-dev/rumble-data), clone your fork beside the client checkout, and create `clients/<your-github-account>.json`. The filename and `account` must exactly match the GitHub account you will use to post results. Choose a stable client ID for this computer; a moderator must review and merge the registration before ranked synchronization will accept it. These commands assume your shell is currently in the `rumble-client` checkout.

On Linux or macOS, set your GitHub account first:

```shell
account=your-github-account
client_id="${account}-desktop-01"
cd ..
git clone "https://github.com/${account}/rumble-data.git"
cd rumble-data
git switch -c register-rumble-client
mkdir -p clients
cat > "clients/${account}.json" <<EOF
{
  "schemaVersion": 1,
  "account": "${account}",
  "clientIds": ["${client_id}"]
}
EOF
git add "clients/${account}.json"
git commit -m "Register Rumble client"
git push -u origin register-rumble-client
cd ../rumble-client
```

On Windows PowerShell, replace the account and client ID first:

```powershell
$account = 'your-github-account'
$clientId = "$account-desktop-01"
Set-Location ..
git clone "https://github.com/$account/rumble-data.git"
Set-Location rumble-data
git switch -c register-rumble-client
New-Item -ItemType Directory -Force clients | Out-Null
@"
{
  "schemaVersion": 1,
  "account": "$account",
  "clientIds": ["$clientId"]
}
"@ | Set-Content -Encoding utf8NoBOM "clients/$account.json"
git add "clients/$account.json"
git commit -m "Register Rumble client"
git push -u origin register-rumble-client
Set-Location ..\rumble-client
```

On GitHub, open your fork's `register-rumble-client` branch and choose **Contribute → Open pull request** to target `robocode-dev/rumble-data`'s `main` branch. Wait for a moderator to merge it, then return to your `rumble-client` checkout. The registration file gives the server permission to accept results for the listed client ID; it does not grant repository write access.

## 3. Create a one-battle configuration

Copy the example configuration and set `clientId` to the exact registered ID. Keep `myBots` empty to let the client select any eligible catalog bots. Select exactly one game type; each `run` command plays one battle, and `battlesPerSession` is currently required configuration but does not control the number of battles.

On Linux or macOS:

```shell
cp rumble-client.example.json rumble-client.json
```

On Windows PowerShell:

```powershell
Copy-Item rumble-client.example.json rumble-client.json
```

Edit `rumble-client.json` to match this example, replacing the `clientId` value:

```json
{
  "schemaVersion": 1,
  "botsRepo": "https://github.com/robocode-dev/rumble-bots",
  "dataRepo": "https://github.com/robocode-dev/rumble-data",
  "clientId": "your-github-account-desktop-01",
  "myBots": [],
  "gameTypes": ["1v1"],
  "battlesPerSession": 1,
  "mode": "ranked",
  "workDirectory": ".rumble-client"
}
```

Keep `rumble-client.json` and the `.rumble-client` directory private and in the client checkout. The state directory contains the synchronized bot cache, pending result journal, submission files, and replay evidence. Back it up and keep it between runs until all results have receipts; never commit it.

## 4. Pull the image and check your setup

Pull the current published image. The launchers also use this image by default:

```shell
docker pull ghcr.io/robocode-dev/rumble-client:latest
./docker/rumble.sh runtimes
./docker/rumble.sh validate rumble-client.json
```

```powershell
docker pull ghcr.io/robocode-dev/rumble-client:latest
.\docker\rumble.ps1 runtimes
.\docker\rumble.ps1 validate rumble-client.json
```

The runtime check confirms the bundled Java, .NET, Python, and Node.js versions without installing anything on your computer. Validation checks the configuration only; it does not access GitHub or prove that registration has merged.

## 5. Synchronize and run one battle

After registration has merged, synchronize the current engine pin, your registration, active bot catalog, and matchup advice:

```shell
./docker/rumble.sh sync rumble-client.json
./docker/rumble.sh run rumble-client.json
```

```powershell
.\docker\rumble.ps1 sync rumble-client.json
.\docker\rumble.ps1 run rumble-client.json
```

`run` synchronizes again before selecting and executing the battle. It records the result in the local journal and saves replay evidence below `.rumble-client/evidence`. It does not submit anything by itself. Do not bypass an engine, catalog, or registration error; resolve the cause and synchronize again.

## 6. Post the result and collect its receipt

Run `submit` to prepare a result batch. The client prints a GitHub result-form address and writes the exact text to paste under `.rumble-client/submissions`:

```shell
./docker/rumble.sh submit rumble-client.json
```

```powershell
.\docker\rumble.ps1 submit rumble-client.json
```

Open the printed address in a browser signed in to the same GitHub account named in your registration. Paste the complete prepared file into the form, review it, and create the issue. The client needs no token; the browser account and registered client ID identify the contributor. Never put a credential in the configuration or submission file.

After the issue is created, run `submit` again. The client checks the issue and canonical result data, then records accepted receipts. Keep the state directory and prepared file if the issue is still processing or a result is rejected; a retry can collect later receipts without losing the journaled battle.

## 7. Check the ranking and contribute again

Accepted eligible battles normally appear on the [Rumble dashboard](https://robocode-dev.github.io/rumble-data/). Result ingestion is swept at 17 and 47 minutes past each UTC hour if the immediate GitHub event is delayed. Catalog changes are checked at 23 minutes past each UTC hour, so a newly merged bot may not be selectable immediately. See the [ranking guide](rankings.md) for eligibility and ranking behavior.

To contribute again, repeat `run` and then `submit`. Each `run` executes exactly one battle. You can run several battles before submitting them, but keep the client state directory and stop if any command reports an error. Change `gameTypes` to a different single game type before running a different format.

## Troubleshooting

| Problem | What to do |
|---|---|
| Docker says it cannot connect to the daemon | Start Docker Desktop or the Docker service, confirm `docker info` succeeds, then rerun the command. |
| Sync says the client ID is unknown | Check that the ID exactly matches `clientIds` in the merged registration file. Wait for the registration PR to merge, then run `sync` again. |
| A newly registered bot is not in the catalog | Keep the state directory, wait for the next catalog check, and run `sync` again. The bot catalog is checked at 23 minutes past each UTC hour. |
| No valid matchup or an invalid bot selection | Leave `myBots` empty, use exactly one `gameTypes` entry, and make sure the selected format has enough active catalog entries. Names in `myBots` must match active bots or teams; local bot folders are not accepted. |
| Engine or behavior version has changed | Pull the latest image and run `sync` again. Use the client version and engine pin selected by the current catalog; do not submit results from a stale engine. |
| No receipt appears after posting | Confirm the issue was created from the prepared file by the registered account. Keep the journal and state directory, wait for processing, then run `submit` again. |
| A receipt exists but the dashboard has not changed | Receipt acceptance and dashboard publication are separate. Wait for the result sweep, then check the dashboard and ranking guide. |
| GitHub asks you to authenticate | Sign in to GitHub in your browser using the registered account. The client itself does not require a token. |

## Optional native path

The native client uses the published Tank Royale Battle Runner `1.3.1` dependency; normal native commands do not need a sibling Tank Royale checkout or a `tankRoyaleSource` Gradle property. Install JDK 17 for the Gradle build and install Java 25, .NET 10, Python 3.14, and Node.js 24 for the bots you intend to run. For Python bots, install the matching API into a virtual environment and point the client at that interpreter:

```shell
python3.14 -m venv .rumble-python
. .rumble-python/bin/activate
python -m pip install "robocode-tank-royale==1.3.1"
export RUMBLE_PYTHON="$(command -v python)"
```

```powershell
py -3.14 -m venv .rumble-python
.\.rumble-python\Scripts\Activate.ps1
python -m pip install "robocode-tank-royale==1.3.1"
$env:RUMBLE_PYTHON = (Get-Command python).Source
```

Build and check the client from its checkout:

```shell
./gradlew --no-configuration-cache build
./gradlew --no-configuration-cache run --args="--check-runtimes"
./gradlew --no-configuration-cache run --args="--validate-config rumble-client.json"
./gradlew --no-configuration-cache run --args="--sync rumble-client.json"
./gradlew --no-configuration-cache run --args="--run rumble-client.json"
./gradlew --no-configuration-cache run --args="--submit rumble-client.json"
```

```powershell
.\gradlew.bat --no-configuration-cache build
.\gradlew.bat --no-configuration-cache run --args="--check-runtimes"
.\gradlew.bat --no-configuration-cache run --args="--validate-config rumble-client.json"
.\gradlew.bat --no-configuration-cache run --args="--sync rumble-client.json"
.\gradlew.bat --no-configuration-cache run --args="--run rumble-client.json"
.\gradlew.bat --no-configuration-cache run --args="--submit rumble-client.json"
```

The native client runs one battle per `run` command and keeps the same journal and evidence in `.rumble-client`. The container path remains recommended because its multi-language runtime environment is pinned and isolated.
