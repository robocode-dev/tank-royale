# Submit a bot to the Rumble

This guide takes a bot from a runnable starter or an existing Tank Royale strategy into the [Rumble](index.md) ranked catalog. You need a GitHub account, Git, Python 3.11 or newer for validation, and the runtimes used by the bots in your catalog checkout. On Windows, make Git for Windows' `sh` available on `PATH`; validation uses the Unix launcher even when started from PowerShell. CI explicitly installs Python, Java 25, .NET SDK 10 and Node.js 24.

If you have not written a bot yet, start with the [Tank Royale tutorial](../tutorial/getting-started.md) and the list of [official Bot APIs](../api/apis.md). Test the bot against sample bots with the normal [GUI battle setup](../articles/gui-battle-setup.md) before submitting it. Local GUI battles are private practice; they never affect the Rumble rankings.

## Before you submit

Ranked bots must meet these rules:

- Use the official Tank Royale Bot API for Java, C#, Python, or TypeScript.
- Run directly from source through the normal Tank Royale booter scripts.
- Depend only on the official Bot API, the runtime dependencies declared by that API, and the platform's standard library. TypeScript's official API requires its declared `ws` runtime peer; the starter includes it and a committed npm lockfile. Other bot libraries remain disallowed.
- Include no compiled programs, archives, generated dependency directories, custom protocol clients, process launchers, or raw socket code.
- Declare one of the permitted licenses: `MIT`, `Apache-2.0`, `BSD-3-Clause`, or `GPL-3.0-or-later`.

