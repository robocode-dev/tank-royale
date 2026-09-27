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

## CPU-capped 64-entry trial — 2026-09-27

The first paired run used a Windows Job Object hard cap of 25% of total host CPU capacity across the full Gradle, runner, and bot process tree on the AMD Ryzen 7 9800X3D (8 physical and 16 logical processors). Windows accounting measured 19.2% average use of total system CPU over the 36.2-second pair. This is a constrained run on the user's fast PC, not a run on lower-spec hardware; the cap also does not establish a general minimum hardware profile. The repeat loop stopped after this first failing pair.

Under stress, each bot received 14,336 of 15,360 expected logical items, a deficit of 1,024 items or 16 batches of 64 per bot. Each bot recorded 16 ordering gaps and 4 skipped turns; the bot telemetry showed no message-type, batch-size, or content errors. Every bot's minimum `getTimeLeft()` was 0 µs; average remaining time ranged from 26,124 to 27,148 µs. Maximum batch construction and API submission time ranged from 4,744 to 17,162 µs, and maximum receive-handler callback duration ranged from 902 to 1,703 µs.

The matched no-message control passed under the same cap with no skipped turns or unexpected messages. It measured 105.40 turns/s, a 25,547 µs minimum and 29,560 µs average remaining budget, and 3,831 µs maximum payload-construction time. The comparison points to the messaging workload as load-sensitive, but these measurements do not isolate serialization, server delivery, callback scheduling, or host scheduling as the bottleneck.

The zero-loss, zero-skip gate failed for the 64-entry batch under this CPU-capped run. Keep PR 276 in draft and do not publish or change the candidate policy from this result alone. For a human-selected next trial, consider a limit of 32 logical payloads per bot per turn while initially retaining the 49,152-byte packet and 262,144-byte per-turn array caps; those byte limits were not approached by this count-heavy workload. This is only a trial recommendation and needs its own stress gate before becoming policy.

## CPU-capped 32-entry trial — 2026-09-27

The 32-entry stress fixture now records the exact skipped-turn numbers in the measured window, turns 11–70; turns 1–10 remain warm-up and are not counted. Two five-bot stress runs failed the delivery gate. In the first, each bot received 7,296 of 7,680 expected items and skipped turns 15, 32, and 61. In the second, bots 1, 2, and 7 skipped turn 18; bots 4 and 5 had no skipped turns. Recipients received 7,584 or 7,616 items, with the deficits matching the batches sent on the skipped turn. The second run measured 39.61 turns/s, a 16,786 µs minimum and 27,879 µs average remaining budget.

The matched 32-entry no-message control also failed once: all five bots skipped turn 32. It received no messages, measured 96.31 turns/s, and reported a 24,512 µs minimum and 29,458 µs average remaining budget. Since the control also skipped a turn, not every skip can be attributed to team-message processing. The stress run's turn-18 losses still show that the 32-entry messaging workload can miss intents under this cap.

All three runs used a 25% Windows Job Object CPU hard cap across the Gradle, runner, and bot process tree on the Ryzen 7 9800X3D with 16 logical processors. The initial invocation measured 366.0 job CPU seconds over 100.1 seconds, about 22.9% of total system capacity; later invocations used the same cap, but the local summary script did not emit their CPU averages. Because both stress and control runs recorded skips, this does not establish 32 as reliable or identify a single messaging bottleneck. Keep the policy unchanged and PR 276 in draft; do not lower the candidate limit or publish it from these measurements. A further human decision is needed on whether to measure on representative hardware or isolate runner scheduling under the same CPU cap.

## Blocking decision

Resolved by the user on 2026-09-23: trial standard ordered batching in one existing packet and event, with no compression at first. Retain the trial packet and byte caps, require batch version 1 from all recipients, and compare the five-bot 30 TPS result against the same zero-loss, zero-skipped-turn gate. The trial does not become a published policy unless that gate passes.

## Deferred skipped-turn diagnosis plan

Before changing trial limits, determine whether synchronized skipped intents originate in Tank Royale's server, runner, Bot API runtime, or host scheduling.

1. Verify the captured skip bitmaps and intent records for turns 15, 18, 32, and 61; keep turns 1–10 classified as warm-up.
2. Capture monotonic per-turn timestamps for tick dispatch, bot tick receipt, intent arrival at the server, skipped-turn detection, and event delivery, including bot IDs.
3. Repeat matched no-message and 32-entry stress workloads with the same team, 30 ms timeout, 30 TPS setting, and warm-up, first under the 25% process-tree CPU cap and then without the cap; record process-tree CPU use for each run.
4. Compare bot send times with server intent arrivals and timeout checks. If intents arrive before the deadline but are treated as absent, inspect server turn bookkeeping and `checkForSkippedTurns`; if bots send late together, inspect runner and host scheduling; otherwise trace the affected Bot API send and event paths.
5. If repeats cluster around powers of two, inspect turn counters, event queues, and buffer boundaries for wraparound or off-by-one behavior; the current observations alone do not establish such a pattern.
6. Keep the candidate limits and published policy unchanged until the cause is understood and the zero-loss, zero-skip gate passes. Keep PR 276 in draft during the investigation.

