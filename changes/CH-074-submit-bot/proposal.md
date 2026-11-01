---
id: CH-074
type: change
status: open
links: [P-007, M-029, M-031, ARCH-027, ARCH-028, AN-023, CAP-014, UC-001]
title: Send a staged bot to the Rumble as a pull request from the signed-in account's own copy
---

# Send a staged bot to the Rumble as a pull request from the signed-in account's own copy

This is the ninth slice of the Rumble participation plan's fourth milestone (the one that delivers the shared modules and the test command). Login, registration and staging now exist in the shared libraries. This change adds what turns a staged bot into a submission: check that the signed-in account has its own copy of the bot repository with the Rumble App installed on it, put the staged files on a branch in that copy as one commit, and open a pull request to the Rumble's bot repository. It is the step the experiment proved by hand. No screen is added.

## Why

The author's journey ends with the bot being proposed to the Rumble, and the maintainer chose that this happens through the author's own copy and a pull request, because the copy is in the author's account and under their control. The experiment showed it works for an outside account, but only the pieces were done by hand. A program must do the same steps the same way every time, without duplicate pull requests after an interruption, and without silently changing the files, so the repository's validation and moderation see exactly what the author reviewed.

## What changes

- A readiness check for the account in an environment. It finds the account's copy of the bot repository and confirms it really is a copy of the Rumble's bot repository, not a different repository with the same name. It then checks that the Rumble sign-in App is installed on that copy. The answer is typed: ready, no copy yet (with the address of the page that creates it), the App not installed on the copy (with the address of the page that installs it), or not the Rumble's repository. GitHub does not let a program create the copy, so the screen sends the person to those pages and checks again; the check only reads.
- A send step that takes a staged submission. It creates a branch in the account's copy from the current head of the Rumble bot repository, builds one commit that puts the staged files under `bots/<language>/<Name>/` with the launcher script as executable (the repository requires mode `100755`) and replaces the whole folder if this is an update, so files the author removed are removed, and opens a pull request from that branch to the Rumble's bot repository. The commit is built through GitHub's low-level Git interface rather than the simple file interface, because only the low-level one can set the executable mode and put many files in one commit.
- The author's certification is required: the send step refuses unless the caller confirms that the author has agreed to publish the source under its declared license, which the wizard asks for on the review screen. The pull request text states this honestly, together with the facts of the submission (name, version, folder, license, number of files, content hash, conversions made), what the program checked and what it did not. It does not tick the repository's own checklist boxes, because the program has not run the repository's smoke check on the author's computer. It does not list the files left out of the copy, because those are names from the author's private folder and the pull request is public; that list belongs in the wizard's review. Moderation does not read this text, so it is written for people.
- The bot's name and version come from the author's file and end up in a public text and a branch name, so they must hold only letters, digits, dots, hyphens and underscores, and the send step refuses otherwise. In the text they are written as code, so they cannot become a mention or a link.
- One open pull request per bot per account. If the account has an open pull request for this bot, on a branch starting with `submit/<Name>-`, and it holds the same files, it is returned unchanged. If its files differ, including a new version, one new commit goes on top of that branch and the title and text are updated, so moderation runs again by itself and the conversation stays in one place; this is what the bot repository already asks of an author who fixes a problem. With no open pull request the branch is `submit/<Name>-<version>`. If that branch holds exactly the staged files, it is an interrupted attempt and the pull request is opened from it; if the name is otherwise taken, by a closed or merged pull request, `-2`, `-3` and so on are used, so a rejected pull request stays as history. Nothing is ever force-pushed or deleted.
- The title is `Add <Name> <version>`, or `Update <Name> to <version>` when the bot's folder already exists in the Rumble's repository.
- Typed results: submitted (with the pull request address and whether an earlier attempt was resumed), setup needed (the readiness answers above), refused (for example the author's certification is missing), or failed with a reason in plain words, including the token not accepted (sign in again) and being offline.
- The public identifier of each environment's sign-in App page is added to the shared environments data file, so the install address can be built.
- Acceptance criteria written before the code, in the bot catalog capability, with positive and negative tests; the participation design's module table is updated on digest.

## Challenge

The assumption most likely to undermine this slice is that the files in the pull request are exactly the staged files. A wrong mode, a changed line ending or a file dropped by the interface would make the repository record a different content hash than the one the author reviewed, and nothing would say so until the catalog is regenerated. The change therefore compares, before it opens the pull request, the identifier GitHub gives each committed file with the identifier computed locally from the staged file, and refuses to open the pull request if any differs. The live check against the test environment must also show that the resulting folder hashes to the staged hash.

A second failure the tests could miss is a large bot: one request per file means a bot with a hundred source files makes more than a hundred requests. That is far inside the signed-in rate limit, but a mid-way interruption leaves loose objects and no branch, which is harmless and is simply sent again.

A third is not proven at all. The experiment made its branch from the copy's own main branch, while this change makes it from the Rumble's current head, so an update builds on the latest published version. GitHub may refuse that when the copy is behind, especially if workflow files changed, because the App has no right to those. A refusal gives the author a plain message to update their copy on GitHub. Whether it happens is only learned from the manual check with a real account.

## Not in this change

No screen. No creation of the copy, which GitHub does not allow a program to do. No checks against the catalog (whether the name belongs to someone else, whether the version is higher than the active one), no following of the pull request's progress through checks, review and publication, and no withdrawal. No change to any Rumble repository. A live proof with a real outside account needs a person to approve a sign-in code and would open a real pull request in the test repository, so it is left as a manual check at the end of the participation work and is not part of this change.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The new library code stays unpublished and shows nothing to users, as decided on 2026-10-31. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any code or document is changed.