The complete rules live in the [`rumble-bots` contribution guide](https://github.com/robocode-dev/rumble-bots/blob/main/CONTRIBUTING.md).

## 1. Fork and clone the catalog

A fork is your own GitHub copy of a repository. A pull request, usually shortened to PR, submits your changes for GitHub moderation. A clean bot-only submission can merge automatically; cases needing judgment go to Flemming.

Fork [`robocode-dev/rumble-bots`](https://github.com/robocode-dev/rumble-bots), then clone your fork and create a branch:

```shell
git clone https://github.com/<your-github-account>/rumble-bots.git
cd rumble-bots
git switch -c add-<bot-name>
```

Replace the angle-bracket placeholders with your GitHub account and bot name.

## 2. Add your bot source

Choose a [runnable starter](https://github.com/robocode-dev/rumble-bots/tree/main/templates) for your language, or adapt your existing bot to the same layout:

| Language | Starter directory | Runtime and dependency setup |
|---|---|---|
| Java | [`templates/java/StarterBot`](https://github.com/robocode-dev/rumble-bots/tree/main/templates/java/StarterBot) | Java 25 and curl; the launcher obtains the pinned official JAR outside the bot directory. |
| C# | [`templates/csharp/StarterBot`](https://github.com/robocode-dev/rumble-bots/tree/main/templates/csharp/StarterBot) | .NET SDK 10; `dotnet run` restores the official package and keeps generated output outside the bot directory. |
| Python | [`templates/python/StarterBot`](https://github.com/robocode-dev/rumble-bots/tree/main/templates/python/StarterBot) | Install the pinned official package from `requirements.txt` in an environment outside the bot directory. |
| TypeScript | [`templates/typescript/StarterBot`](https://github.com/robocode-dev/rumble-bots/tree/main/templates/typescript/StarterBot) | Node.js 24 and npm; the launcher uses native TypeScript support and `npm ci` in a temporary run directory. |

The starters pin Bot API 1.3.1 for the current Rumble client. They turn the gun and fire at scanned opponents; replace that strategy with your own after testing it in the GUI. Their dependency files and lockfile are source inputs, while downloaded packages and generated output stay outside the submitted tree.

For example, copy the Java starter to `MyBot` using a Unix shell:

```shell
mkdir -p bots/java
cp -R templates/java/StarterBot bots/java/MyBot
```

Or use PowerShell:

```powershell
New-Item -ItemType Directory -Force bots/java | Out-Null
Copy-Item -Recurse templates/java/StarterBot bots/java/MyBot
```

Substitute `csharp`, `python` or `typescript` when choosing another language. In the copy, replace every `StarterBot` identifier in file contents with `MyBot`, including the class name, launcher entry-point paths and smoke marker. Rename `StarterBot.json`, `StarterBot.sh`, `StarterBot.cmd` and `src/StarterBot.<extension>` to the new name. For C#, also rename `StarterBot.csproj`. Keep `src/` unchanged, and retain the dependency manifests; TypeScript needs both `package.json` and `package-lock.json`.

Your individual bot has this layout:

```text
bots/
└── <platform>/
    └── <BotName>/
        ├── <BotName>.json
        ├── <BotName>.sh
        ├── <BotName>.cmd
        └── src/
            └── source files
```

Use `java`, `csharp`, `python`, or `typescript` for `<platform>`. The directory name, config filename, and bot `name` must match exactly, including case. Source files belong under `src/`, and the boot scripts point at the entry point there, for example `"$SCRIPT_DIR/src/MyBot.py"`. Existing entries in [`bots/`](https://github.com/robocode-dev/rumble-bots/tree/main/bots) are useful working examples. Keep `node_modules`, virtual environments, `bin`, `obj`, JARs and DLLs out of the submission.

Add a `license` field to the bot's JSON configuration, for example:

```json
{
  "name": "MyBot",
  "version": "1.0.0",
  "authors": ["Your name"],
  "description": "What my bot does.",
  "platform": "JVM",
  "programmingLang": "Java 25",
  "gameTypes": ["1v1", "melee", "twinduel"],
  "license": "Apache-2.0"
}
```

This example uses Java; keep the correct platform and language values from your working bot configuration. The license applies to the complete bot directory. By opening the PR, you certify that you have the right to publish the source under that license.

## 3. Validate the submission

For Python, install the declared API in a virtual environment outside the bot directory. On a Unix shell:

```shell
python -m venv .venv
.venv/bin/python -m pip install -r bots/python/MyBot/requirements.txt
.venv/bin/python scripts/validate_bot.py --root . --owner <your-github-account> --smoke
```

On PowerShell:

```powershell
python -m venv .venv
.venv/Scripts/python.exe -m pip install -r bots/python/MyBot/requirements.txt
.venv/Scripts/python.exe scripts/validate_bot.py --root . --owner <your-github-account> --smoke
```

For other languages, run the same validator used by CI with your Python installation:

```shell
python scripts/validate_bot.py --root . --owner <your-github-account> --smoke
```

The validator checks the directory layout, license, source-only rules, ownership, and boot scripts. Its TypeScript checks cover dependency declarations and the npm lockfile; moderators review dependency use across all languages. It sets `RUMBLE_SMOKE=1`, invokes each individual bot's `.sh` launcher, and requires a zero exit with `RUMBLE_SMOKE_READY <BotName>` in stdout within 20 seconds. Preserve that branch when replacing the starter strategy. The starter smoke branch loads the API and exits before connecting to a server; a passing smoke check proves source startup, not a completed battle.

Initial dependency restoration needs network access. If a cold download exceeds the smoke timeout, run the launcher once with `RUMBLE_SMOKE=1` to warm its external cache, then retry the validator. Run validation from the repository root; it checks existing entries too, so their runtimes must be available. For Python, use the environment containing the pinned API as shown above; the validator passes that interpreter to the launcher through `RUMBLE_PYTHON`.

On Linux and macOS, make your launcher executable before local validation:

```shell
chmod +x bots/java/MyBot/MyBot.sh
```

On every platform, including Windows, stage and record the executable bit in Git:

```shell
git add bots/java/MyBot
git update-index --chmod=+x bots/java/MyBot/MyBot.sh
git ls-files --stage bots/java/MyBot/MyBot.sh
```

Substitute your actual platform and name. The last command must show mode `100755`; this is the executable bit that the Linux CI checkout receives. Fix every reported validation problem before opening the PR.

## 4. Open the pull request

Commit and push your branch, then open a PR against `robocode-dev/rumble-bots`:

```shell
git add bots/<platform>/<BotName>
git commit -m "Add <BotName>"
git push --set-upstream origin add-<bot-name>
```

Use the repository's bot-submission checklist in the PR description. GitHub checks the exact PR commit, screens source and launchers, and runs your bot through one complete round against a reference bot in an isolated container. If all checks pass and the PR changes only one bot directory, the moderator App approves and merges it automatically. A bot that fails to run receives a changes-requested review with a diagnosis; push a fix to rerun the check. Security uncertainty, TwinDuel team metadata and changes outside one bot directory go to Flemming with the specific reason.

When the PR is merged, CI adds the bot to the generated catalog. `rumble-data` checks that catalog at 23 minutes past every UTC hour. A changed catalog is regenerated into the [dashboard](https://robocode-dev.github.io/rumble-data/), where the new active version starts at APS 0 and waits for its first eligible battles. [Ranking and version rules](rankings.md) explain exactly when results count.

## Submit a TwinDuel team

A TwinDuel team is its own catalog entry. Start from the [JSON-only team template](https://github.com/robocode-dev/rumble-bots/blob/main/templates/team/StarterTeam/StarterTeam.json). Its `teamMembers` field contains exactly two member slots, each identifying an active bot by its full `name version` identity, including an explicit version:

```json
{
  "name": "MyTwinTeam",
  "version": "1.0.0",
  "authors": ["Your name"],
  "license": "Apache-2.0",
  "teamMembers": ["MyFirstBot 1.0.0", "MySecondBot 1.0.0"]
}
```

Put the team under one of the recognized platform folders. Its directory contains only `<TeamName>.json`; it has no source or boot scripts of its own. Both slots may name the same bot for a true twin team. The slots may also name bots written with different Bot API languages, in which case the catalog publishes the team platform as `Mixed`. A team cannot contain another team, and every member must be an active individual bot.

Member versions are part of the published team identity. To use a different member version, update its `teamMembers` entry explicitly. Bump the team's own `version` whenever you change a member bot or its version.

## Ownership, versions, and slots

The first merged PR for a bot or team name reserves that name for your GitHub account. Only that account, or another account registered to the same owner, may submit later versions.

Published source versions are immutable. When the source changes, increase the version in the bot configuration and submit it again. The latest version becomes active; the previous identity and matchups containing it leave the current ranking, while immutable month-end snapshots remain available. See [What happens when a bot gets a new version](rankings.md#what-happens-when-a-bot-gets-a-new-version).

Each owner may have five active catalog entries by default. An individual bot or a TwinDuel team each uses one slot. Updating an existing entry to a new version does not consume another slot.

## Help your bot get ranked

New entries need battle samples before their ranking means much. Community clients prefer matchups with too few samples, so coverage improves automatically as contributors run battles. You can also [run a Rumble client](client-guide.md) and list your own entries in `myBots`; the client then gives useful matchups involving them priority.
