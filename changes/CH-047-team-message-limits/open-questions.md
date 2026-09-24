---
id: OQ-002
type: open-question
status: draft
links: [CH-047]
title: CH-047 open questions
---

# Open questions

## Trial result — 2026-09-22

The acceptance gate failed under the five-bot workload: each member broadcast 128 small messages for 60 turns at 30 TPS. The runner measured 40.52 TPS and captured 37,760 sent messages with 264,510 encoded payload bytes, so raw turn throughput was not the constraint. The five recipients recorded 29,440 to 29,568 received messages each, against 30,720 expected, 2 to 3 skipped turns each, and 29,187 to 29,342 ordering violations each.

`MutableTurn` had stored private bot events in a `HashSet`, which made event order non-deterministic and discarded equal messages. The Java and .NET Bot APIs then converted the ordered tick events into `HashSet`s, with the same effect. CH-047 now keeps ordered lists across both routes, and the Java mapper regression preserves duplicate messages in order.

With that repair and a 100 ms test deadline, the five-bot workload delivered all 30,720 messages to every recipient in order, with no skipped turns, at 43.08 TPS. At the normal 30 ms deadline, the workload still failed: each recipient received 29,184 messages, recorded 3 skipped turns, and the runner measured 40.46 TPS. A focused Java Bot API test also drains 512 ordered team messages in one dispatch, so the two-turn event age is not established as the source of the remaining loss. The trial must not publish the 128-message policy.

## Revised 64-message trial — 2026-09-22

The five-bot workload at 64 messages per bot per turn also failed at the normal 30 ms deadline. The runner measured 57.49 TPS and captured 19,200 sent messages with 128,520 payload bytes. Four recipients received 14,912 of 15,360 expected messages and recorded 6 ordering violations and 2 skipped turns; the fifth received 14,848 messages, with 4 ordering violations and 1 skipped turn. The reduced count improves throughput but does not meet the zero-skipped-turn acceptance gate.

## Initial acceptance result — 2026-09-23

The batching gate passed on the matched local Tank Royale API and runner. The five-bot team sent 128 ordered items in one batch per turn for 60 turns, after a ten-turn warm-up, with the standard 30 ms intent timeout and a 30 TPS default. Every bot received all 30,720 expected items in order; there were zero skipped turns during the workload. The runner measured 94.94 turns/s from turn 10 through turn 80. The compact encoded outbound team-message arrays totaled 2,747,040 bytes; estimated teammate payload fan-out was 10,988,160 bytes, excluding WebSocket framing and transport overhead. The passing result accepts the 64-packet, 128-logical-payload, 49,152-byte packet, and 262,144-byte per-turn array limits without compression.

The shared cross-platform boundary suite includes the exact 262,144-byte boundary and rejects 262,145 bytes. It also exercises malformed raw intents, Unicode byte accounting, directed and broadcast batches, order, and next-turn delivery. The bridge team-message conformance tests pass against the matched build. The CombatTeam run completes in both engines with zero reported errors; its score difference is recorded separately below.

The five-round CombatTeam compatibility run against the matched 1.4.0 API and runner completed in both engines with zero reported errors. Classic scored 10,645 and Tank Royale 13,367 (+25.6%); score parity remains a separate discrepancy and is outside the messaging delivery gate.

## Repeatability check — 2026-09-24

On the final PR commit, eleven fresh executions of the five-bot workload ran at the standard 30 ms intent timeout and 30 TPS setting. Ten delivered all 30,720 expected items to every bot in order with no skipped turns. Nine captured passing runs measured 81.1 to 89.3 turns/s; the tenth passed, but its throughput output was not captured. One run failed: one bot recorded a skipped turn, and each of the other four recorded one missing 128-item batch and an ordering gap. This leaves the stress acceptance gate unresolved despite the ten passing executions.

The measured failure is a missed intent deadline during the messaging workload. Per-turn latency traces and a control run without messaging were not captured, so the cause cannot yet be attributed to message processing or host scheduling, and no revised limit is justified by these measurements. Keep the change and PR in draft pending a human decision on further diagnosis or revised trial limits.

