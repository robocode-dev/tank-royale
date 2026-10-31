---
id: ARCH-027
type: design
status: draft
links: [P-007, ADR-052, AN-023, ARCH-028, UC-005, CAP-001, CAP-012, CAP-014, CAP-015, CAP-016]
title: Target Rumble participation flows and environment boundaries
provenance: inferred
reversal-cost: low
---

# Target Rumble participation flows and environment boundaries

This is the target design for the [replacement campaign](../../plans/P-007-rumble-participation.md), not a description of shipped functionality. Existing capability criteria continue to describe the supported client until their implementing changes replace them. This document owns shared interfaces and boundaries; capability-local algorithms and schemas belong with their implementing criteria.

## Modules and process boundaries

| Component | Responsibility |
|---|---|
| New `lib:rumble` | Synchronization, scheduling, session state, GitHub integration, registration, durable queue, receipts, and typed status models; no Swing dependency |
| New `rumble` executable | Headless session controller and isolated worker entry point, with the same behavior as the GUI-managed session |
| New `lib:bot-project` | Starter generation, source inspection, submission staging, metadata validation, and validation orchestration |
| Existing GUI | Wizards, bot management, contribution controls, and presentation |
| Existing runner, server, booter, recorder | Battle execution and existing lifecycle responsibilities |
| Catalog and result repositories | Moderation, authoritative source catalog, registration, ingestion, and ranking projections |

```mermaid
flowchart LR
    GUI[GUI] --> CORE[Shared Rumble services]
    CLI[Headless controller] --> CORE
    CORE --> GH[GitHub catalog and results]
    CORE --> WORKER[Isolated battle worker]
    WORKER --> RUNNER[Existing Battle Runner]
    RUNNER --> ENGINE[Server, booter and replay recording]
```

The host owns credentials and upload authorization. Workers receive a pinned job and constrained source/evidence locations, and return typed results, replay references, and diagnostics. They never receive GitHub credentials, the host home directory, or a container-engine socket. Host/worker commands and events are versioned and machine-readable; callers never parse human logs. Unknown versions fail with an actionable error. GUI events are dispatched without blocking Swing's event thread. The GUI manages a separate controller and stops its managed session on exit; independent background operation uses the CLI. Reuse the replay viewer for finished battles; live contribution-battle visualization is deferred.

### Module dependencies

An arrow means "depends on". The rules below are what keeps the credential boundary real and the shared library free of desktop code; a later change adds an automated check that fails the build when a rule is broken.

```mermaid
flowchart TD
    GUI[gui] --> RUMBLE[lib:rumble]
    GUI --> BOTPROJECT[lib:bot-project]
    CLI[rumble executable, host mode] --> RUMBLE
    CLI --> BOTPROJECT
    RUMBLE --> BOTPROJECT
    RUMBLE --> COMMON[lib:common]
    BOTPROJECT --> COMMON
    WORKER[rumble executable, worker mode] --> RUNNER[runner]
    WORKER --> PROTOCOL[Host and worker messages in lib:common]
    RUMBLE -. process boundary, versioned messages .-> WORKER
    RUNNER --> CLIENT[lib:client]
```

- **The shared library has no desktop code.** `lib:rumble` never depends on Swing or on the `gui` module, so the GUI and the headless controller behave the same.
- **Only the host holds credentials.** GitHub tokens, registration and upload authorization live in `lib:rumble` and the host side of the executable. The worker mode depends on the runner and the shared message types only; it cannot reach the GitHub code and is started with no token, no home directory and no container socket.
- **The worker is reached only through versioned messages.** `lib:rumble` starts and talks to the worker across a process boundary with machine-readable commands and events. It does not depend on the runner, so replacing how battles run does not touch session logic.
- **Project tooling knows no GitHub.** `lib:bot-project` inspects, stages and validates a project and returns a typed staged submission. `lib:rumble` takes that value and does the fork, branch, commit and pull request work. The staging code never needs a token.
- **Nothing depends on the GUI.** Dependencies point from the GUI and the executable down to the libraries, never back.

## Downloading competitor bots

A Robocoder can download selected or all active competitor bots into a folder of their own; the [Download bots screen](gui-mockups.md) and the matching [use case](../../use-cases/UC-005-download-competitor-bots.md) show the journey. The rules:

