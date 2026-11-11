---
id: CH-096
type: change
status: open
links: [P-007, CAP-016, G-004]
title: Sending a bot asks the author to sync their copy when it lacks a change to the Rumble's workflows, and every refusal says which step it was
---

# Sending a bot asks the author to sync their copy when it lacks a change to the Rumble's workflows, and every refusal says which step it was

The maintainer sent RumbleOnboardingScout 1.0.1 to the test environment from the desktop program. The fourth step found the copy of the bot repository and the sign-in App ready, the fifth found the version new, and sending then failed with "GitHub did not allow this. Check that the Rumble sign-in App is installed on your copy of the bot repository." The App was installed on the copy, with write access to code and pull requests, and the same account had sent through the same App on 5 and 7 November.

The author of this change repeated the send's GitHub calls with the saved sign-in of the test environment, with nothing sent: every read, a blob, a tree and a commit in the copy, and a pull request against a branch that does not exist (refused as invalid, not as forbidden) all worked. Creating the branch in the copy was refused with "Resource not accessible by integration". The program builds the commit on the Rumble's newest main branch, and that branch had one commit the copy did not have, which changed `.github/workflows/validate.yml`. A branch that brings a changed workflow file into a repository needs GitHub's Workflows permission, which the sign-in App does not hold, on purpose. On 7 November the copy was up to date, so it worked.

This change is part of the plan item for bot creation, submission and updates.

## Why

Every participant whose copy was made or last synced before the Rumble changed one of its workflows cannot send at all, and the message points at the App installation, which is fine, so nobody can find the cause. The maintainer said that this must under no circumstances hurt the participants, or nobody will use it.

## What changes

- **The fourth step also checks the copy.** After finding the copy and the App, the program compares the copy's main branch with the Rumble's. When the Rumble has changed a file under `.github/workflows/` that the copy lacks, the step says so in plain words, with a button that opens the copy on GitHub, where the author presses Sync fork. As with making the copy and installing the App, the window then finds the synced copy by itself and goes on. This happens only after the Rumble changes its own workflows, which is rare; merged bots never touch workflows, so a copy that only lacks other bots goes on at once, as today.
- **The send keeps building on the Rumble's newest main branch.** Building on the copy's own main branch was weighed and rejected: an update sent from a copy that lacks the author's own merged earlier version would add the same files with other content on both sides, and the pull request could not be merged.
- **No new right for the App.** Giving the App the Workflows permission would let the program sync the copy by itself, but every participant would have to approve an App that may change GitHub Actions in their copy, which reads as alarming and costs trust.
- **Every refusal says which step it was.** When GitHub refuses a step, the message names it (reading the Rumble's repository, writing the files to the copy, making the branch, opening the pull request) with GitHub's own message as the detail, instead of pointing at the App installation. A refused branch, should the copy still lack a workflow change, says to sync the copy.
- **The command line** gives the same setup answer, "copy-behind", with the copy's address.
- **Texts** are resources in every language file, translated.

## Challenge the commitment

The riskiest assumption is that the comparison finds the case before the send. GitHub's comparison lists at most 300 changed files; when it does not answer or lists that many, the step goes on as today, and the refused branch then gives the sync message, so the author is never left with a misleading message. The credible alternative is the Workflows permission, which removes even the rare click but asks every participant to trust the App with their workflows.

## Out of scope

- Building the commit on the copy's own branch, rejected above.
- The Workflows permission for the App.
- The English detail lines of the bot project checks in the desktop program, which are a change of their own.

## Acceptance criteria

- **The fourth step asks for a synced copy only when it lacks a change to the Rumble's workflows, and a refused step is named.** With a fake GitHub: a copy that lacks a workflow change gives its own state with a button that opens only the copy on GitHub, and the wizard looks again by itself and goes on once the copy is synced; a copy that lacks only other changes, or that the comparison cannot judge, goes on at once; a branch refused by GitHub gives the sync message; any other refusal names its step and keeps GitHub's message; the command line answers "copy-behind"; and every text is in every language file. (Unit)
- **The maintainer sends from a copy that lacks a workflow change.** In the desktop program on the test environment, with the maintainer's copy one workflow change behind, the fourth step asks to sync the copy; after Sync fork on GitHub the window goes on by itself, and the bot is sent and its pull request followed. (Human)

## Identity allocation

The change and its two criteria were allocated with the repository's coordinated allocation.

## Decision

The maintainer chose the first of two ways on 11 November 2026: find the case before sending and ask for one click, rather than giving the App the Workflows permission. The maintainer asked the author to go on and fix it without a further stop for review ("ja, fortsæt endelig og få det fikset").
