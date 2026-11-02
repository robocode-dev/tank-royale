---
id: CH-075
type: change
status: open
links: [P-007, M-029, M-031, ARCH-027, ARCH-028, CAP-014, UC-001]
title: Check a staged bot against the Rumble's rules before sending it, and follow its pull request afterwards
---

# Check a staged bot against the Rumble's rules before sending it, and follow its pull request afterwards

This is the tenth slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). The previous slice can send a staged bot as a pull request. Two things are still missing around that step: telling the author before sending that the Rumble will refuse the bot, and telling them afterwards what became of the pull request. Both are read-only on GitHub. No screen is added.

## Why

Today an author learns that a name belongs to someone else, that a bot with the same version has different source, or that their five slots are used only after the repository's checks fail on a public pull request. That is a poor first experience and leaves a failed pull request in public. After sending, the author has only a link; whether the bot is waiting for checks, was sent back for a fix, waits for a human moderator, or is already in the catalog is spread over statuses, reviews and a generated file. The wizard needs one answer in plain words for each of these.

## What changes

- A pre-check that takes a staged submission, the account's login and the Rumble's current rules, and returns the problems the Rumble's own validator would raise, in plain words with the change that fixes each: the account is banned, the bot is disqualified, the bot belongs to another owner, the same version is already published with different source (raise the version), the identical bot version is already active (nothing to send), the name is easily confused with another bot's (the validator's own lookalike rule), and the account has no free slot among its five. It reads three files of the bot repository at its current head: the catalog, the owners and the bans. A bot whose name is new is not a problem; an update by its owner is not either.
- The pre-check only advises. The repository's validator stays the authority, and the send step does not call the pre-check, so a difference between them can never block a submission the Rumble would accept. The wizard shows the findings and lets the person decide.
- A progress reader for the account's pull request for a bot. It returns one typed state: waiting for the checks, being checked, sent back with the moderator's reason to fix, waiting for a human moderator, accepted and being merged, merged and waiting for the catalog, in the catalog (with the published version), or closed without merging. It reads the pull request, its commit status from the moderation check, its reviews, and the catalog; it does not comment, approve or merge.
- Both readers use the existing read-only GitHub interface and the existing catalog reading. No new permission is asked of the sign-in App.
- Acceptance criteria written before the code, in the bot catalog capability, with positive and negative tests; the participation design's module table and the plan's progress note are updated on digest.

## Challenge

The assumption most likely to be wrong is that the pre-check agrees with the repository. It copies rules from a Python script in another repository, including the lookalike table, and the script can change. A copy that is stricter than the original blocks authors for nothing; a copy that is looser only means the old experience. Three things limit the damage. The pre-check is advice and never a gate. It carries the same test cases as the validator's own checks where they exist. And a live check, skipped by default, runs the pre-check for every active public bot: as its own owner it must find nothing to send, and as another account it must say the bot belongs to someone else. A drift in the table would show as a lookalike finding on a published bot.

The progress states depend on texts and statuses that the moderation workflow writes today. If the workflow changes its wording, the reader falls back to "being checked" rather than guessing, and says so. This makes the state a best reading, not a promise. The fixtures record the exact statuses the live test environment produced, taken from a real pull request there.

## Not in this change

No screen. No comment on, approval of or merge of a pull request. No withdrawal. No change to any Rumble repository or its workflows. No live proof with a real outside account; that remains the manual check at the end of the participation work.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new library code stays unpublished and shows nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
