# Tank Royale Rumble

Rumble is the community ranked ladder for Tank Royale. Bots live in a public, reviewed catalog, contributors run local clients that fight ranked battles and submit results automatically, and a static dashboard tracks rankings from those results.

Rumble runs entirely on GitHub across three repositories, with no central server and no secrets to operate: [`rumble-bots`](https://github.com/robocode-dev/rumble-bots) (the bot catalog), [`rumble-data`](https://github.com/robocode-dev/rumble-data) (results and rankings), and [`rumble-client`](https://github.com/robocode-dev/rumble-client) (the battle runner contributors use).

## Get started

- **[Submit a bot](bot-author-guide.md)** — write a bot with an official Bot API, get it reviewed, and enter it into the ranked pool.
- **[Run battles](client-guide.md)** — pull the client, register once, and fight ranked battles that submit themselves.
- **[Moderate](moderator-guide.md)** — review submissions and keep the ladder healthy.

## Rankings

The live leaderboard, pairings, and every ranked result are published from [`rumble-data`](https://github.com/robocode-dev/rumble-data): see the [dashboard](https://robocode-dev.github.io/rumble-data/).

## Current status

Rumble supports the **1v1**, **TwinDuel** (twin-team), and **Melee** ranked formats. The client is still pre-release: there is no published container image or native distribution yet, so running battles today means building the client from source. See [Run battles](client-guide.md) for the exact steps.
