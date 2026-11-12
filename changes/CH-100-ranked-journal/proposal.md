---
id: CH-100
type: change
status: open
links: [P-007, CAP-016, ADR-054]
title: Keep every completed ranked battle in a durable journal of its own in the shared library, with nothing lost on a crash and no way to pick favourable results
---

# Keep every completed ranked battle in a durable journal of its own in the shared library, with nothing lost on a crash and no way to pick favourable results

A contribution session has chosen its next battle, but nothing yet keeps the result. The plan item for contributor-controlled unattended sessions (M-030) asks that a completed ranked result reaches a durable journal before any upload, survives an interrupted process, a network failure and a rate limit, and never offers a way to keep only favourable results. The shared library (`lib:rumble`) has a queue that lives in memory only and an import of the old client's files; it has no journal of its own.

This slice builds only the journal: the record of one completed ranked battle, a file per Rumble environment that records are added to and never rewritten, and the reading and state changes on top of it. Running the battles, batching, posting and receipts are later slices that use it.

## Why

Everything after the battle depends on the journal being trustworthy: batches are cut from it, receipts are matched to it and a contributor's computer time is only worth something if a result survives a crash. Building it alone, with a simulated crash, is far cheaper than finding out later that a power cut lost a day of battles or that a half-written last line makes the whole file unreadable.

## What changes

- **A record of one completed ranked battle** holds the fields the result data expects: a battle ID, the completion time, the client identity and version, the engine's behavior version, the game type, the rounds, the catalog commit the bots came from, and each participant's name, version, source hash and full result. A battle that did not complete is never a record.
- **One journal per environment.** The file lives in that environment's state folder, and a journal refuses to be used for the other environment, as the pending queue already does.
- **Adding a record is durable before it returns.** The record is written as one line, flushed to disk, and only then reported as kept. An interrupted write leaves at most a torn last line, which is ignored and reported, never taken for a record, and never stops the earlier records from being read.
- **The file is only ever added to.** Moving a record on (waiting, handed to a batch, accepted, rejected, set aside) is a separate line that points at the record, so the history is kept and a crash cannot lose a record by half-applying a move.
- **A record from another engine epoch is set aside, not sent.** When the engine's behavior version in the Rumble's current pin differs from the record's, the record is marked as set aside with that reason and stays available.
- **There is no filter.** The journal has no operation that keeps, drops or hides a result by how well it went; the only ways a record leaves the waiting state are the receipt-based moves and the epoch rule, and the build checks that the public interface offers no other.
- **The same battle ID is added once.** Adding it again is reported as already kept.

## Challenge

- **Assumption most likely to be wrong:** that flushing a file to disk is dependable on every operating system the desktop program runs on, and that a crash can be simulated faithfully in a test. A flush that is only a request to the system would make "durable" a claim the test cannot support.
- **Credible alternative:** keep the journal in an embedded database. Rejected for now: a plain file of lines is what the old client used, can be read and backed up by a person, and the contributor guides already tell people to keep it; a database adds a dependency to the shared library for no need in this slice.
- **Cheapest test:** write records, cut the file at every byte position of the last line, and read it back each time; also kill a real child process in the middle of adding records on this computer and read what is left.
- **Result that revises the work:** any cut position that loses an earlier record or makes the file unreadable, or a record that was reported kept and is missing after a killed process, stops the slice.
- **How it could pass every criterion and still fail a contributor:** the journal is correct but a contributor cannot tell what happened to their results. The states are therefore plain codes with the record's own details, so that the later screens can show completed, waiting, handed over, accepted, rejected and set aside separately, as the plan asks.

## Out of scope

Reading the old client's journal files that the import copied unchanged into their own folder: their format has not been checked against this one here, so whether they are read, converted or left alone is decided in a later slice. Also out of scope: cutting batches, posting, receipts, running battles, the replay evidence store and the screens.

## Acceptance criteria

These will be written as criteria of the Rumble client capability, each with its identity, when the work starts. Test class Unit unless stated.

- **A completed ranked result is durable before it is reported kept, and reading it back gives the same record.** (Integration: a real child process is stopped in the middle of adding records.)
- **A torn last line is ignored and reported, and never costs an earlier record.** Cut at every position, both directions.
- **The journal belongs to one environment.** Using it for the other is refused.
- **The same battle is kept once.**
- **A record moves on by a line that points at it, and the earlier lines are never changed.**
- **A record from another engine epoch is set aside with its reason and stays available.**
- **The journal offers no way to keep or drop a result by its outcome.** A build check of the public interface.

## Identity allocation

The change was allocated with the repository's coordinated allocation. Criterion identities are allocated when the criteria are written.

## Human evidence

None expected: every criterion here is proved by tests.
