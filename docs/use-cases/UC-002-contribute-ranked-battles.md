---
id: UC-002
type: use-case
status: draft
links: [G-004, CAP-001, CAP-012, CAP-015, CAP-016]
title: Contribute computing time with control over bot priorities
provenance: inferred
reversal-cost: low
---

# Contribute computing time with control over bot priorities

## Actors

A contributor who may own no bot, the GUI or CLI session controller, isolated battle workers, and result ingestion.

## Trigger

The contributor wants to donate computer time to public rankings or exercise the development Rumble.

## Main flow

1. Select the environment, sign in through GitHub, and complete automatic checked registration; inspect and resolve execution prerequisites.
2. Choose formats, resource limits, session duration/count, and all-bot, preferred-selection, or strict-selection scheduling. Select one or more entries regardless of ownership.
3. Start the session. The client synchronizes and runs eligible pinned battles, respecting chosen priorities and resource limits.
4. Every completed ranked battle reaches a durable journal; enabled automatic submission sends complete batches and collects durable receipts.
5. Inspect completion, queue, acceptance and publication separately, open rankings or a replay, and pause or stop when desired.

## Outcome

The contributor controls where their compute is spent and sees accepted contributions without manually posting each batch. GUI and CLI implement the same behavior.

## Failure and recovery

Strict selection waits rather than running unrelated battles when no valid matchup exists. Interruption, partial rejection, expired authorization and rate limits retain outstanding results. Cancelled battles are not complete results. Switching environments never moves a pending queue; incompatible engine epochs are quarantined with an explanation. A restart resumes safely without duplicate accepted facts.
