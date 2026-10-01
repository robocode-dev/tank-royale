---
id: AN-002
type: analysis
status: active
links: [G-001, CAP-019]
title: CombatTeam scoring and skipped-turn retest
---

# AN-002 — CombatTeam scoring and skipped-turn retest

This record preserves the post-fix CombatTeam comparison and its skipped-turn telemetry for the team-scoring repair documented by [CAP-019](../capabilities/CAP-019-battle-scoring/README.md). The raw run data is in [AN-002-combatteam-retest.json](AN-002-combatteam-retest.json).

## Setup

CombatTeam 3.25.0 ran on a 1200×1200 battlefield for 10 rounds with two teams of five using Tank Royale 1.4.0 from source commit `9ce6a9312a3017a28b1289d5f0098cbf168e8b42`, bridge API 0.5.0 from commit `134494f32d62c794dd5f5136cd5d01b591fd3ad9`, and wrapper 0.3.1. The CombatTeam jar SHA-256 is `64618918a708b58cd8877b78071e64dc77239fdf43113fade60f566962f04268`.

## Findings

The five component-capture attempts all recorded every Tank Royale bot (IDs 1–10) skipping round 1, turn 1. Additional recorded skip events were:

| Attempt | Additional skipped turns and bot IDs |
|---|---|
| 1 | Round 1, turn 135: bot 5 |
| 2 | Round 1, turn 2: bot 3; round 1, turn 78: bots 1, 2, 3, 4, 5, 6, 8, 9, 10; round 1, turn 79: bot 7; round 3, turn 46: bots 2, 5, 7, 8, 9, 10; round 3, turn 47: bots 1–10 |
| 3 | Round 1, turn 80: bots 1–10; round 2, turn 6: bots 1, 3, 4, 5, 7, 9, 10 |
| 4 | Round 1, turn 78: bots 2, 6, 9, 10; round 1, turn 80: bots 2, 7, 8; round 1, turn 136: bot 10 |
| 5 | Round 1, turn 78: bots 1, 8, 9; round 1, turn 239: bot 2; round 3, turn 108: bots 1–10 |

Attempts 1 and 2 recorded no bot errors. Their aggregate survival totals were 12,500 in both engines in attempt 1 and 12,450 in Classic versus 12,500 in Tank Royale in attempt 2. Attempts 3, 4, and 5 recorded three, four, and two bridge-side `NullPointerException`s, respectively; their score components remain in the raw JSON as diagnostic output. These five captures are not a clean five-run parity pass.

The telemetry confirms that round 1, turn 1 skips occur during warm-up in this workload and that later skips also occurred in these attempts. It does not show that scoring caused any skip, nor does it establish skip-free execution. The scoring unit tests cover the event-level rules independently; CombatTeam provides partial end-to-end score evidence with the above reliability limit.

## Implication

Keep skipped-turn events attributable to their recorded round and turn when interpreting this workload. Do not treat the warm-up event as evidence of a scoring regression, and do not claim the captured runs had no later skips or no bridge errors.
