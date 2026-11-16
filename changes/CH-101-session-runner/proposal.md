---
id: CH-101
type: change
status: open
links: [P-007, CAP-016, CAP-021, ADR-054]
title: Run a contribution session's chosen battles one at a time on the worker and keep each completed result in the journal, with pause and stop that never keep a half-finished battle
---

# Run a contribution session's chosen battles one at a time on the worker and keep each completed result in the journal, with pause and stop that never keep a half-finished battle

The shared library can now choose the next ranked battle of a contribution session and can keep a completed result durably, and it can start the battle worker as a separate process. Nothing yet joins the three: no code takes the chosen battle, puts the bots in place, runs it on the worker and writes the result to the journal. The plan item for contributor-controlled unattended sessions (M-030) asks for exactly that loop, with one battle at a time, a bound on the number of battles or none, and pause and stop that finish or cancel the active battle without ever turning an unfinished battle into a ranked record.

This slice builds that loop in the shared library only: no screen, no command, no network except the existing reading of the catalog and the bot sources. Cutting batches, posting and receipts, and the screens and command-line commands that start a session are later slices.

## Why

This is the first time the pieces meet, so it is where wrong assumptions between them show. The planner speaks in entry names, the worker in folders and hashes, the journal in a record with the engine's behavior version and the catalog commit. Building the join alone, with a fake worker for speed and one real battle for truth, finds those mismatches before screens depend on the loop.

## What changes

- **A session runner in `lib:rumble`** takes a session request, a snapshot of the catalog at one commit, the Rumble's engine pin, the client identity, a folder for bot sources, the worker command, the journal and a way to ask it to pause or stop. It repeats: ask the planner, place the chosen bots, run one battle on the worker, and on a completed battle add exactly one record to the journal.
- **Bots are placed by hash.** Each chosen entry is fetched from the catalog commit into a cache folder, checked against the catalog's source hash, and reused while the hash matches. A bot that cannot be placed (hash mismatch, missing from the archive, no network) is not run; the session reports it and chooses another battle without it for a while rather than stopping.
- **Only a completed battle becomes a record.** A worker that refuses the job, fails, times out, exceeds a limit or ends without a finish produces a typed outcome and no record. A battle on another behavior version than the engine pin is never started, because the worker already refuses it.
- **One battle at a time, with limits.** The concurrency is one. The worker's total time and output limits and its memory setting come from the session request.
- **Pause finishes the active battle, then waits. Stop** has two forms: finish the active battle and stop, or cancel it now, in which case the worker and everything it started are ended and nothing is recorded.
- **The loop reports what it does** as plain events (battle started, completed and kept, not kept with its reason, waiting with the planner's reason, paused, finished) that the later screens and command line show, instead of writing text itself.
- **The record is made from the job and the result alone.** The record's participants and their source hashes come from what was run, not from what was chosen.

## Challenge

- **Assumption most likely to be wrong:** that the pieces fit as they stand. The Rumble's engine pin (`engine.json`) is not read anywhere in the library yet, so the loop takes it as an input and a later change decides where it comes from; if the pin's shape differs from what the worker and the journal expect, this slice stops there.
- **Credible alternative:** run battles inside the desktop program and the command line separately and share only the planner. Rejected: that is the divergence the shared library exists to prevent.
- **Cheapest test:** run the loop against the existing fake worker for the fast cases, and run one real battle with two sample bots through the real worker to see that a real result becomes a record that the journal reads back.
- **Result that revises the work:** a real result that cannot be made into a record without guessing a field, or a cancelled battle that leaves a worker process alive, stops the slice.
- **How it could pass every criterion and still fail a contributor:** the loop is correct but a contributor's computer is quietly busy or idle for no visible reason. The events therefore name every state, including why it waits, so that the later screens never have to show a silent session.

## Out of scope

Reading the engine pin and the advice from the data repository, cutting batches, posting, receipts, the replay evidence store, running on more than one battle at a time, and the screens and commands. Real battles are tried only with Java sample bots on Windows, as the worker was before.

## Acceptance criteria

These will be written as criteria of the Rumble client capability, each with its identity, when the work starts. Test class Unit unless stated.

- **A completed battle becomes exactly one record in the journal, made from what was run.**
- **A battle that did not complete becomes no record**, for every kind of worker outcome that is not a completion, and the session goes on.
- **A bot that cannot be placed is not run**, and the session chooses another battle instead of stopping.
- **Pause finishes the active battle and then waits; finish-and-stop does the same and ends; cancel-now ends the worker and keeps nothing.** (Integration for cancel-now: a real worker process is ended.)
- **A battle count ends the session, and the planner's wait is reported with its reason instead of spinning.**
- **One real battle through the real worker is kept in the journal and read back.** (Integration, skipped by design on a computer without the bots' runtimes.)

## Identity allocation

The change was allocated with the repository's coordinated allocation. Criterion identities are allocated when the criteria are written.

## Human evidence

None expected: every criterion here is proved by tests.
