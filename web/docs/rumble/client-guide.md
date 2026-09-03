# Battle contributor quickstart

The Rumble client runs local ranked battles against the published [Rumble](index.md) catalog and submits every completed result automatically. Full setup and usage instructions live in the [`rumble-client` README](https://github.com/robocode-dev/rumble-client#tank-royale-rumble-client) — that's the single source of truth for running the client, kept next to its own Dockerfile and launcher scripts so it never drifts from what's actually there. This page covers the one step that happens in a different repository, then hands you off.

## 1. Register once

Before a client can submit ranked results, its forge account must be registered. Open a pull request against [`rumble-data`](https://github.com/robocode-dev/rumble-data) adding `clients/<your-forge-account>.json`, whose `account` field matches the filename and whose `clientIds` lists the stable client identifier(s) you'll use. A moderator reviews this once; after it's merged, issues from your account and client ID are accepted.

## 2. Build, configure, and run

Everything else — building the Docker image, configuring `rumble-client.json`, and running `validate` / `runtimes` / `sync` / `run` / `submit` — is in the [`rumble-client` README](https://github.com/robocode-dev/rumble-client#tank-royale-rumble-client). Follow its Quickstart in order; it's the same one whether you got here from this page or found the repository directly.

## Next steps

Back your battle contributions with your own bots — see [Submit a bot](bot-author-guide.md) — and watch results land on the [dashboard](https://robocode-dev.github.io/rumble-data/).
