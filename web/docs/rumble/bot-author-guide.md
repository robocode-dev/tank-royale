# Bot author quickstart

This is the fastest path from nothing to a ranked-eligible bot in the [Rumble](index.md) catalog.

## Requirements

- Your bot must be built on an **official Tank Royale Bot API** (Java, C#, Python, or TypeScript). Custom frameworks or hand-rolled protocol implementations are not eligible for ranked Rumble — see [the APIs](../api/apis.md) if you haven't built a bot yet, and the [tutorial](../tutorial/getting-started.md) for a walkthrough.
- Bots run **directly from source**, exactly like the [sample bots](../articles/installing-sample-bots.md) — nothing is precompiled or uploaded as a binary.
- Your bot's platform-specific dependencies are limited to the official Bot API package plus the standard library (for TypeScript: the official npm package with a committed lockfile).

## 1. Write your bot

Develop and test it locally against the sample bots first, using Robocode's normal [GUI battle setup](../articles/gui-battle-setup.md) — this is "practice mode": nothing you run locally is ever submitted anywhere.

## 2. Lay it out for submission

`rumble-bots` uses the same booter directory convention as any Tank Royale bot: a directory holding `<BotName>.json` (booter config), `<BotName>.sh` and `<BotName>.cmd` (boot scripts), and your source. Copy [the bot submission template](https://github.com/robocode-dev/rumble-bots/blob/main/.github/PULL_REQUEST_TEMPLATE/bot-submission.md) into `bots/<platform>/<BotName>/` in your fork of [`rumble-bots`](https://github.com/robocode-dev/rumble-bots).

## 3. Declare a license

Every bot needs an explicit license — add a `license` field with one of these SPDX identifiers to your bot's config JSON: `MIT`, `Apache-2.0`, `BSD-3-Clause`, or `GPL-3.0-or-later`. Submitting a PR certifies you have the right to publish the code under that license.

## 4. Validate locally

Run the validator before opening a PR, so you catch problems before CI does:

```shell
python scripts/validate_bot.py --root . --owner <your-forge-account> --smoke
```

It checks your directory structure, that only source files are present, dependency allowlist compliance, and that the bot boots and connects the way the booter will run it.

## 5. Open the pull request

Open a PR against `rumble-bots`. Validation CI re-runs the same checks; a moderator then reviews the PR (first-time authors get a stricter look). Once merged, your bot is added to the generated `bots/index.json` catalog and enters ranked matchmaking.

## Ownership and versioning, briefly

- The first merged PR for a bot name **reserves that name for you**, identified by your forge account. Only your registered accounts can submit new versions of your own bots.
- Published versions are immutable — changing your source means bumping the version. Only the latest version of a bot stays in the ranked pool; older versions stay in history.
- Each owner has a limited number of active bot slots (5 at launch). Version bumps are free; a new bot name consumes a slot.

Full submission, ownership, licensing, and moderation rules live in [`rumble-bots`'s `CONTRIBUTING.md`](https://github.com/robocode-dev/rumble-bots/blob/main/CONTRIBUTING.md).

## Next steps

Once your bot is merged, watch it climb the [dashboard](https://robocode-dev.github.io/rumble-data/) as ranked battles run. Running a [client](client-guide.md) yourself gives your own bots priority in matchmaking.
