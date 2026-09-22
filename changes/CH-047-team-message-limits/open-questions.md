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

`MutableTurn` had stored private bot events in a `HashSet`, which made event order non-deterministic and discarded equal messages. CH-047 now carries an ordered-list repair, verified through the bot tick mapper. The repeated stress run still misses messages and records skipped turns because client event dispatch cannot drain the inbound batch within its two-turn event-age window. The trial must not publish the 128-message policy.

## Blocking decision

Should the next trial first preserve ordered private events and then retest a lower per-turn message count, or should it keep the 128-message target and introduce a separately bounded inbound event queue with an explicit overflow rule? The acceptance gate requires a human decision before either policy is adopted.