## Explicit 30 TPS matched diagnosis — 2026-09-27

The opt-in diagnostic runner now passes `--tps=30` to the embedded server. Before this probe, `ServerManager` always passed `--tps=-1` (max speed); setting `BattleSetup.defaultTurnsPerSecond=30` did not throttle the server. Earlier measurements above therefore record their configured battle default, not an effective 30 TPS server run.

One matched pair ran with a 25% Windows Job Object CPU hard cap over the full Gradle, runner, and bot process tree. The stress case measured 24.63 TPS and the no-message control 21.28 TPS. The stress bots all skipped measured turn 11, the first send turn after the 1–10 warm-up, and each received 7,552 of 7,680 expected items with four ordering errors. The control had no measured skips. Server traces show tick 11 dispatched, the skip event sent, then the late intents arriving in turn 12. The skip timestamps were synchronized across the five workload bots. Job accounting measured 112.8 CPU seconds over 38.7 seconds (2.91 average cores, 18.2% of the 16 logical processors); this includes Gradle setup and is not a battle-only CPU measurement.

Two uncapped matched pairs completed with zero skips. In the first, stress delivered all 7,680 expected items per bot with no ordering or content errors at 25.17 TPS; control measured 25.63 TPS. In the repeat, stress measured 21.29 TPS and control 21.63 TPS, again with complete ordered delivery and no skips. Uncapped process-tree accounting for the repeat was 107.7 CPU seconds over 30.0 seconds (3.58 average cores, 22.4% of the 16 logical processors), including Gradle setup. The measured stress and control rates tracked within 0.5 TPS in both uncapped runs.

The acceptance gate is not met: neither uncapped run reached 30 effective TPS, and the capped control was also below 30 TPS. The 32-entry messaging workload delivered reliably in both uncapped runs, while the capped run showed a synchronized first-send-turn miss. These results point to a shared runner/server/host pacing limit at the requested 30 TPS and do not justify a revised message count or byte limit. The source of the lower effective rate is not isolated yet; the historical skips at turns 15, 18, 32, and 61 have not been reproduced with this timing trace. Keep the candidate policy unchanged and PR 276 in draft. Before another limit decision, fix or calibrate the local 30 TPS measurement path, then repeat the matched test and record the effective rate and skip turns.

## Local pacing correction and post-warm-up repeat — 2026-09-27

The server now starts the next response timer while still holding the tick lock, after dispatching the new tick and clearing the prior intents. This closes the interval where bots could answer the new tick before its timer was active, causing `notifyReady()` to be ignored. Turn pacing now advances against an absolute tick deadline, subtracts the observed model-update duration, and re-bases on game start, pause/resume, or TPS changes; late OS wake-ups no longer accumulate from turn to turn. The benchmark begins timing at turn 11 because turns 1–10 are warm-up.

With explicit server `--tps=30`, the post-fix uncapped five-bot 32-entry stress completed with 7,680/7,680 ordered items per bot, zero skipped turns, and zero protocol or content errors. The latest standalone stress run measured 30.13 TPS. In two matched repetitions, stress measured 29.98 and 30.20 TPS while the no-message control measured 29.97 and 29.98 TPS. An earlier matched run with a 1.0 ms scheduling margin measured 30.18 TPS for stress and 29.98 TPS for control. Each stress run had complete delivery and no skipped turns. The 30 TPS threshold is therefore met by the latest standalone run and two of the three latest matched stress runs, with one miss of 0.02 TPS; the matched Gradle task still exits nonzero because the control test independently requires 30 TPS.

The measured rates remain close for stress and control, so the 32-entry message load is not the rate bottleneck. Buffered traces still show occasional Windows timer wake-ups several milliseconds later than the requested delay; absolute deadlines let subsequent turns catch up, while short 60-turn samples vary by a few hundredths of a TPS. This local evidence does not establish performance on slower machines, and no post-fix CPU-capped repeat has been run. Keep the message limits and published policy unchanged; do not treat this trial as a release gate pass until the strict repeated 30 TPS gate is resolved.

## Sustained 500-turn local repeat — 2026-09-27