## Latency instrumentation and matched control — 2026-09-24

The stress bot now samples minimum and average `getTimeLeft()` during the 60 measured turns, maximum time spent constructing and submitting a batch, and maximum duration of one receive-handler callback. A matched five-bot control constructs the same 128 strings on the same turns but sends nothing. Both tests use the same 30 ms timeout, arena, opponents, and turn count. These are per-bot summaries; the handler duration is per callback rather than the sum of callbacks in a turn, and the test does not measure server-internal processing time.

Eight fresh paired executions completed, counting the initial pair and seven repetitions: seven stress passes and one failure. The next repetition was interrupted after the failure was found. The control passed in each completed pair without skipped turns or unexpected team-message events. In the failed stress run, bot 1 recorded one skipped turn and a minimum time budget of 0 µs. Each of the other four teammates recorded one missing 128-item batch and an ordering gap. Their minimum budgets fell as low as 370 µs, and their maximum batch-construction/submission intervals reached 21,730 µs. Bot 1's maximum individual receive-handler callback was 4,763 µs. In the same pair, the control's minimum and average remaining budgets were 23,221 µs and 29,284 µs, with a maximum payload-construction interval of 6,352 µs.

The failure directly establishes that the candidate workload can exhaust a bot's turn budget on this machine; the control comparison points to combined team-message processing as the load-sensitive path, but the aggregate telemetry does not isolate client encoding, server delivery, callback scheduling, or host scheduling as the source. A passing stress pair measured a 6,718 µs minimum and 26,638 µs average remaining budget, also showing a narrow margin even without a missed turn. The zero-loss, zero-skip acceptance gate has failed; keep the candidate unpublished and the PR in draft.

For a next trial, consider limiting each bot to 64 logical payloads per turn while retaining the 49,152-byte packet and 262,144-byte per-turn array caps initially. The measured 128-entry batch packet was about 9.2 KiB, well below either byte cap, so lowering byte limits would not target the observed count-heavy workload. The 64-entry batch candidate is unverified; the earlier 64-message count-only workload used a different, unbatched path and also failed its gate. Do not change the trial policy until a human selects a candidate and its stress gate passes.

## Batched 64-entry trial — 2026-09-24

The five-bot team sent one batch of 64 ordered payloads per sender for 60 turns at the standard 30 ms timeout and 30 TPS setting. Ten fresh paired executions completed with all 15,360 expected logical items received by each bot in order, zero skipped turns, and no test failures. The matched control constructed 64 payload strings on the same turns and sent none; it also completed all ten runs without skipped turns or unexpected team messages. Each paired run used the same arena and opponents and forced the Gradle test task to rerun.

Across the ten stress runs, measured throughput ranged from 99.1 to 118.6 turns/s, average remaining turn budget from 26.958 to 27.589 ms, and minimum remaining budget from 8.581 to 15.803 ms. Maximum batch construction and API submission time ranged from 8.716 to 13.655 ms; the slowest single receive-handler callback was 6.164 ms. The control's minimum remaining budget ranged from 21.234 to 25.461 ms, with a 29.325 to 29.453 ms average. The encoded outbound arrays totaled 1,378,320 bytes; estimated teammate payload fan-out was 5,513,280 bytes, excluding WebSocket framing and transport overhead.

All ten 64-entry runs passed on the user's fast local PC, but their minimum budget fell as low as 8.581 ms. This does not establish reliability on average or lower-spec hardware, and it does not resolve the 128-entry failure. The 64-entry test fixture is measurement evidence only; do not change the candidate API limit or publish the policy based on this host's results. Keep PR 276 in draft until the chosen trial passes on representative slower hardware or an agreed calibrated CPU limit.

## Blocking decision

Resolved by the user on 2026-09-23: trial standard ordered batching in one existing packet and event, with no compression at first. Retain the trial packet and byte caps, require batch version 1 from all recipients, and compare the five-bot 30 TPS result against the same zero-loss, zero-skipped-turn gate. The trial does not become a published policy unless that gate passes.
