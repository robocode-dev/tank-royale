---
id: OQ-002
type: open-question
status: active
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

## Blocking decision

Should the next trial lower the per-turn message count again, starting at 32, or should it profile and remove the remaining deadline misses before selecting another limit? The acceptance gate requires a human decision before either policy is adopted.