This section supersedes the preceding 60-turn conclusion about the unresolved local 30 TPS gate. The matched trial now measures 500 send turns after warm-up turns 1–10, from observer tick 11 through tick 511. It captures final delivery and skip telemetry separately after the measured interval. The embedded server is explicitly started with `--tps=30` through the runner's opt-in timing diagnostics setting; `BattleSetup.defaultTurnsPerSecond=30` alone does not throttle it. Two passive no-message bots replace the combat opponents in this long benchmark so all five workload bots remain alive through the measurement. A rolling 60-turn skip bitmap is sampled on each observer tick and reconstructs exact skipped-turn numbers across the full 500-turn window.

Two matched uncapped repetitions passed the strict 30 TPS and zero-skip checks. Stress measured 30.00261 TPS and 30.00055 TPS; the no-message control measured 30.00318 TPS and 30.00161 TPS. In both stress runs, all five bots received 64,000 of 64,000 expected logical messages in order, with no protocol or content errors and no skipped turns. The corresponding control bots also had no skipped turns. An earlier 300-turn attempt missed the stress threshold by 0.0035 TPS and used combat opponents that eliminated workload bots before final telemetry; it was superseded by the longer passive-opponent test. An unpaced diagnostic-disabled attempt measured above 200 TPS and was discarded because the server had `--tps=-1`.

This establishes the local five-bot 32-entry workload at 30 TPS for two 500-turn repetitions with timing instrumentation enabled. It does not establish performance on slower machines, and no post-fix CPU-capped run has been made. The trial message limits and published policy remain unchanged pending the other release-gate evidence.

## 1,000-turn post-fix repeat and capped failure — 2026-09-27

This section supersedes the 500-turn local conclusion above. I extended the matched workload to 1,000 sends after the 1–10 warm-up, widened the stress bot's received-count telemetry to 24 bits, and retained rolling exact skip-turn reporting. The embedded server is explicitly paced at 30 TPS. The test now reports observer-callback timing separately and calculates the acceptance TPS from server monotonic tick-dispatch timestamps for turns 11 and 1,011.

In the latest uncapped 1,000-turn pair, every stress bot received all 128,000 expected logical messages in order with zero skips or protocol/content errors. Server-dispatch timing measured 29.99767 TPS for stress and 29.99992 TPS for control, narrowly below the strict 30 TPS assertion; the observer-callback measurements were 29.99946 and 30.00174 TPS. Earlier 500-turn uncapped runs passed twice, so local 30 TPS pacing is close to the threshold but not repeatably above it over the longer window.

The post-fix 25% Windows Job Object capped 1,000-turn attempt failed the zero-skip delivery gate. The stress bots skipped turns 17 and 43: bots 1, 2, 3, and 6 skipped both; bot 7 skipped turn 43. They received 127,776 items each except bot 7, which received 127,744, against 128,000 expected; the no-message control had no skips. Stress time-left telemetry reached 0 µs (29,593 µs average); the control minimum was 25,689 µs. The cap-run observer measured 30.14 TPS for stress and 30.00 TPS for control, which did not prevent the message loss. Job accounting recorded 242.86 CPU seconds over about 127 seconds (1.91 average cores), below the cap's four-core allowance on this 16-logical-processor host.

Server traces show the turn-17 advance callback arriving about 399 ms after its timeout was scheduled. Around turn 43, a requested 17.8 ms pacing wait lasted 174.8 ms. These host-side stalls align with the synchronized skipped turns; they do not point to JSON message size or ordering logic. The 25% Job Object run is a constrained run on this fast PC, not a representative slower-machine measurement, and its average CPU use did not saturate the cap. The strict local 30 TPS/zero-skip gate is therefore not established across capped runs. Keep the candidate limits and published policy unchanged and PR 276 in draft pending a human decision on representative hardware or a better calibrated scheduling test.

## Uncapped 1,000-turn rerun — 2026-09-27

The matched 32-entry stress and no-message control tests were rerun with the server paced at 30 TPS, after warm-up turns 1–10, over 1,000 measured send turns. The stress bots each received all 128,000 expected items in order; both workloads recorded zero skipped turns, zero protocol errors, zero content errors, and empty exact skipped-turn lists for all five bots. The stress run measured 29.99705 server-dispatch TPS (29.99839 observer-callback TPS); the control measured 29.99839 server-dispatch TPS (29.99911 observer-callback TPS). Minimum and average remaining turn budgets were 21.081 ms and 29.553 ms for stress, and 25.767 ms and 29.865 ms for control.

Both tests failed the strict 30 TPS assertion, by about 0.003 TPS for stress and 0.002 TPS for control. This rerun confirms complete delivery and no skips on the uncapped local machine, but it does not pass the throughput gate or replace the capped-run failures above. Keep the candidate policy unchanged and PR 276 in draft.
