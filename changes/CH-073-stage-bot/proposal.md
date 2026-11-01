---
id: CH-073
type: change
status: open
links: [P-007, M-029, M-031, ARCH-027, ARCH-028, CAP-014, UC-001]
title: Stage a bot project as a separate, validated copy that is ready to submit
---

# Stage a bot project as a separate, validated copy that is ready to submit

This is the eighth slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). `lib:bot-project` can already inspect a bot folder and validate it. This change lets it prepare the folder for submission: copy it, without touching the original, into a separate staging folder laid out exactly as the bot repository wants it, check the copy against the same rules, and return a description of exactly what would be published. Bot submission through a fork and a pull request is the next slice and needs this. No screen is added.

## Why

The submission wizard promises that the author's project is never changed and that the author sees the exact files and license before anything is published. Today there is nothing that decides which files go, in what shape, and what the result is. The author's project folder is rarely clean: a C# project has `bin` and `obj` folders from building, a Python one has `__pycache__`, a TypeScript one has `node_modules`, and most have an editor folder or a Git folder. A Windows author's launcher script often has Windows line endings, which break a Unix shell script, and Windows cannot set the executable bit that the bot repository requires. Those are the things that make a first submission fail for reasons the author cannot easily see. Staging is where they are handled once, in a way that can be checked and shown.

## What changes

- A staging step in `lib:bot-project` that takes a bot or team folder and a staging folder chosen by the caller and returns either a staged submission or the list of problems that stopped it. The original folder is only read.
- What the staged copy holds: the files of the project laid out as `bots/<language>/<Name>/` (the shape of the bot repository, so the next step can add the folder as it is). Version-control folders, editor folders and caches (`.git`, `.svn`, `.hg`, `.idea`, `.vs`, `.vscode`, `__pycache__`) and the generated dependency and build folders that the bot repository refuses (`node_modules`, `.venv`, `bin`, `obj`) are left out and listed as not included, with the reason, not treated as problems. Symbolic links are left out and listed too, so nothing outside the project can be copied by a link. Binary and archive files in the source stay problems, as the repository refuses them.
- Fixes made only in the copy, and listed so the author sees them: the Unix launcher script gets Unix line endings, and it is recorded as executable (set on the copy where the system can, and recorded in the description either way, so the later step commits it as executable). No other file is changed.
- The copy is checked with the same validation as before, so a staged submission is a copy that passes. If it does not, nothing usable is returned and the problems are given in the same plain-words form.
- The description of a staged submission: the bot's name, version, language, license, authors, the staging folder, every file with its size and whether it is executable, the files not included and why, the fixes made, and the content hash of the copy, which is the hash the bot repository will record for it after a merge. Staging the same project twice gives the same files and the same hash.
- Safety rules for the folders: the staging folder must not lie inside the project or contain it, and an existing staging folder is emptied only if it is one this library made earlier, recognised by a marker file; otherwise staging refuses.
- The content hash moves into `lib:bot-project`, which is where the rule belongs, and `lib:rumble` uses it from there, so there is one implementation of it. Its tests move with it. This also matches the module dependency diagram, in which the Rumble library uses the bot project library.
- Acceptance criteria written before the code, in the bot catalog capability, with positive and negative tests, and the module design text updated on digest.

## Challenge

The assumption most likely to undermine this slice is that the staged copy hashes to the same value as the bot repository will compute after the merge. The experiment on fetching bots showed the repository's hash rule is stable, and the staged tree has the same layout as the repository, but the one thing that can differ is a file the repository's tooling changes on the way in. The cheapest check is built in: the live check that already fetches every active public bot is extended to stage each fetched bot and expect the staged hash to equal the catalog's recorded hash and the staged copy to pass validation. A bot that the repository accepts must stage unchanged.

A second failure the tests could miss is changing the line endings of a launcher that is not meant to be changed, such as a script that deliberately holds binary data. The change limits the conversion to the two launcher scripts named after the bot and lists every conversion, so it can be seen and challenged.

## Not in this change

No starters or project generation. No change to the author's project. No catalog checks (name clashes, ownership, version order) and no GitHub, fork, branch, commit or pull request work. No screen. No smoke run of the bot. No change to the bot repository.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new library code stays unpublished and shows nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
