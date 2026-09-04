# Submit a bot to the Rumble

This guide takes a bot that already works in Tank Royale and puts it into the [Rumble](index.md) ranked catalog. You need a GitHub account, Git, Python 3, and the runtimes used by the bots in your catalog checkout. CI has all four supported runtimes.

If you have not written a bot yet, start with the [Tank Royale tutorial](../tutorial/getting-started.md) and the list of [official Bot APIs](../api/apis.md). Test the bot against sample bots with the normal [GUI battle setup](../articles/gui-battle-setup.md) before submitting it. Local GUI battles are private practice; they never affect the Rumble rankings.

## Before you submit

Ranked bots must meet these rules:

- Use the official Tank Royale Bot API for Java, C#, Python, or TypeScript.
- Run directly from source through the normal Tank Royale booter scripts.
- Depend only on the official Bot API package and the platform's standard library. TypeScript bots must also commit their lockfile.
- Include no compiled programs, archives, generated dependency directories, custom protocol clients, process launchers, or raw socket code.
- Declare one of the permitted licenses: `MIT`, `Apache-2.0`, `BSD-3-Clause`, or `GPL-3.0-or-later`.

The complete rules live in the [`rumble-bots` contribution guide](https://github.com/robocode-dev/rumble-bots/blob/main/CONTRIBUTING.md).

## 1. Fork and clone the catalog

A fork is your own GitHub copy of a repository. A pull request, usually shortened to PR, asks the Rumble moderators to merge your changes into the public catalog.

Fork [`robocode-dev/rumble-bots`](https://github.com/robocode-dev/rumble-bots), then clone your fork and create a branch:

```shell
git clone https://github.com/<your-github-account>/rumble-bots.git
cd rumble-bots
git switch -c add-<bot-name>
```

Replace the angle-bracket placeholders with your GitHub account and bot name.

## 2. Add your bot source

Copy your working bot into the folder for its language:

```text
bots/
└── <platform>/
    └── <BotName>/
        ├── <BotName>.json
        ├── <BotName>.sh
        ├── <BotName>.cmd
        └── source files
```

Use `java`, `csharp`, `python`, or `typescript` for `<platform>`. The directory name, config filename, and bot `name` must match exactly. Existing entries in [`bots/`](https://github.com/robocode-dev/rumble-bots/tree/main/bots) are useful working examples.

Add a `license` field to the bot's JSON configuration, for example:

```json
{
  "name": "MyBot",
  "version": "1.0.0",
  "authors": ["Your name"],
  "description": "What my bot does.",
  "platform": "JVM",
  "programmingLang": "Java 17",
  "gameTypes": ["1v1", "melee", "twinduel"],
  "license": "Apache-2.0"
}
```

This example uses Java; keep the correct platform and language values from your working bot configuration. The license applies to the complete bot directory. By opening the PR, you certify that you have the right to publish the source under that license.

## 3. Validate the submission

Run the same validator used by CI:

```shell
python scripts/validate_bot.py --root . --owner <your-github-account> --smoke
```

The validator checks the directory layout, license, dependencies, source-only rules, ownership, and boot scripts. The smoke check also starts the source entry point. Fix every reported problem before opening the PR.

## 4. Open the pull request

Commit and push your branch, then open a PR against `robocode-dev/rumble-bots`:

```shell
git add bots/<platform>/<BotName>
git commit -m "Add <BotName>"
git push --set-upstream origin add-<bot-name>
```

Use the repository's bot-submission checklist in the PR description. CI runs the validator again, and a moderator reviews the submission. A green check is required, but it does not replace review.

When the PR is merged, CI adds the bot to the generated catalog. `rumble-data` synchronizes that catalog at 23 minutes past every UTC hour. The bot then appears on the [dashboard](https://robocode-dev.github.io/rumble-data/) and waits for its first ranked battles.

## Submit a TwinDuel team

A TwinDuel team is its own catalog entry. Its `teamMembers` field contains exactly two member slots backed by active bot versions:

```json
{
  "name": "MyTwinTeam",
  "version": "1.0.0",
  "authors": ["Your name"],
  "license": "Apache-2.0",
  "teamMembers": ["MyFirstBot 1.0.0", "MySecondBot 1.0.0"]
}
```

Put the team under one of the recognized platform folders. Its directory contains only `<TeamName>.json`; it has no source or boot scripts of its own. Both slots may name the same bot version for a true twin team. The slots may also name bots written with different Bot API languages, in which case the catalog publishes the team platform as `Mixed`. A team cannot contain another team, and every member must be an active individual bot.

Member versions are part of the team identity. If either member gets a new version, submit a new team version that names the updated members.

## Ownership, versions, and slots

The first merged PR for a bot or team name reserves that name for your GitHub account. Only that account, or another account registered to the same owner, may submit later versions.

Published source versions are immutable. When the source changes, increase the version in the bot configuration and submit it again. The latest version becomes active; older results remain in history but no longer determine the current rank.

Each owner may have five active catalog entries by default. An individual bot or a TwinDuel team each uses one slot. Updating an existing entry to a new version does not consume another slot.

## Help your bot get ranked

New entries need battle samples before their ranking means much. Community clients prefer matchups with too few samples, so coverage improves automatically as contributors run battles. You can also [run a Rumble client](client-guide.md) and list your own entries in `myBots`; the client then gives useful matchups involving them priority.
