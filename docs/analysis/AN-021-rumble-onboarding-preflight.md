---
id: AN-021
type: analysis
status: active
links: [P-006, M-025, AN-017, AN-019, AN-020]
title: Rumble public onboarding preflight
---

# Rumble public onboarding preflight

## Consumer and conclusion

This finding supports M-025 of [the Rumble announcement-readiness plan](../plans/P-006-rumble-announcement-readiness.md), which requires a non-maintainer to follow the published bot-author and battle-contributor journeys through a catalog entry, accepted result receipt and visible ranking. The published paths are now available: the [bot-author guide](https://robocode.dev/rumble/bot-author-guide.html) describes source submission, and the [battle-contributor guide](https://robocode.dev/rumble/client-guide.html) describes registration, ranked execution and token-free result-form submission. The current client release is [v0.3.0](https://github.com/robocode-dev/rumble-client/releases/tag/v0.3.0), built from commit `97530e05ec8d895b5067672e190f59053b9fb284`; the public `latest` image resolves to Linux/amd64 digest `sha256:32ab3259f0ce69f319174b54ba36f4d85949c1a30ea4e65a94010b9c931acf2d`.

The final milestone remains unproven. The non-member test account `robocode-rumble-test` has an active `RumbleTestScout` 1.0.2 catalog entry, but the current public 1v1 projection records zero battles for that version. Existing evidence records an earlier accepted result for the same account, and the account has since submitted another bot version; neither record shows a fresh end-to-end run from a clean environment using the currently published client. No new result was posted during this preflight.

## Evidence boundary

Public pages and JSON endpoints were fetched directly on 2026-10-21. The client guide's live HTML describes pasting the prepared batch into a GitHub form, says the client needs no token, and explains how to collect the receipt. The bot-author guide links the runnable starters and describes the pull-request and moderation path. These observations supersede older cached page text that still showed the former token-based flow.

The [v0.3.0 release](https://github.com/robocode-dev/rumble-client/releases/tag/v0.3.0) points to commit `97530e05ec8d895b5067672e190f59053b9fb284`. `docker manifest inspect ghcr.io/robocode-dev/rumble-client:latest --verbose` returned digest `sha256:32ab3259f0ce69f319174b54ba36f4d85949c1a30ea4e65a94010b9c931acf2d` for `linux/amd64`. The Docker CLI is version 29.7.2. At the time of this preflight, its Linux engine named pipe was unavailable, so the image was not pulled or run.

The public [bot catalog](https://github.com/robocode-dev/rumble-bots/blob/main/bots/index.json), at repository commit `0719f0511c9edbf1e35b2d125d098bffc4cd7826`, lists `RumbleTestScout` 1.0.2 as active under `robocode-rumble-test`, with source hash `sha256:b02fe3fd3cbe64756cb54ba3ea8995b4a248777a41c4fc3d445a436a210d0ec8`. Its version-update pull request [#22](https://github.com/robocode-dev/rumble-bots/pull/22) was authored by that account and merged on 2026-10-21. The public [1v1 projection](https://robocode-dev.github.io/rumble-data/data/leaderboard/1v1.json), projection ID `9c4db43dabd971a6ed980ff48964de2515264c64cab518a8b2966618bbb4036d`, contains the active version with zero battles. The public client totals still list one battle for registered client `robocode-rumble-test-01`; [AN-017](AN-017-rumble-outside-account-result-evidence.md) records that earlier battle's acceptance and publication.

## Documented prerequisites

The bot-author guide names a GitHub account, Git, Python 3.11 or newer for validation, and the runtimes used by the chosen bot. The battle-contributor guide names a GitHub account, Git and Docker or Podman for the published image. Ranked execution also requires a client registration pull request merged into `rumble-data`; the container includes the bot runtimes and does not need a host language toolchain or a Tank Royale checkout. These are published requirements, not evidence that an outside participant completed them in a clean environment.

## What remains to verify

The previous outside-account result evidence ([AN-017](AN-017-rumble-outside-account-result-evidence.md)) used a prepared client state, and the live bot submissions ([AN-019](AN-019-live-rumble-bot-submission.md)) do not record a fresh participant environment or a battle against the currently active 1.0.2 version. The walkthrough verification ([AN-020](AN-020-rumble-contributor-walkthrough-verification.md)) also used prepared local state and did not post a result. Together these prove useful parts of the journeys, but not M-025's complete outside-user demonstration.

To close M-025, a non-maintainer must follow the published bot-author and contributor instructions in a recorded environment. Record the host OS and version, container engine and image digest, source and catalog revisions, account prerequisites, registration and bot pull requests, every human intervention, battle ID, result issue and acceptance receipt, and the resulting public ranking row. Label each run clean or prepared and name every prerequisite. If an undocumented intervention is needed, correct the public guide and repeat the affected journey. Do not treat the existing test account's historical battle as a result for the active 1.0.2 catalog entry.
