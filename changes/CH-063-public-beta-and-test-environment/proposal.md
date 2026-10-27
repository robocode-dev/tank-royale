---
id: CH-063
type: change
status: open
links: [P-007, M-027, ADR-052, PDR-019]
title: Establish the public Beta and an isolated test Rumble
---

# Establish the public Beta and an isolated test Rumble

This change delivers the second milestone of the Rumble participation plan: the existing Rumble is presented honestly as a public Beta, and a separate test Rumble exists where experiments, resets and automated tests can run without any chance of touching public data.

## Why

Every later milestone (outside-account sign-in, the desktop interface, unattended sessions, withdrawal) needs a safe place to be tried with real GitHub accounts, and the plan forbids using the live Rumble as disposable test infrastructure. The public Rumble also needs a short, accurate invitation and a feedback route, so early participants know it is a Beta and where to report problems. The maintainer wants as little manual work as possible.

## What changes

- Two new repositories in the `robocode-dev` organization, named after the existing ones with a `-test` ending: `rumble-bots-test` (the test bot catalog) and `rumble-data-test` (test results, rankings and the test dashboard, published at `robocode-dev.github.io/rumble-data-test/`). There is no `rumble-client-test`: the client is shared code and only chooses which environment it talks to. The plan's design already names these two repositories; the names are checked first and a conflict is the maintainer's decision.
- Two new GitHub Apps with the same names as the existing ones plus `-test` (`tank-royale-rumble-moderator-test` and `tank-royale-rumble-publisher-test`), with exactly the permissions of the existing Apps, each installed only on the test repositories. Separate Apps make the isolation a real technical boundary: nothing the test Apps hold can write to a public repository.
- The workflows in the test repositories come from the same sources as the public ones, with the environment chosen by inputs, so the two cannot drift apart.
- A reset tool for the test environment only. It must refuse any target that is not a test repository, refuse results or submissions that name the other environment, and be covered by checks that show both that it works on the test repositories and that it is refused on the public ones.
- A short Beta notice and feedback route on the public bot repository, the dashboard and the participant home page, kept accurate to what works today.

## How the manual steps are minimized

GitHub cannot copy an App, but it can create one from a prepared settings file in one click by an organization owner. I prepare those files from the existing Apps' public settings, so the maintainer only confirms creation and installation in the browser. Everything else (repositories, workflows, settings, secrets, the reset tool and the Beta text) is done from the command line.

## Not in this change

No outside-account experiment, no desktop interface, no unattended sessions, no withdrawal and no changes to scoring or the live results. Live public pages are only changed after the maintainer has seen the exact text, and nothing in the public repositories is reset or removed.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. Changes to other repositories (new repositories, Apps, workflow changes and Beta text) are separate outward-facing actions and each waits for the maintainer's go-ahead after the exact action is shown. The work serves the simple-participation goal; the repository states no vision.