- **Optional and public.** Downloading needs no GitHub sign-in, because the catalog and the bot sources are public. Nothing is downloaded unless the person asks.
- **Verified and pinned.** Only bots that are active in the pinned catalog commit are offered for a new download. Each bot's source is checked against the content hash the catalog records before it is placed in the folder, and a bot that fails is skipped and reported. The same catalog fetch and verification that ranked contribution uses is reused, so there is one implementation of it in `lib:rumble`.
- **Licensed and visible.** Every bot is published under a declared license that permits copying. The screen shows each bot's license before the download and keeps it with the bot's files.
- **A license level for every bot.** The Bot Info panel in New Battle and the download screen show the declared license with one of three levels, always as an icon and words: free to copy, copy with conditions (copyleft or credit required), or do not copy (no license declared, an unrecognised or proprietary license, or a no-derivatives or non-commercial license). The level is a reading aid for what the bot declares and is not legal advice. The GUI already receives the license and team members in its own boot entry, so the panel reads them by the bot's directory; no change to the game protocol is needed.
- **A separate folder.** The default is a `rumble-bots` folder beside the normal bots directory, and the person may choose another. The GUI adds it to the bot directories with a label, and shows its bots in New Battle as a collapsed group after the person's own bots. The folder is never one of the person's own bot directories unless they choose it explicitly.
- **Never overwrites.** A downloaded bot never replaces one of the person's own bots, even when the names match. Updates replace only earlier downloads of the same bot.
- **Withdrawn bots are marked, not erased.** A bot the Rumble later retires, withdraws or disqualifies stays in the folder, marked as no longer active, until the person removes it.
- **Ranked contribution does not use the folder.** Ranked battles use only the pinned catalog and its verified sources. Downloaded bots are for reading and for practice battles.
- **Untrusted code.** A downloaded bot is another person's code. In a practice battle in the GUI it runs with the user's own rights, like any bot in the normal directory. Moderator review and the container used for ranked battles do not extend to that case, so the screen says so and the design does not call downloaded bots safe.

Module ownership follows the dependency rules above: catalog fetch, hash verification and the download operation belong to `lib:rumble` and return typed results with no Swing code, and the GUI owns the folder choice, the label and the New Battle grouping. The bot-directory setting today records only a path and whether it is enabled, so a label for the downloaded folder needs an extended setting. The change that builds it must read the existing setting without losing entries.

## Environments and account boundary

The existing `rumble-bots` and `rumble-data` repositories and `https://robocode-dev.github.io/rumble-data/` remain the public Beta. The test environment is the repositories `rumble-bots-test` and `rumble-data-test` under `robocode-dev`, created after checking that those names were free, with its dashboard at `https://robocode-dev.github.io/rumble-data-test/`. The test dashboard carries a visible Test badge, a banner and a Test page title so it cannot be mistaken for the live one. Its two GitHub Apps, `tank-royale-rumble-moderator-test` and `tank-royale-rumble-publisher-test`, have the same permissions as the public Apps, are installed only on `rumble-bots-test`, and keep their keys as Actions secrets there; `rumble-data-test` has no App because the result repository uses none. A third public App, `tank-royale-rumble-contrib-test`, is the sign-in App for participants and keeps no secret; it is installed on `rumble-data-test` and on a participant's own fork of the bot repository. The test bot repository has its own main-branch moderation ruleset that requires the test moderator App's status check. Name conflicts are a maintainer decision, not permission to repurpose an existing repository. The development dashboard belongs to the test result repository. Share workflow sources or pinned reusable workflows; deployment inputs select the environment.

Environment identity is carried through configuration, catalog/result discovery, registrations, caches, journals, submissions, and reset tooling. Public and test state have separate roots and authorization installations. The receiver verifies its own environment; client-side labels are insufficient. Ordinary installations default to public Beta. Advanced environment selection shows a persistent development warning; switching never retargets an existing pending queue. Legacy state is imported as public state after source/destination verification, with the original retained until successful migration.

Test reset lives in Tank Royale as `scripts/rumble/rumble_environments.py`, so the later shared integration harness can call it. It knows exactly four repository names, two public and two test, and treats a repository as a test repository only on an exact match of owner and name, so a lookalike or foreign name is refused. A reset works on a local checkout of the test results repository, checks that its origin is that repository, removes accepted facts and derived projections, keeps moderation records, and raises a reset generation number that lets the receiver reject submissions made before the reset. Without `--apply` it only lists what would go. It does not decide anything about public data: it has no code path that reaches a public repository.

