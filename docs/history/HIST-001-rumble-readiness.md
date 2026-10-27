---
id: HIST-001
type: historical-record
status: active
links: [P-007, PDR-019]
supersedes: [AN-021, AN-022]
title: Superseded Rumble readiness source archive
---

# Superseded Rumble readiness source archive

This active archive preserves historical source; none of the copied records is active guidance. The old plan is superseded by the [participation campaign](../plans/P-007-rumble-participation.md), and its pre-announcement policy by the [public-Beta decision](../decisions/PDR-019-rumble-public-beta-campaign.md). The two old analyses are retired as actionable artifacts. Their requests to close the old walkthrough are quoted history only; that walkthrough was not completed.

The complete Markdown sources below are copied from accepted Git revision `e0baa0337a83aee98550ab39d72b3f517619461b`. They can be read offline; their original headers and links are kept verbatim as evidence inside code fences. The archive does not reactivate their identities, historical active statuses, or obsolete instructions. Current work follows the successor plan.

## P-006 — superseded historical record

Original source: `docs/plans/P-006-rumble-announcement-readiness.md`. Current direction: P-007.

````markdown
---
id: P-006
type: plan
status: active
links: [G-003, AN-014, PDR-015, AN-015, AN-016, AN-017, AN-018, AN-019, AN-020, AN-021, AN-022]
title: Rumble announcement readiness
---

# Rumble announcement readiness

This campaign serves Robocoders who want to submit bots and contribute ranked battles without maintainer privileges. The outside-contributor access, result publication, and bot-submission milestones are complete. The battle-contributor walkthrough has a complete, exercised guide; the published image and acceptance evidence are recorded below. The [pre-announcement audit](../analysis/AN-014-rumble-announcement-readiness.md) supplies the initial findings; it is historical evidence, not proof that every reported risk still exists when implementation begins. Recheck the relevant revisions before each milestone.

## Scope and sequence

