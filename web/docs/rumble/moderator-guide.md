# Moderate the Rumble

The Tank Royale Rumble moderators protect a fair, reviewable competition. They are a team in the `robocode-dev` GitHub organization. GitHub automatically assesses routine bot submissions; human moderators handle uncertain submissions, client registrations, disputes and automation health. There is no hidden admin panel: moderation happens through pull requests and the policy files in `rumble-bots` and `rumble-data`.

This guide states the participation rule for distinct ranked entries and explains the normal moderation workflow. The repository policies govern moderation commands, permissions and dispute procedures.

## Volunteer as a moderator

Open a [moderator volunteer request](https://github.com/robocode-dev/rumble-bots/issues/new?template=moderator-volunteer.yml) in `rumble-bots`. The request is public. Describe your experience with Robocode or Tank Royale, the languages you can review, your experience reviewing GitHub pull requests, and the time you can offer. Do not post credentials or private information.

Moderators must be able to read bot source and check logs, understand the Rumble's bot and result rules, and explain decisions with evidence. Familiarity with at least one official Bot API language is sufficient if you ask another reviewer for help with unfamiliar code. You must keep untrusted bot code away from privileged credentials, leave required checks and branch rules in place, and handle bans, exclusions and appeals through reviewed pull requests. Disclose conflicts of interest and let another moderator decide cases involving your own bots or account.

Existing moderators review the request and may ask you to assess an example submission before access is granted. A team maintainer or organization owner adds accepted volunteers to the moderator team after the team has the repository permissions needed for the role. Team membership can be removed when a volunteer steps down or no longer follows these rules.

## Review a bot submission

Bot submissions arrive as pull requests in [`rumble-bots`](https://github.com/robocode-dev/rumble-bots). A PR that changes exactly one bot directory, passes source and security screening, and completes a full round against a trusted reference is approved and merged by the moderator App. No human action is needed for that result.

When the `bot-moderation` check stays pending, GitHub comments with the reason and calls for the moderator team. Review that case as follows:

1. Read the stated uncertainty and the assessment log. Never merge a submission with a failing check.
2. Confirm that the pull request changes only the author's bot or team entry and does not edit generated catalog files.
3. Check that the directory contains source rather than binaries, generated dependencies, archives, or unrelated files.
4. Confirm that the bot uses an official Bot API and declares an allowed SPDX license.
5. Review restricted-code diagnostics and suspicious or confusable names. Ask the author to resolve anything unclear before approval.
6. For an existing bot name, confirm that the submitting GitHub account owns it and that the version increased when source changed.
7. For a TwinDuel team, confirm that it has two member slots backed by active individual bot versions and contains only its team JSON. Repeated member identities are valid within one twin team.

If a pending case is acceptable, comment `/moderation approve <reason>` on the current PR commit. This records the human decision and turns the required check green. An eligible moderator must then submit an approving GitHub PR review; changes to protected paths also need an approval from a code owner. Ask another moderator to decide and approve a PR you authored or one involving your own bot or account, since GitHub does not count an author's approval of their own PR. Merge manually only after the required status and reviews pass. A new commit requires a new assessment and review. If the bot does not run, GitHub requests changes from its author; do not override that failure. Team metadata and non-bot changes always take the human path. The exact permissions and recovery steps live in [`rumble-bots/GOVERNANCE.md`](https://github.com/robocode-dev/rumble-bots/blob/main/GOVERNANCE.md).

## Assess a reported near-duplicate

Bots derived from existing catalog entries are welcome when they make a substantial independent contribution to battle strategy. You may reject a derivative that retains substantially the same strategy without such a contribution. This includes copies disguised by extensive rewriting, code reorganization or translation to another language; rejection is not limited to cosmetic changes. A new name, different colors, renamed variables, formatting changes or small parameter adjustments do not qualify when the underlying strategy remains substantially unchanged. License permission to reuse code does not guarantee eligibility for a separate entry.

Compare the underlying implementation and strategic decisions, using source and battle behavior as evidence, and ask the author to explain their contribution. Look beyond textual similarity: changed syntax, structure or language can preserve the same targeting, movement and decision logic. Record evidence that the submission is derived from the compared bot and why its changes do or do not make a substantial strategic contribution. Shared helper code or a common codebase can be legitimate when the strategy has been substantially developed; a contribution need not improve the bot's score to qualify. Similar behavior alone is insufficient grounds for rejection: independently written bots can use the same strategy. Do not use a fixed similarity percentage or treat overlap as proof of bad intent. Repeated member identities within one TwinDuel team remain valid.

Record the compared bot and version, the relevant source similarities, the claimed strategic changes and the reason for the decision in the submission PR or dispute. Let the author respond and request reconsideration by a moderator without a conflict of interest; defer cases involving your own bot or account to another moderator.

This is a human eligibility rule. Passing automated checks does not settle whether a bot is a near-duplicate, and those checks do not automatically flag every such case. For a pending submission, use the existing moderation process and request changes or decline the entry with the recorded reason. If the bot has already been accepted automatically, handle the report through the repository's dispute procedure; any resulting disqualification uses a reviewed PR under [Handle disputes and abuse](#handle-disputes-and-abuse). A near-duplicate report alone is not grounds for an account ban.

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

Add the GitHub account to `bannedAccounts` in `rumble-data/bans.json`. Future submissions are rejected, and earlier facts from that account stop contributing when projections are regenerated.

If the account owns catalog entries, add those entries to `disqualifiedBots` in the same file. The generated catalog excludes disqualified bots, but their existing result facts remain in history. Bans may be temporary or permanent.

### Resolve a suspicious name or submission

The validator detects identical normalized name skeletons and restricted code constructs. Treat the diagnostic as a reason to inspect the submission, not as proof of bad intent. Resolve impersonation, name squatting, unsafe code, licensing complaints, account recovery, and appeals under [`rumble-bots/GOVERNANCE.md`](https://github.com/robocode-dev/rumble-bots/blob/main/GOVERNANCE.md).

## Keep the automation healthy

The organization-owned `Tank Royale Rumble Moderator` App supplies the review and merge identity. Its exact registration fields, repository permissions, installation scope, Actions secrets, branch rule and recovery steps are in the [`rumble-bots` moderator setup](https://github.com/robocode-dev/rumble-bots/blob/main/GOVERNANCE.md#moderator-app-registration).

Result submissions normally trigger ingestion when GitHub applies the `result-submission` label. A scheduled fallback runs at 17 and 47 minutes past every UTC hour. Catalog checks run at 23 minutes past every UTC hour and skip aggregation when unchanged. Changed writers explicitly request Pages, while a 41-minute-past hourly reconciliation deploys only when the site differs from the latest successful deployment. See [Rumble rankings and APS](rankings.md) for the public freshness, active-version, and immutable snapshot contract.

If GitHub disables scheduled workflows after repository inactivity, re-enable them. A newly labelled result issue also wakes the ingestion workflow, but moderators should not rely on incoming traffic as the only health check.

Once a month, follow the `rumble-data` compaction procedure for facts older than three full months. It must produce verified rollups without changing the generated projections before the individual facts move to the archive branch.

Once each quarter, run the fork drill: fork the repositories, enable their workflows and Pages, run validation and aggregation locally, and record the result in a governance issue. The drill proves that another maintainer can recover the Rumble from its public repositories and documentation.

Operational details and decision authority live in [`rumble-data/GOVERNANCE.md`](https://github.com/robocode-dev/rumble-data/blob/main/GOVERNANCE.md) and [`rumble-bots/GOVERNANCE.md`](https://github.com/robocode-dev/rumble-bots/blob/main/GOVERNANCE.md).
