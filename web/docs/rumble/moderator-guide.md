# Moderate the Rumble

Rumble moderators protect a fair, reviewable competition. They review bot submissions and client registrations, handle disputes, and keep the GitHub automation healthy. There is no hidden admin panel: moderation happens through pull requests and the policy files in `rumble-bots` and `rumble-data`.

The repository policies remain authoritative. This guide explains the normal workflow and points to those policies when judgment is required.

## Review a bot submission

Bot submissions arrive as pull requests in [`rumble-bots`](https://github.com/robocode-dev/rumble-bots).

1. Wait for validation CI to pass. Never merge a submission with a failing check.
2. Confirm that the pull request changes only the author's bot or team entry and does not edit generated catalog files.
3. Check that the directory contains source rather than binaries, generated dependencies, archives, or unrelated files.
4. Confirm that the bot uses an official Bot API and declares an allowed SPDX license.
5. Review restricted-code diagnostics and suspicious or confusable names. Ask the author to resolve anything unclear before approval.
6. For an existing bot name, confirm that the submitting GitHub account owns it and that the version increased when source changed.
7. For a TwinDuel team, confirm that it has two member slots backed by active individual bot versions and contains only its team JSON. Repeated member identities are valid within one twin team.

First-time authors deserve a closer look. Owners with a history of clean submissions may qualify for auto-merge on later version bumps, as described in [`rumble-bots/GOVERNANCE.md`](https://github.com/robocode-dev/rumble-bots/blob/main/GOVERNANCE.md).

## Review a client registration

Client registrations arrive as pull requests in [`rumble-data`](https://github.com/robocode-dev/rumble-data).

1. Confirm that the PR adds `clients/<account>.json` and does not change result facts or generated projections.
2. Check that the filename and `account` exactly match the submitting GitHub account.
3. Check that `clientIds` is a non-empty list of stable, non-empty identifiers.
4. Require the Verify Rumble data workflow to pass.
5. Merge after one moderator approval.

Ordinary code, catalog, ban, exclusion, and policy changes in `rumble-data` follow the same green-CI and one-approval rule. CI is the only writer of accepted facts and generated projections on `main`.

## Handle disputes and abuse

Every action below is a reviewed pull request with the reason recorded in its description. Preserve the facts; change whether they count.

### Exclude a disputed result

Add its `battleId` to `rumble-data/exclusions.json`. The next aggregation omits it from rankings without deleting the accepted fact. Removing the exclusion restores it on a later recomputation.

### Ban an account

Add the GitHub account to `rumble-data/bans.json`. Future submissions are rejected, and earlier facts from that account stop contributing when projections are regenerated.

If the account owns catalog entries, update `rumble-bots/bots/banned.json` as well. The generated catalog excludes disqualified bots, but their existing result facts remain in history. Bans may be temporary or permanent.

### Resolve a suspicious name or submission

The validator detects identical normalized name skeletons and restricted code constructs. Treat the diagnostic as a reason to inspect the submission, not as proof of bad intent. Resolve impersonation, name squatting, unsafe code, licensing complaints, account recovery, and appeals under [`rumble-bots/GOVERNANCE.md`](https://github.com/robocode-dev/rumble-bots/blob/main/GOVERNANCE.md).

## Keep the automation healthy

Result submissions normally trigger ingestion when GitHub applies the `result-submission` label. A scheduled fallback runs at 17 and 47 minutes past every UTC hour. Catalog synchronization runs at 23 minutes past every UTC hour. The Pages workflow publishes dashboard changes after accepted data is pushed.

If GitHub disables scheduled workflows after repository inactivity, re-enable them. A newly labelled result issue also wakes the ingestion workflow, but moderators should not rely on incoming traffic as the only health check.

Once a month, follow the `rumble-data` compaction procedure for facts older than three full months. It must produce verified rollups without changing the generated projections before the individual facts move to the archive branch.

Once each quarter, run the fork drill: fork the repositories, enable their workflows and Pages, run validation and aggregation locally, and record the result in a governance issue. The drill proves that another maintainer can recover the Rumble from its public repositories and documentation.

Operational details and decision authority live in [`rumble-data/GOVERNANCE.md`](https://github.com/robocode-dev/rumble-data/blob/main/GOVERNANCE.md) and [`rumble-bots/GOVERNANCE.md`](https://github.com/robocode-dev/rumble-bots/blob/main/GOVERNANCE.md).