Tank Royale owns this cross-repository campaign and its public user guides. Implementing changes may also belong in [rumble-client](https://github.com/robocode-dev/rumble-client), [rumble-bots](https://github.com/robocode-dev/rumble-bots), [rumble-data](https://github.com/robocode-dev/rumble-data), [the Book of Robocode](https://github.com/robocode-dev/book-of-robocode), and [the organization profile](https://github.com/robocode-dev/.github). Each milestone records full links to its implementing PRs, pinned revisions and verification evidence; external corpus references use the `clue:` identity form. Completion depends on merged changes and published user-facing content, not merely prepared PRs.

| ID | Milestone | Exit criterion | Status | Evidence |
|---|---|---|---|---|
| M-020 | Establish a supported outside-contributor path | Reproduce or disprove the audit's token and label risks with a non-member account lacking repository push access. Record exact prerequisites and the supported authentication/intake design against the existing credential boundary; any required contract or policy change has a reviewed decision before implementation. A privileged-account success does not satisfy this exit. | done | Decision: outside contributors submit through the web form, recorded and accepted as [ADR-049](../decisions/ADR-049-rumble-browser-form-submission.md); the API transport stays optional for accounts with push access and a GitHub App login is the recorded later option. Evidence: Both audit risks reproduced on 2026-10-18 with the non-member account `robocode-rumble-test`, which has no push access and no maintainer help beyond merging its registration ([rumble-data PR 20](https://github.com/robocode-dev/rumble-data/pull/20), merged 2026-10-18). F1 confirmed: the fine-grained token page offers only the account itself as resource owner and states that such a token can change only resources owned by that owner, so a token limited to `robocode-dev/rumble-data` cannot be made; a classic token with only the `public_repo` scope can be created. F2 confirmed: an issue created through the API with the `result-submission` label ([rumble-data issue 21](https://github.com/robocode-dev/rumble-data/issues/21)) arrived without the label and the ingestion run was skipped, so the issue stayed unprocessed with no receipt. New finding: the same deliberately invalid batch submitted through the `Result submission` web form ([rumble-data issue 22](https://github.com/robocode-dev/rumble-data/issues/22)) received the label from the issue template, ingestion ran, posted a rejection receipt and closed the issue, with no token and no maintainer action. A prefilled form link carries only a few results: an estimated five one-versus-one results (about 6,100 characters) opened with the batch filled in, and ten (about 11,800 characters) was refused with the message that the request URL is too long, so a full 60-result batch cannot travel in a link and a short link with the contributor pasting the batch is the candidate (one extra manual step per batch). A pasted batch of 60 one-versus-one results (about 45,500 characters, deliberately invalid) created through the web form by the non-member account was labelled by the template, ingested, answered with one rejection line per result and closed ([rumble-data issue 24](https://github.com/robocode-dev/rumble-data/issues/24)), so a short link with the contributor pasting the batch works at that size. An earlier attempt (issue 23) failed because the batch was copied from a rendered Markdown view, which dropped the code fences, and was also created by a maintainer account, so it is not evidence; a client should hand the batch over as plain text, never as a Markdown file. Left for the next milestone: a valid batch through the form, whether batches near the issue body limit need splitting by size, receipt correlation for an outside account without a credential, and revising the client criteria and the credential passage of ADR-043. Observed on the way, for the later guide and entry-point milestones: the default pull request template of `rumble-data` is a maintainer checklist and the registration template is not offered, first-time contributors' checks wait for moderator approval, and moderators get no notification of registrations. Account prerequisites: a free personal GitHub account, no organization membership, and a registration pull request merged by a moderator. |
| M-021 | Accept and publish outside-contributor results | Using the path established above, a registered outside contributor runs and submits a ranked battle, receives a durable acceptance receipt and observes its eligible result in published rankings without maintainer labeling or repair. Focused failure/retry checks cover unregistered identities, invalid submissions and interrupted submission without duplicate facts or lost pending results. Implementing PRs and result evidence are linked. | done | Completed 2026-10-19. Contract alignment merged in [Tank Royale PR 283](https://github.com/robocode-dev/tank-royale/pull/283), and evidence merged in [Tank Royale PR 284](https://github.com/robocode-dev/tank-royale/pull/284). Corrections for the [prepared client implementation review](../analysis/AN-015-rumble-browser-submission-review.md) merged in [rumble-client PR 20](https://github.com/robocode-dev/rumble-client/pull/20), commit `80af3517affb912c45073ba513ad016f586e6e47`; [regression and container-battle evidence](../analysis/AN-016-rumble-browser-submission-verification.md) records 66 passing tests. The [outside-account acceptance and recovery evidence](../analysis/AN-017-rumble-outside-account-result-evidence.md) links issue 25, its acceptance receipt, canonical fact and published ranking data, plus journal recovery after transient HTTP 500 responses and focused failure/retry checks. |
| M-022 | Make bot submission reproducible in every official language | Java, C#, Python and TypeScript have linked copy-and-rename templates validated by the actual submission workflow, with source-only dependencies, launchers, metadata, smoke behavior and executable-bit instructions. A JSON-only TwinDuel submission is covered. At least one first bot and a version update proceed from the published instructions through merged catalog entries; language-template checks are recorded separately from that live submission. | done | Completed 2026-10-19. Starter templates, validator and submission workflow merged in [rumble-bots PR 11](https://github.com/robocode-dev/rumble-bots/pull/11); the author guide and dependency clarification merged in [Tank Royale PR 286](https://github.com/robocode-dev/tank-royale/pull/286). Four-language smoke and battle checks remain separately recorded in the [template-verification evidence](../analysis/AN-018-rumble-starter-validation.md). Live evidence in [AN-019](../analysis/AN-019-live-rumble-bot-submission.md): the registered outside owner submitted its first bot in [rumble-bots PR 12](https://github.com/robocode-dev/rumble-bots/pull/12), then updated its source and version in [PR 14](https://github.com/robocode-dev/rumble-bots/pull/14); the published [bot index](https://github.com/robocode-dev/rumble-bots/blob/main/bots/index.json) records version 1.0.0 as `superseded` and 1.0.1 as `active` under the same owner with distinct source hashes. The first publisher run failed because its token could not push, and the second failed before GitHub associated the merge with its PR; both were recovered with documented manual dispatches. [PR 13](https://github.com/robocode-dev/rumble-bots/pull/13) fixes fork-submission permissions; [PR 16](https://github.com/robocode-dev/rumble-bots/pull/16) adds a bounded association retry, whose validation passed but whose automatic retry path has not yet been exercised by another live submission. The [official API runtime-dependency decision](../decisions/ADR-050-official-bot-api-runtime-dependencies.md) clarifies the permitted TypeScript `ws` peer. |
| M-023 | Publish a complete battle-contributor walkthrough | The recommended container path gives ordered shell and PowerShell steps from clone and registration to accepted receipt, visible ranking and repeat contribution. Copyable defaults match one-battle behavior; native instructions match released dependencies. Troubleshooting covers authentication, registration, receipts, catalog delay, engine upgrades, state retention and invalid selections. Supported architecture/resource limits and any unverified paths are explicit. Commands are exercised under recorded OS, image and source revisions. | done | Depends on M-021; audit findings F5–F6 and F10–F12. Evidence: [walkthrough guide](../../web/docs/rumble/client-guide.md); accepted receipt and public ranking in [AN-017](../analysis/AN-017-rumble-outside-account-result-evidence.md). Public release [v0.3.0](https://github.com/robocode-dev/rumble-client/releases/tag/v0.3.0), client source [`97530e0`](https://github.com/robocode-dev/rumble-client/commit/97530e05ec8d895b5067672e190f59053b9fb284), pinned Tank Royale source [`9c25770`](https://github.com/robocode-dev/tank-royale/commit/9c25770c02671d1f9d941aad312013562a441427), Battle Runner 1.3.1; both `0.3.0` and `latest` resolve to `linux/amd64` digest `sha256:32ab3259f0ce69f319174b54ba36f4d85949c1a30ea4e65a94010b9c931acf2d`. On Windows 11 Pro 26300, PowerShell 7.6.7, Docker Desktop 4.90.0, Docker Engine 29.7.2, `runtimes`, `validate`, `sync`, one ranked 1v1 battle and token-free `submit` passed. Submit prepared three one-result batches; no issue was posted. The run reused prepared account/config/state; clean onboarding, ARM64, Apple Silicon, Podman, and native Windows/macOS execution remain unverified (see AN-020). Implemented in [Tank Royale PR 288](https://github.com/robocode-dev/tank-royale/pull/288). |
| M-024 | Align public entry points and resolve launch gates | The Book, bot repository, organization profile, main README, tutorial exits and dashboard consistently link submission, battle contribution and rankings. Contradictory availability statements are corrected after confirming their meaning. Any genuine pre-opening validation or moderator-staffing gate is evidenced as met, or revised through an explicit reviewed decision. Deployed pages and relevant links are checked. | done | Bot moderation is implemented in [rumble-bots PR #18](https://github.com/robocode-dev/rumble-bots/pull/18), with its active ruleset and live test recorded in governance by [PR #29](https://github.com/robocode-dev/rumble-bots/pull/29). Book PR [#5](https://github.com/robocode-dev/book-of-robocode/pull/5), Tank Royale docs PR [#289](https://github.com/robocode-dev/tank-royale/pull/289), and Rumble dashboard PR [#26](https://github.com/robocode-dev/rumble-data/pull/26) are merged. The [bot repository](https://github.com/robocode-dev/rumble-bots), Book overview/rankings pages, Tank Royale docs home and all four tutorials, [Rumble overview](https://robocode.dev/rumble), [organization profile](https://github.com/robocode-dev), and [live rankings dashboard](https://robocode-dev.github.io/rumble-data/) were checked live and returned HTTP 200 with the relevant submission, contribution and rankings routes. Tank Royale Pages deployment [39478853029](https://github.com/robocode-dev/tank-royale/actions/runs/39478853029), Book deployment [39478862623](https://github.com/robocode-dev/book-of-robocode/actions/runs/39478862623), and dashboard deployment [39627148826](https://github.com/robocode-dev/rumble-data/actions/runs/39627148826) succeeded. The former moderator-staffing gate is replaced by the bot-first moderation workflow; the active [main ruleset](https://github.com/robocode-dev/rumble-bots/rules/25580362) requires `bot-moderation`, one approval and code-owner review, with one organization-owner account's pull-request-only recovery bypass. |
| M-025 | Demonstrate announcement readiness with an outside Robocoder | A non-maintainer follows the published author and contributor journeys through a catalog entry, accepted result receipt and visible ranking. Evidence distinguishes clean and prepared environments, names every prerequisite and intervention, and records exact versions. Any undocumented necessary intervention is fixed and the affected journey repeated. The maintainer receives the remaining limitations and evidence for the announcement decision. | todo | Public guides and the v0.3.0 image were checked in [AN-021](../analysis/AN-021-rumble-onboarding-preflight.md). A prepared-host practice run in [AN-022](../analysis/AN-022-rumble-v030-practice-run.md) synchronized the current catalog and completed a battle without a client identity or public submission. The active outside test bot still has no ranked battles; the clean outside-user journey remains outstanding. The [moderator team change](https://github.com/robocode-dev/rumble-bots/pull/30) shares human review through the existing GitHub team. |

Authentication/intake was the first work item because documentation cannot repair an inaccessible service path. That milestone established the credential-free browser-form decision. The result-publication milestone verified the path through an accepted outside-account result, journal recovery and published ranking data. The bot-submission milestone verifies official starters and live first-version and update submissions in the catalog. The contributor walkthrough and public entry points are now documented and aligned; the final outside-Robocoder onboarding remains.

## Challenge and stopping conditions

The main assumption is that the intended open participation model can work within the accepted credential and trust boundaries. A credible alternative is an explicitly limited pilot while that conflict is resolved. The cheapest useful test is the outside-account experiment in the first milestone, using authorized test data and recording account access without secrets. If it requires broader credentials, repository privileges, manual labeling, or a new service, stop implementation of that path and bring the actual tradeoff to a reviewed decision. Do not grant participants maintainer access just to make onboarding pass.

A technically passing campaign could still fail if the author needs undocumented runtimes or the contributor cannot distinguish a posted issue from an accepted score. The final milestone follows both complete public journeys rather than treating green CI or a documentation checklist as sufficient evidence. Historical maintainer tests and prepared-machine tests remain useful but are labeled honestly.

## Boundaries

No scoring change, new game format, unattended client-session feature, authentication architecture, or release is approved by this plan. A bounded repeat-command recipe may document current behavior; introducing a session feature needs separate scope. Separate FAQ/onboarding filenames are optional: complete, reachable guidance is the promised outcome. Publishing the announcement remains the maintainer's decision after readiness evidence is available.

The corpus has no vision document. This plan proceeds under the existing learning-and-competition purpose and the maintainer's explicit request. The completed original Rumble and hardening plans remain immutable. Adoption's already-evidenced milestones are not reopened or silently made prerequisites of this campaign.
````

## PDR-015 — superseded historical record

Original source: `docs/decisions/PDR-015-rumble-readiness-next-campaign.md`. Current direction: PDR-019.

````markdown
---
id: PDR-015
type: decision
status: inferred
links: [G-003, P-006, AN-014]
title: Prove public Rumble participation before its announcement
author: agent
accepted-by: []
---

# Prove public Rumble participation before its announcement

## Context

Rumble spans several repositories, and working maintainer-operated battles do not establish that ordinary Robocoders can submit bots and contribute results. Public guidance must describe a path that works for the people invited by the announcement.

## Decision

The [announcement-readiness campaign](../plans/P-006-rumble-announcement-readiness.md) is the next planned work. Establish a supported outside-contributor path first, then complete the author and contributor guidance, align public entry points, and demonstrate both journeys with a non-maintainer before the announcement decision.

The campaign chooses the required outcome and sequence, not a replacement authentication architecture or an announcement date. Decisions that change credential scope, intake behavior, or existing launch gates remain reviewed work within the campaign.
````

## AN-021 — superseded historical record

Original source: `docs/analysis/AN-021-rumble-onboarding-preflight.md`. Current direction: P-007.

````markdown
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
````

## AN-022 — superseded historical record

Original source: `docs/analysis/AN-022-rumble-v030-practice-run.md`. Current direction: P-007.

````markdown
---
id: AN-022
type: analysis
status: active
links: [P-006, M-025, AN-020, AN-021]
title: Rumble v0.3.0 practice-mode container run
---

# Rumble v0.3.0 practice-mode container run

## Consumer and conclusion

This finding adds a local container observation for M-025 of [the Rumble announcement-readiness plan](../plans/P-006-rumble-announcement-readiness.md). The published v0.3.0 image synchronized the public catalog and completed one practice-mode battle from a fresh client checkout and empty state directory. The host was prepared: Docker was already installed, the daemon was started for this run, and the pinned image was already present locally. Practice mode omitted a client identity and did not submit a result, so this is not an outside-user onboarding run and does not meet M-025.

## Evidence boundary

The run was performed on 2026-10-21 with PowerShell 7.6.7 on a Windows host reporting `Windows 10 Pro`, build `26300`, display version `26H2`. Docker client and server were both 29.7.2 using the `desktop-linux` context. The client repository was freshly cloned at tag `v0.3.0`, commit `97530e05ec8d895b5067672e190f59053b9fb284`, into a new temporary directory. The state directory `.rumble-client` began empty.

The run used public image digest `sha256:32ab3259f0ce69f319174b54ba36f4d85949c1a30ea4e65a94010b9c931acf2d` for Linux/amd64. The practice configuration had no `clientId`, selected only `1v1`, used `mode: practice`, and kept the documented default state directory. Runtime checks passed with Java 25.0, .NET SDK 10.0, Python 3.14 and Node.js 24.21. Configuration validation passed. Synchronization read `rumble-data` commit `8e30996a829aa72c54eecc7c741ea8fd19e2a90c`, accepted behavior version 1 and cached 15 active bots from snapshot `89ebc9e78ef40b576dec0bff1cb6ff755183004d`.

The client completed one 35-round 1v1 battle, ID `7fd39f6a-e276-43ae-b1f2-250799813710`, with Vector 1.0.0 and Corners 1.0.0. Replay evidence remains in the temporary checkout at `.rumble-client/evidence/7fd39f6a-e276-43ae-b1f2-250799813710.battle.gz`. The new active `RumbleTestScout` 1.0.2 version was not selected. The `submit` command was not run, so no issue, receipt or public ranking change was created.

## Attempted and rejected configuration

The first practice configuration set `workDirectory` to `.rumble-client-m025-practice`. The pinned launcher mounts its state directory at `/work/.rumble-client`, so the client could not write to that custom path and synchronization stopped with a read-only-filesystem error. This was a nonstandard override introduced for the preflight; the guide's documented `.rumble-client` path worked without a guide change. No battle ran during the failed attempt.

## Meaning for M-025

This prepared-host run confirms that the current published image can synchronize and run a local practice battle with the bundled runtimes. It does not demonstrate a non-maintainer following the author and contributor journeys, running the active test bot, submitting a ranked result, collecting its acceptance receipt or seeing that result in the public ranking. M-025 remains open until a non-maintainer completes those steps in a documented environment.
````
