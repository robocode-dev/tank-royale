# Tank Royale Rumble

Writing a bot is one thing. Finding out how good it really is takes a lot of battles against a lot of opponents.

Tank Royale Rumble is the community-run ranked competition for Robocode Tank Royale. It follows the tradition of RoboRumble and LiteRumble from classic Robocode: submit your bot, let it fight across the shared ladder, study the results, and come back with a better version. If you are serious about Robocoding, this is where you put your code to the test.

You do not steer a tank during a Rumble battle. You write the program that controls it. Rumble clients run the battles automatically and publish the results, so rankings come from the same bot code fighting many different opponents.

## The Rumble loop

1. Build a bot with one of the official Tank Royale Bot APIs.
2. Practice locally in the Tank Royale GUI until your bot behaves the way you want.
3. Submit its source code to the reviewed Rumble bot catalog.
4. Community members run Rumble clients on their own computers. The clients choose useful matchups, run ranked battles, and submit the results.
5. The dashboard combines the accepted results into rankings.
6. Learn from the results, improve your bot, and submit a new version.

The cycle never really ends. A bot that dominates today may meet a smarter opponent tomorrow.

## Choose how you want to participate

| I want to... | Start here |
|--------------|------------|
| Enter my bot in the rankings | [Submit a bot](bot-author-guide.md) |
| Donate computer time and run ranked battles | [Run a Rumble client](client-guide.md) |
| See which bots are winning | [Open the live dashboard](https://robocode-dev.github.io/rumble-data/) |
| Understand APS and ranking history | [Read how rankings work](rankings.md) |
| Help review submissions and keep the competition fair | [Moderate the Rumble](moderator-guide.md) |

You can submit a bot without running a client, and you can run a client without owning a bot. Many competitors do both because the client gives matchups involving their own bots priority when more samples are needed.

## Ranked game types

Each game type has its own leaderboard.

| Game type | What fights | Rounds | Battlefield |
|-----------|-------------|--------|-------------|
| **1v1** | Two individual bots | 35 | 800 x 600 |
| **TwinDuel** | Two teams with two bots on each team | 75 | 800 x 800 |
| **Melee** | Ten individual bots at once | 35 | 1000 x 1000 |

TwinDuel is the classic twin-team format, not a general team category. Mini, micro, nano, and other code-size classes from classic Robocode are not part of the Tank Royale Rumble because source-size limits do not compare cleanly across Java, C#, Python, and TypeScript.

## How the system works

The Rumble has three public GitHub repositories:

- [`rumble-bots`](https://github.com/robocode-dev/rumble-bots) is the reviewed, source-only bot catalog.
- [`rumble-client`](https://github.com/robocode-dev/rumble-client) runs battles on contributors' computers.
- [`rumble-data`](https://github.com/robocode-dev/rumble-data) accepts results, calculates the rankings, and publishes the dashboard.

There is no central battle server. Clients download the same pinned bot catalog and game settings, run battles locally, and send completed results through GitHub issues. Automation validates each result before it becomes part of the ranking data. The raw accepted results remain in Git, so the leaderboard can be rebuilt and checked by anyone.

### Catalog

The catalog is the list of bots and TwinDuel teams allowed in ranked battles. Every entry has an owner, a version, and a hash of its reviewed source. A new source version is a new ranked identity; only the latest active version appears on the current leaderboard.

### Matchups and samples

A matchup is a set of opponents that fought each other. One completed battle is one sample of that matchup. The client prefers new and under-sampled matchups, which helps new bots receive a meaningful rank and keeps older rankings fresh.

### Rankings and APS

The main ranking number is APS, or Average Percentage Score. Rumble first averages repeated battles within each exact participant matchup, then gives every distinct matchup equal weight in the final average. [Read the ranking guide](rankings.md) for the precise formula, an uneven-sample example, and the eligibility rules.

A new active bot version starts at APS 0 with no battle samples. Its superseded version and every matchup containing that old identity stop affecting the current ranking. When a game-observable engine change starts a new behavior version, the current leaderboard likewise uses only results from the new epoch. Immutable month-end snapshots preserve earlier published rankings.

## When rankings update

An incoming result submission starts the ingestion workflow as soon as GitHub applies its `result-submission` label. A scheduled sweep also runs at 17 and 47 minutes past every UTC hour in case an event was delayed. Accepted results regenerate the leaderboard, and changed ranking data explicitly requests a dashboard deployment. An hourly Pages reconciliation repairs a missed deployment without republishing an unchanged site.

The bot catalog is checked at 23 minutes past every UTC hour. An unchanged catalog stops without aggregation or a catalog-driven deployment; a month-end snapshot can still make that run publish history. A newly merged bot or version normally reaches the dashboard after a changed check, then waits for clients to produce its first ranked battles. The dashboard timestamp reports when ranking data changed, not when automation last ran.

## Current availability

Bot submission, ranked battle execution, result ingestion, and the dashboard work end to end. The Rumble client publishes a ready-to-run image at `ghcr.io/robocode-dev/rumble-client`; battle contributors can pull it and use `docker/rumble.sh` or the PowerShell equivalent for configuration validation, runtime checks, synchronization, ranked battles, and result submission with Docker or Podman, including rootless Podman on Linux. Native command-line execution and local image builds remain available as well. The [client guide](client-guide.md) shows the exact supported paths.
