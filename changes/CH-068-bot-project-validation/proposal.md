---
id: CH-068
type: change
status: open
links: [P-007, M-029, M-031, ARCH-027, ARCH-028, CAP-014, UC-001]
title: Start the bot project library by inspecting and validating a bot folder
---

# Start the bot project library by inspecting and validating a bot folder

This is the fourth slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). It creates the new library `lib:bot-project` and gives it its first job: look at a bot folder on disk, say what bot it is, and tell the author exactly what must change before it can be submitted, using the same rules the bot repository applies when it moderates a submission. Nothing is sent anywhere and nothing is shown to a user yet; the library stays unpublished and the Rumble menu stays hidden.

## Why

The plan's goal for authors is that they submit and update a bot without Git commands, without hand-editing JSON and without discovering problems only after a pull request fails. Today an author learns about a missing license, a wrong platform, a missing launcher or a forbidden dependency only when the bot repository's checks reject the pull request, possibly hours later. The same rules can run on the author's own computer in seconds, before anything is published, and the submission wizard's check step in the mockups needs exactly that answer: a list of failing checks, each with the change that fixes it.

The validator that decides acceptance is a Python script in the bot repository. It stays the authority, because it also sees the whole catalog. The library gives the author the same answer for everything that depends on the bot's own folder alone, so a bot that passes locally is not rejected for a reason the author could have fixed on their own computer.

## What changes

- A new Gradle module `lib:bot-project`, written in Kotlin, built and tested with the rest of the project, unpublished, with no dependency on the desktop program, the battle runner or any GitHub code. It depends only on the common library and standard tools, as the dependency rules say.
- Inspection of a folder: find the bot configuration file named after the folder, read its name, version, authors, license, description, platform and team members, decide which of the four official languages the project uses (Java, C#, Python, TypeScript), and tell team folders (a configuration file with exactly two member identities and nothing else) from individual bots.
- Validation with the bot repository's per-folder rules, each failure carrying a stable code, the file it concerns and the change that fixes it in plain words: required fields present, the folder name equal to the configuration name, authors a non-empty list of names, the license one of the allowed ones, the platform matching the language folder, team folders shaped correctly, both launcher scripts present, at least one source file of the right language that refers to the official Bot API, no generated build or dependency folders, no binary or archive files, none of the constructs the repository sends to a moderator, and for TypeScript the manifest and lockfile rules for the official API and its one declared runtime peer. Findings come back as a list so an author sees every problem at once, not only the first.
- A source of truth for the rules that is easy to compare with the bot repository: the allowed licenses, platform table, forbidden file endings, forbidden constructs and TypeScript peer list live in one data file in the library, next to the code that applies them.
- Acceptance criteria written before the code, in the bot catalog capability, which owns the validation policy, with positive and negative tests for each group of rules, using small folders created by the tests.
- An opt-in check that runs the library over every active bot of the public Rumble and expects each to pass, as the catalog check does for hashes. A bot the repository accepts must never be reported as failing.
- The participation design's module table says what the library holds, on digest.

## Challenge

The assumption most likely to undermine this slice is that rules copied into Kotlin will stay the same as the Python script they were copied from. If the bot repository tightens a rule and the library does not follow, authors will pass locally and fail on the pull request, which is the problem this slice is meant to remove, and nothing will say so. The cheapest protection is built in: the opt-in check against the public bots catches rules the library is stricter about; for rules the repository adds, a list of the repository's own negative test cases, copied as fixtures with the commit they came from, must each fail in the library with the same reason. The proposal accepts that the library tells an author "these checks pass" and never "this will be accepted", because the repository also checks names, ownership and versions against the catalog.

A second way it could fail while the tests pass: the executable bit on the Unix launcher. The repository requires it, but a Windows author cannot set it, and a check that fails on Windows would block every Windows author for something they cannot fix by hand. The proposal does not check the bit locally and leaves setting it to the later staging step, which records the launcher as executable when it prepares the submission. See the open questions.

## Not in this change

No starters, no project staging, no copying of an author's files, no smoke run of the bot and no changes to the author's folder: this slice only reads. No checks that need the catalog (name collisions, ownership, version order). No GitHub, sign-in, fork or pull request work, no menu and no screen. No change to the bot repository. No new decision record: the rules are the repository's existing policy.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new library stays unpublished and shows nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
