---
id: CH-050
type: change
status: open
links: [G-001]
title: Match classic team survival scoring
---

# CH-050 — Match classic team survival scoring

**Plan:** This change is plan-less; no active plan item covers score parity in team battles.

## What and why

The official CombatTeam confirmation measured a 26.98% mean score increase in Tank Royale across five matched 10-round battles. In the final repeat, Tank Royale awarded 21,200 survival points and 360 last-survivor points, compared with Classic Robocode's 12,500 and 1,000. The source explains the component mismatch: TurnProcessor submits the bots defeated on the current turn, and ScoreTracker grants one survival award to every remaining bot whenever that set is non-empty. This includes teammates and does not count multiple deaths in the same turn separately. Classic processes each dead bot and awards survival only to living bots on other teams. Tank Royale also detects a last survivor only when one individual bot remains, while Classic awards the living members of the last team and computes the bonus from opposing bot count.

Make Tank Royale scoring follow those existing Classic mechanics. For each newly dead bot, award 50 survival points to each living bot on another team exactly once; same-team deaths and repeated dead state on later turns award nothing. When only one team remains, award each living team member 10 points per opposing bot, once. Team results continue to sum their members' individual scores. Increase behaviorVersion as required by ADR-042 because scores and rankings can change.

Update the published scoring and team strategy articles to explain per-bot death events and team aggregation, and add the fix to the existing 1.4.0 changelog. Keep the product version at 1.4.0; do not publish a release as part of this change.

## Commitment challenge

The assumption most likely to undermine this repair is that team scores should equal the sum of each member's Classic-compatible score, including survival points for every living opponent on each enemy bot death. The credible alternative is to preserve Tank Royale's team-level simplification and only stop repeat awards for the same dead bot. The user confirmed that Classic scoring is the target, and Classic's Battle.handleDeadRobots() and RobotStatistics.scoreSurvival() show the event-level rule. The cheapest useful test is a deterministic server unit case with two teams of multiple bots: one death, a repeated dead-state update, simultaneous deaths, and a team win. Stop and revise if those exact cases do not match the Classic score calculation or if the public team result fails to equal the sum of the surviving members' scores.

An implementation could pass its unit tests and still fail bot authors if the server fixes the per-bot fields but publishes a different aggregate through round-end or game-end results. Exercise the team aggregation path and rerun CombatTeam against a matched local runner before calling the score discrepancy resolved.

## Scope

The change adds explicit server scoring criteria, repairs per-bot survival and last-survivor scoring, bumps the battle behavior epoch, updates user-facing scoring documentation and changelog, and records matched CombatTeam regression evidence. It does not change Bot API behavior, message limits, skip telemetry, or the Tank Royale product version.

## Post-fix CombatTeam verification

On 2026-10-01, I ran CombatTeam 3.25.0 at the official teamrumble setup (1200×1200, 10 rounds, two teams of five) using the Tank Royale runner and Java Bot API built from this change branch at 1.4.0, plus bridge API 0.5.0 and wrapper 0.3.1 built from bridge commit `134494f32d62c794dd5f5136cd5d01b591fd3ad9`.

The standard five-run score confirmation completed four runs without a bridge-only error; score deltas were −5.2%, −6.1%, −8.1%, and −7.3%, averaging −6.675%. The fifth run stopped on a bridge-only `NullPointerException` in `mn.c.e.equals`.

A second five-run comparison captured every reported team score component and skipped-turn event. Attempts 1 and 2 completed without bot errors, with score deltas of −6.84% and −6.83%. Across those two clean component captures, aggregate survival was 12,500 in both engines in attempt 1, and 12,450 in Classic versus 12,500 in Tank Royale in attempt 2. Attempts 3, 4, and 5 completed at the runner level but logged three, four, and two bridge-side `NullPointerException`s respectively; their scores are recorded as diagnostic output, not clean parity evidence. Detailed per-team components, pinned artifacts, exceptions, and all `{bot_id, round, turn}` skip events are in [combatteam-retest.json](combatteam-retest.json).

All ten Tank Royale bots reported the round 1, turn 1 warm-up skip in each component-capture attempt. Additional skips were also recorded later, including rounds 1, 2, and 3; the exact bot IDs and turns are in the linked JSON evidence. The score correction removes the prior +26.98% mean CombatTeam discrepancy in the error-free comparisons, while the repeated bot exception prevents treating the whole CombatTeam retest as a clean pass.
