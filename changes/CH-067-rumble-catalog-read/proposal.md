---
id: CH-067
type: change
status: open
links: [P-007, M-029, M-038, ARCH-027, CAP-014, CAP-016]
title: Read the Rumble bot catalog and fetch bot sources with hash verification in the shared library
---

# Read the Rumble bot catalog and fetch bot sources with hash verification in the shared library

This is the third slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). It teaches `lib:rumble` to read a Rumble's bot catalog and to fetch a bot's source from a given catalog commit, placing it on disk only when its content hash matches the catalog. Ranked contribution, bot downloading and the bot pages all need exactly this, and it needs no sign-in because the catalog is public. It adds nothing a user can see: the modules stay unpublished and no menu is shown.

## Why

The catalog says which bots exist and records a content hash for each, so that what runs is exactly what was reviewed. Two later pieces of work depend on checking that hash before trusting a source tree: the download of competitor bots and the ranked battle session. If each wrote its own fetch and its own hash code, the first mismatch in how they compute the hash would silently let unreviewed code through in one of them. One implementation in the shared library, with a test that proves it agrees with the real catalog, removes that risk before either feature exists.

## What was checked first

Before proposing, a read-only check fetched the live public bot repository. The catalog (`bots/index.json`, schema version 1) lists 16 bots. Downloading GitHub's zip archive of the repository at the catalog's own commit and computing each bot's hash with the repository's rule (a SHA-256 over the sorted slash-separated relative paths and file bytes, each followed by a zero byte, skipping `__pycache__`) matched all 16 recorded hashes. So a zip archive is a sound way to obtain the sources, it needs no Git installation, and it keeps line endings exactly as stored, which a Windows Git checkout might not. The archive is about 1.7 MB for the whole repository, so a download of many bots needs one archive, not one per bot.

## What changes

- A typed reading of the catalog: `schemaVersion`, generation time, commit and bot entries with name, version, platform, path, hash, owner, authors, added date, status and team members. Unknown fields are ignored, a missing team-member list means none, and an unsupported schema version is refused with a clear message. Only `active` entries are offered as current; the other states are kept as history.
- Fetching a bot's source from a catalog commit: the library asks for the repository's zip archive at that commit, extracts only the chosen bots' folders to a temporary place, computes each tree hash with the catalog's rule, and moves a bot into the target folder only when it matches. A mismatch leaves nothing in the target and is reported with the bot's name and version. Entries that would escape the target folder, such as paths with `..` or absolute paths, are refused.
- The network sits behind a small interface so tests use fixtures and never touch the internet. The real implementation uses Java's built-in HTTP client against the public GitHub addresses. Reads need no credential.
- Acceptance criteria for the above, written before the code, in the Rumble client capability, with one test that checks the library's hash against a vector produced by the bot repository's own algorithm, including a source file with Windows line endings.
- The caller supplies the catalog commit to read. Finding the right commit from the result repository's pointer is a later slice.
- The participation design says which module owns what for this part, on digest.

## Challenge

The assumption most likely to undermine this slice is that the archive at a commit always holds exactly the files the hash was computed over. Today it does for all 16 bots, but Git can leave files out of an archive when the bot repository marks them `export-ignore`, and a future change to that repository could do so without anyone noticing. The cheapest protection is already part of the design: the library refuses a source whose hash differs, so such a change would show up as a refused bot, never as unreviewed code. The proposal also adds a test that runs the library against a small fixture archive with a file that an archive would normally drop, to prove the failure is reported and nothing is placed.

A second way it could fail while every test passes: fetching a whole 1.7 MB archive to extract two bots is wasteful, and it grows with the catalog. If the bot repository grows to hundreds of megabytes the single archive approach stops being acceptable. The slice keeps the fetch behind the interface so a per-bot method can replace it without touching callers, and records the size in the open questions so the maintainer can decide when it matters.

## Not in this change

No sign-in, no registration, no queue, no download screen and no menu. No finding of the pinned commit from the result repository, no cache of previous downloads and no reading of the bot's license or description, which are later slices. No change to the live Rumble repositories and no new decision record: the hash rule and catalog format are the existing contract.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new library code stays unpublished and shows nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
