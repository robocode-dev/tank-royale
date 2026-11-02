---
id: CH-081
type: change
status: open
links: [P-007, M-029, M-030, M-034, ARCH-027, ARCH-028, CAP-016, CAP-022, UC-001]
title: Copy the old Rumble client's state into the new Rumble state without changing or losing any of it
---

# Copy the old Rumble client's state into the new Rumble state without changing or losing any of it

This is the last slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). The plan says the new shared modules replace the separate Rumble client repository, and that its configuration, client ID, journals, receipts, replays and own-bot preferences must be moved over non-destructively before the old client can be retired. Nothing has been moved so far, and the shared library has no place yet for most of it. This change builds the safe copy: it reads the old client's files, never changes them, and puts verified copies and a record of what was copied into the new state folder of the right Rumble environment. No screen is added.

## Why

A contributor who has run the old client holds things that cannot be recreated: a registered client ID, a journal of completed ranked results that may not have been uploaded yet, receipts that prove earlier results were accepted, and replay evidence. Losing the journal means losing unsubmitted results; mixing the old client's data into the wrong environment means public results end up in the test Rumble or the other way round. Doing the copy once, carefully, before the new session features exist, means they can be built against data that is already safe.

## What changes

- A reader of the old client's configuration file that accepts only the schema version it knows and refuses anything else with a plain message. It finds the client ID, the own-bot list, the game types, the battles per session, the mode and the work folder, and from the two repository addresses it works out which Rumble environment the state belongs to. A pair of addresses that belong to no known environment, or to two different ones, is refused before anything is copied.
- A preview that lists, without writing anything, every file that would be copied from the old work folder (the journal files, the receipts, the replay evidence and the bot cache are named), how large it is, and anything that cannot be copied, together with the folder it would go to.
- The copy itself. Each file is copied byte for byte into a folder of its own for imported old state below the environment's state folder, written first under a temporary name, checked against the source by a SHA-256 hash, and only then given its final name. The old files are opened only for reading and are never moved, renamed, changed or deleted, and the old folder is never written into.
- A record of the import, written last, listing every file with its source path, size and hash, the time, and the program version. It is the proof that the import completed. Running the import again does nothing for files already copied and unchanged, copies files that were missing or that changed since, and reports what it did, so an interrupted import is simply run again.
- A typed result: imported (with counts), nothing to import, refused (unknown version, unknown or mixed environment) or failed with a plain reason, such as a file that could not be read or a disk that is full.
- The parts that already have a home are put there as well: the client ID and the own-bot preferences are read into a small typed preferences record in the environment's state folder. Everything else (the journal, receipts, replays) is preserved verbatim and not converted, because the new journal, receipt and replay formats do not exist yet; the later session slices will read from the preserved copy, and the criteria for those slices will say how.
- The fixture check gets a case of its own for this, named the old client import.
- Acceptance criteria written before the code, in a new capability for the old client import, with positive and negative tests. The participation design says what is copied, what is only preserved, and what is deliberately left alone.

## Challenge

The assumption most likely to be wrong is that a copy made while the old client might still be running is a consistent copy. The old client appends to its journal while it runs, so a file can change between the moment it is read and the moment its copy is checked, and a file copied halfway through a write may end in a broken last line. The import therefore hashes the source before and after reading it and treats any difference as a file that changed during the import, which is reported and left for the next run, and the record is not written as complete until every file matched. It cannot make a half-written last line whole; the copy is verbatim, so whatever the old client wrote is what the new slices will have to read, including how they treat a broken last line, and the criteria for those slices must say so.

The second weak spot is that the formats of the old files are not documented in this repository and were read here only from the old client's source and its example configuration. Because the copy is byte for byte and only the configuration file is parsed, an unexpected journal format cannot make the import lose or damage anything; it can only make a later reader refuse it. The configuration parser is strict and refuses an unknown schema version, so a future old-client version is refused and not guessed at.

The third is that a mistake in telling the environments apart would put public data in the test environment or the reverse. The environment comes only from the two repository addresses, using the same exact-match rule the shared library already uses to tell its environments apart, and a mix of one public and one test address is refused. A client ID registered in one environment is never copied into the state of the other.

## Not in this change

No conversion of the journal, receipts or replays, no upload of imported results, and no change to what the old client does. The old files are never moved or deleted: retiring the old client, and offering the person a way to remove the old files afterwards, are decisions for the moment a replacement session exists. No reading of the old client's token, which it takes from an environment variable and never stores. No change to the desktop program's bot directory setting; the extension of that setting for downloaded bots, with its label, belongs to the download feature, which must read the existing entries without losing any. No screen.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new library code stays unpublished and shows nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
