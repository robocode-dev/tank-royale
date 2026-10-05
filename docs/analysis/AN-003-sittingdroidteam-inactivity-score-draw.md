---
id: AN-003
type: analysis
status: active
links: [CAP-019]
title: SittingDroidTeam's zero score is compatible with a draw, but death timing is unobserved
provenance: inferred
reversal-cost: low
---

# AN-003 — SittingDroidTeam's zero score is compatible with a draw, but death timing is unobserved

## Question

Does Tank Royale's scoring code explain the confirmed all-zero result for `logiblocs.SittingDroidTeam_1.0.jar`, or does the result identify a scoring defect?

## Evidence boundary

The source reviewed is Tank Royale commit `5000c678b7fffc8a6147adb5dc4b42b3c6b4d4bb`, the same source used to build the local Runner 1.4.0 and Bot API 1.4.0 artifacts. The consumer is bridge milestone `clue:robocode-dev/robocode-api-bridge/M-006`; its five-repeat observation `61afa98612b51fb6` reports 4,000 Classic points versus 0 Tank Royale points on five official 1200×1200, 10-round, two-team runs. A separate one-pair component capture on those settings reported 2,000 survival points for each Classic team and zero for every Tank Royale component. The diagnostic capture did not record a per-bot death timeline, and no Tank Royale source or acceptance criterion was changed.

## What was inspected

`TurnProcessor.processTurn()` resets the shared inactivity counter when a bullet hits, increments it each turn, and applies 0.1 damage to every bot once the counter exceeds the configured limit. The Classic game preset and Tank Royale's shared default both use 450 inactivity turns. `MutableBot.applyDamage()` subtracts that damage from energy.

`ScoreTracker.registerDeaths()` removes all newly defeated participants before awarding 50 survival points to still-alive participants on other scoring groups. This matches `SCR-001`: simultaneous defeats count separately for opposing bots that remain alive. `SCR-002` explicitly gives no last-survivor bonus for a draw. The round ends when at most one scoring group remains alive or when all remaining groups are disabled.

The corresponding bridge measurement and its component breakdown are recorded in `clue:robocode-dev/robocode-api-bridge/AN-031`.

## Finding

The inspected score tracker has no demonstrated arithmetic defect. If all teams become disabled together under the shared inactivity damage, there are no surviving opponents to receive survival points, so a zero-scoring draw follows from the current implementation and criteria. The captured result is compatible with that path, but the measurement did not record the death set or its turn, so simultaneity remains an inference rather than an observed fact.

The confirmed score difference is therefore not localized to score calculation. The missing evidence is the per-bot death and disabled-state timeline from both engines; until that exists, neither a scoring-criteria change nor a Tank Royale code change is justified.

## Consumer handoff

Bridge milestone M-006 should retain this as an unresolved score discrepancy and collect death-turn evidence before choosing a scoring or lifecycle repair. The cited consumer is `clue:robocode-dev/robocode-api-bridge/M-006`.
