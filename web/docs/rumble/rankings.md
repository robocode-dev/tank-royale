# Rumble rankings and APS

The [live Rumble dashboard](https://robocode-dev.github.io/rumble-data/) ranks active bots and TwinDuel teams using APS, or Average Percentage Score. APS compares how much of the available score an entry earns across its distinct matchups. Higher is better.

## How APS is calculated

For each accepted battle, Rumble calculates every participant's score share:

```text
score share = participant totalScore / sum of every participant's totalScore
```

If the total score is zero, every participant receives a zero share for that battle. Rumble then groups battles by the exact sorted set of participating `name version` identities. That set is one distinct matchup, whether it has one sample or many.

For each bot or team, Rumble:

1. Averages its score shares across repeated battles of the same matchup.
2. Averages those per-matchup values across all of its distinct matchups.
3. Multiplies the result by 100.

Each distinct matchup therefore has equal weight. Running one matchup more often improves the estimate for that matchup without making it dominate the overall ranking.

### Example with uneven samples

Suppose Alpha has these results:

| Matchup | Samples | Mean score share |
|---------|---------|------------------|
| Alpha 1.0 vs. Bravo 1.0 | 9 | 90% |
| Alpha 1.0 vs. Charlie 1.0 | 1 | 30% |

Alpha's APS is `(90 + 30) / 2 = 60`. It is not 84, which would incorrectly give the nine-sample matchup nine times the weight.

APS is stored to four decimal places and displayed with two. Ties are ordered by stable bot identity. The dashboard also shows the number of accepted battles and distinct matchups behind each value; a high APS based on little data should be treated as preliminary.

## Which battles count

A battle contributes to the current leaderboard only when all of these conditions hold:

- The result was accepted from a registered, unbanned contributor and has not been excluded by moderation.
- No participant is currently disqualified.
- The result uses the current `behaviorVersion`, which identifies the game-observable rules used for ranked play.
- Every participant's exact `name version` identity is currently active and eligible for that game type.

The complete participant set matters. When one participant version becomes inactive, that old matchup stops affecting the live APS of every participant in it.

For `1v1`, a matchup contains two active individual bots. For TwinDuel, it contains two eligible team entries whose members are active and do not overlap. For Melee, the complete set of active individual participants identifies the matchup.

## What happens when a bot gets a new version

A version is part of a bot's ranked identity. A new version becomes active only after its reviewed pull request is merged into [`rumble-bots`](https://github.com/robocode-dev/rumble-bots) and the catalog change reaches `rumble-data`.

The new active version appears on the current leaderboard with APS 0, zero battles, and zero matchups until clients submit eligible battles containing it. The superseded version leaves the current table, and matchups containing that version no longer affect current rankings. Its accepted facts remain auditable, and month-end snapshots created while it was active remain unchanged.

## Current rankings and monthly snapshots

The current leaderboard is cumulative: it uses all eligible accepted facts in the current behavior epoch, not just battles from the current month. It does not reset on the first day of a month.

Before the first result or catalog update proceeds in a new UTC month, `rumble-data` copies the previous current leaderboards and bot details into an immutable month-end snapshot. If no writer ran across several month boundaries, each missing month records the same last published cumulative state. Snapshots are read-only; later results, moderation, and bot versions never rewrite them.

Use the dashboard's **Ranking period** selector to switch between **Current** and an available month. Historical months begin with the first boundary after snapshot support was deployed; earlier months are not reconstructed.

Late accepted results update the current cumulative ranking. They do not change an already published monthly snapshot.

## When the dashboard updates

Result submissions normally trigger ingestion immediately. Scheduled result sweeps run at 17 and 47 minutes past every UTC hour if an event was delayed.

The reviewed bot catalog is checked at 23 minutes past every UTC hour. When its normalized content is unchanged, `rumble-data` skips aggregation, commit, and deployment. A new bot or version changes the catalog and triggers regeneration.

Changed ranking data explicitly requests a dashboard deployment. A separate Pages reconciliation runs at 41 minutes past every UTC hour and deploys only when the current site differs from the latest successful deployment. GitHub Actions schedules can run late, so these times are operating cadences rather than deadlines.

The dashboard's **ranking data last updated** value changes only when the current leaderboard or bot-detail data changes. An unchanged poll, identical aggregation, deployment, or monthly snapshot does not make the ranking appear newer.

## Verify the ranking data

The ranking inputs, aggregation code, generated projections, publication history, and governance rules are public in [`rumble-data`](https://github.com/robocode-dev/rumble-data). Accepted result facts remain immutable; moderation changes which facts are eligible without deleting them.