GitHub identity is required for publishing and ranked contribution, not local bot creation or practice. Use device authorization and Windows credential storage; never request a password or hand-copied personal token. Keep contribution authorization separate from additional publishing permissions. Never ship an application secret in the desktop client.

The [outside-account experiment](../../analysis/AN-023-outside-account-sign-in-experiment.md) proved this direction in the test environment with an ordinary account that is not a member of the organization, and recorded only rights the account itself held; a maintainer-token success never counted. What it showed:

- **The sign-in App.** A third public App, `tank-royale-rumble-contrib-test`, is what a participant signs in with. It has no private key or secret anywhere, because signing in with a code needs only its public client ID. Its device sign-in setting must be switched on by hand in the App settings, since GitHub does not allow that through the App manifest. The moderator and publisher Apps are not for sign-in and refuse it.
- **Tokens.** A user token lasts eight hours and comes with a refresh token. Renewal works without a client secret, so nothing secret has to live in the desktop client. Revoking the App in the participant's GitHub settings stops the token at once.
- **Registration without a maintainer.** The participant opens an issue titled `[register]` in the result repository. A trusted workflow reads the account from the issue author, refuses banned or malformed names, creates the registration file and a client ID, or returns the existing one, and replies on the issue.
- **Result intake without a label.** An outside account cannot apply the `result-submission` label. A `[result]` issue is therefore accepted by its title, the receiving workflow applies the label with its own token and always processes the issue that triggered it, and the reply on the issue is the durable receipt. Another account's client ID, a repeated battle ID and a banned account are each refused with a receipt.
- **Bot submission.** The participant forks the bot repository on the web and installs the App on that fork, once; the desktop program cannot create the fork through the API. The App can then push a branch to the fork and open a pull request, and the bot repository's own validation starts without a maintainer. The fork stays in the author's account under their control, which is why submission uses a pull request and not an issue.

Still unproven: the bot repository's own ban list against a fork pull request, and the move of the registration workflow and the label-free intake from the test result repository to the live one.

Automatic registration derives account identity from the authenticated request, checks bans and duplicates, and assigns or retains a stable installation client ID. Trusted repository workflows make authoritative changes. A payload cannot choose another owner or grant itself authority. Preserve existing registrations. No hosted broker is authorized; an incompatible permission boundary requires a reviewed decision.

## GUI and bot authoring

One top-level **Rumble** menu contains **Get started**, **Submit or update bot**, **My bots**, **Contribute battles**, **Results and queue**, **Rankings**, **GitHub account**, and **Help**. **Rankings** opens the selected environment's dashboard in the default browser without sign-in; public Beta uses `https://robocode-dev.github.io/rumble-data/`. **Help** opens the central participant documentation. **Battle → Create bot** is also available from Rumble's getting-started screen, and normal local battles stay separate from ranked contribution.

Create runnable Java, C#, Python, and TypeScript projects with consistent names, metadata, launchers, and pinned dependencies. Authors use their own editor and chosen language runtime. Submission must not require a host Git install, unrelated runtime, manual executable-bit changes, or manual JSON repair. Stage existing projects separately without rewriting their original files. Run validation, show actionable failures, then show the exact source and license declaration before a user publishes. Automate fork, branch, commit and PR creation; preserve ownership, version, and moderation rules. Track checks, review, publication, and active-catalog visibility separately. First versions and updates share this flow.

## Contribution sessions and result durability

Contributors choose **All bots**, **Prefer selected bots**, or **Only selected bots**. Multiple entries may be chosen regardless of ownership; preferences follow active versions. TwinDuel selections identify team entries. Prefer mode chooses a valid selected-entry battle before falling back; strict mode requires at least one selected entry in every battle. Opponents come from all eligible entries. If no valid strict matchup exists, wait and explain rather than spending compute elsewhere. Missing advice does not make an otherwise valid matchup impossible. Rotate fairly across selected entries, use coverage to choose opponents, and preserve ranked eligibility rules.

Start with one concurrent battle, configurable CPU/memory limits, selected game formats, and a battle-count bound or continuous operation. Rotate across selected formats. Pause finishes the active battle; stop offers finish-and-stop or cancel-now. Incomplete or cancelled battles never become completed ranked records. Live viewing is not needed to contribute.

Completed ranked results reach the durable journal before upload. When automatic submission is enabled, complete batches retry and collect publication-first receipts without manual per-batch posting. Preserve pending records across process interruption, network failure, revocation, rate limiting, and partial rejection. Quarantine incompatible engine epochs. Show completed, queued, submitted, accepted, rejected, and ranking-publication states separately. Never expose a favorable-result selection filter. Migrate existing journals, receipts, replays, client IDs, and `myBots` preferences non-destructively.

## Withdrawal and test cleanup

Owners withdraw their own entries; moderators may withdraw any entry. Preview the exact version and dependent teams before confirmation. Withdrawal excludes the entry from new matchmaking and current rankings and frees its active-entry slot, but preserves ownership, source history, accepted facts, and archived snapshots. Existing current-ranking rules exclude complete matchups containing an inactive participant, so opponents' current APS may change; the confirmation explains that effect. Disqualification remains a separate moderation action.

Dependent teams become ineligible without withdrawing unrelated member bots. Explicit restoration is permitted only if source/version ownership, active-version, dependency, and moderation rules still hold. Withdrawing a member does not silently substitute another member or version. Stale queued results are handled with explicit receipts under the resulting eligibility contract, never deleted without explanation.

The first public cleanup withdraws the then-active versions of the identified test bots `RumbleTestScout` and `Orbit`. Resolve their ownership, exact versions and all dependent teams from the catalog at execution time; the initial observation included an `OrbitVector` team, but the operation uses the current dependency graph. Preview and record these exact identities, then revalidate immediately before mutation. If a version or dependency changed, refresh the preview rather than withdrawing an unseen version. Unrelated bots remain untouched. Public test entries are withdrawn, not purged. Reset tooling operates only on explicitly selected development resources, previews the reset scope, and rejects public repository IDs and state paths. Test resets create a fresh test generation so old queued data cannot repopulate the reset environment.

## Readable dashboard and metadata

Leaderboard links open HTML detail pages with shareable bot identity, game format, and ranking period. Format-specific data must not overwrite another format's bot shard. Retain machine-readable JSON as a secondary link and preserve compatibility during migration. Existing archived snapshots stay immutable; where their old bot detail shard represents the wrong format, derive available statistics from that archive's selected-format leaderboard rather than pretending the shard is correct. Show missing information honestly.

Display name, version, authors/owner, description, language/platform, license, supported formats, lifecycle state, source link, and team members when relevant. Show rank, APS, battle and matchup counts, and update time for the selected format/period. Render text safely, validate outbound links, and keep provenance clear for historical metadata. Descriptive metadata changes should not force a strategy version reset where they do not change execution inputs.

Only maintainers grant **Official sample bot** status through trusted catalog metadata; submitters cannot self-certify it. Official samples remain ranked benchmarks. A visibility filter hides/shows their rows without recalculating APS or competitive rank. Beating samples is a learning target, not an entry gate; a badge does not promise a uniform difficulty across formats. Unknown historical sample metadata remains unknown rather than being silently backfilled.

## Documentation and verification

`https://robocode.dev/rumble/` is the canonical participant home. Keep repository-specific implementation contracts close to their code, and link other participant entry points to central instructions. Audit all organization repositories, not just the three Rumble repositories; record update-required, aligned, unrelated, historical-only, or inaccessible outcomes. Preserve established URLs or supply redirects. Public Beta wording describes what currently works and distinguishes the planned GUI. Update public instructions when the corresponding feature is available, not ahead of it.

M-029 delivers lib:bot-project and the initial integration harness; M-031 delivers author metadata, while M-033 delivers the trusted sample-metadata schema/update path and projections. Their dependencies are explicit in the campaign table. M-037 completes and verifies one documented local integration command with isolated fixture state, simulated GitHub responses, and real container battles where needed, producing a short report and redacted diagnostics. Use the development Rumble for real outside-account GitHub tests. A short public-Beta smoke check remains necessary because test installation permissions do not prove live installation permissions. Before public mutations, persist a run manifest identifying every intended and created bot/team/version. Finish by withdrawing every temporary entry through normal lifecycle operations and verifying its removal from active published rankings and matchmaking, while preserving accepted facts and snapshots. Failed or interrupted tests retain a resumable cleanup obligation; unresolved withdrawal prevents the final milestone from completing. The maintainer checklist asks for observations, not manual transcription of IDs and versions. Windows is the initial complete journey acceptance target; no evidence from a prepared machine is presented as a clean install.
