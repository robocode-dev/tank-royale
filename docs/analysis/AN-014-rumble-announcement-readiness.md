---
id: AN-014
type: analysis
status: active
links: [CAP-014, CAP-015, CAP-016, P-003, P-004]
title: Rumble public onboarding and announcement readiness
---

# AN-014 — Rumble public onboarding and announcement readiness

## Question and consumer

Can an ordinary Robocoder, without organization membership or repository write access, follow the published documentation to submit a bot, contribute ranked battles, and verify updated scores? This audit supplies the maintainer's pre-announcement correction backlog. It does not change requirements or reopen completed plans. Recommendation: correct the participant blockers below and demonstrate the journey with an outside contributor before announcing unrestricted participation.

## Evidence boundary

Investigated on 2026-10-18 using Windows, PowerShell, Python, Git, GitHub CLI, public HTTP requests, GitHub documentation, and source inspection. Repository access used an existing authenticated GitHub CLI; public website and GHCR metadata checks did not use personal credentials. All checks are classified as **prepared-environment checks**, requiring this toolchain and network access. No clean-machine onboarding, new participant registration, public issue submission, bot PR, container battle, or browser interaction was performed. Source-derived failure predictions are distinguished below from reproduced checks. No remote resources were modified.

Sources were pinned to these revisions; links in findings preserve the observed revision:

| Repository | Revision | Scope |
|---|---|---|
| Tank Royale | `8bb5ba1f0bbc11cf007150ac0f4f966238d7a734` | All five Rumble web guides, navigation, README, tutorial exits, Rumble plans/design |
| rumble-client | `c308a4c9de427d0bad467463049e6eb38a5d1f5a` | README, example configuration, launchers, build/publish configuration, selection and submission code |
| rumble-bots | `c5bea8e9e3100b4241c5e54f44b7033d27c91f53` | README, contribution/governance rules, PR template, validator, CI, catalog and example launchers |
| rumble-data | `0e40162a157776787ca237bc3217c5977f7c12dc` | README, contribution/governance rules, ingestion, dashboard entry page, catalog, engine and publication data |
| Book of Robocode | `ef77ed9812f39104b9a0481c6c29433f988b8b57` | Competition references, introduction, glossary, migration and Twin Duel guidance |
| Organization profile | `5cadea40e8378cda9ecd76f1823aebefa2400c86` | Public profile README |
| Robocode API bridge | `ae5e588553ef3281c9b13e5fcc5469273fb1b9f6` | README compatibility/discovery context only; bridge behavior was not audited |

The organization's repository inventory was reviewed. Sounds and graphics playground repositories were excluded because they do not own this onboarding flow. This is a documentation and targeted implementation audit, not a complete correctness/security review of every repository.

## What is already in place

The public Rumble section provides separate author, contributor, moderator, and ranking guides. It correctly distinguishes private GUI practice from ranked contribution, explains source-only submission and licensing, describes versioned identities and TwinDuel teams, and states that bot ownership and running a client are independent. The ranking guide explains equally weighted matchup averages, active-version eligibility, behavior epochs, cumulative rankings, historical snapshots, and publication timing. These are substantial foundations; another complete documentation system is unnecessary.

Observed checks:

- All 16 relative Markdown links in the five Rumble guides resolved to existing local files; anchors and every external destination were not exhaustively checked.
- The live overview, contributor guide, ranking guide, moderator guide, dashboard, and inspected Book pages returned HTTP 200. The deployed contributor guide and Book introduction contain the stale statements identified below.
- Live leaderboard JSON was accessible for 1v1, TwinDuel, and melee and contained battle results. The synchronized catalog matched the bot repository's count: 14 entries, including three teams, with JVM and Python represented.
- `python .git/rumble-audit/rumble-bots/scripts/validate_bot.py --root .git/rumble-audit/rumble-bots --owner documentation-audit` passed: `validated 14 bot(s)`. This was static validation without `--smoke`, on Windows; it does not establish Linux executable-bit correctness or that a new author's bot starts.
- Anonymous GHCR registry metadata access succeeded for `rumble-client:latest`; its image config reports `linux/amd64`, created 2026-09-15. This checked manifests/configuration, not a complete image pull or execution. The latest GitHub release was `v0.2.0`, with no downloadable release assets.
- The Maven Central POM for Battle Runner 1.3.1 returned HTTP 200.
- Recent ingestion, catalog synchronization, and Pages runs reported success. For example, [Pages run 39134666192](https://github.com/robocode-dev/rumble-data/actions/runs/39134666192) succeeded on the audit date. This establishes operational activity, not successful outsider onboarding.

## Findings to resolve before public participation

### F1 — The documented token path excludes the intended outside contributor

**Priority: blocker. Evidence: published documentation plus GitHub's documented access restrictions; no outsider-token experiment performed.** The [contributor guide](https://github.com/robocode-dev/tank-royale/blob/8bb5ba1f0bbc11cf007150ac0f4f966238d7a734/web/docs/rumble/client-guide.md) instructs users to create a fine-grained token restricted to `robocode-dev/rumble-data`, granting Issues read/write. [GitHub's token documentation](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#fine-grained-personal-access-tokens-limitations) explicitly lists contributions to public repositories by non-members and outside collaborators as unsupported. A merged `clients/<account>.json` registration is an application-level record, not organization membership or repository access.

The contributor guide, client README, and data contribution guide need a supported public authentication story with exact account prerequisites, approval behavior, expiration/recovery instructions, and verification. An internal token runbook does not supply that public path. Do not silently replace this with a broadly scoped token: changing the Issues-only credential boundary requires a full change and explicit contract/design review. Completion evidence should be a registered non-member account successfully submitting through the intended supported mechanism.

### F2 — An outside contributor's issue can remain outside ingestion forever

**Priority: blocker. Evidence: code and GitHub API contract; predicted outsider failure, not a live submission.** [GitHubIssueOpsTransport.java](https://github.com/robocode-dev/rumble-client/blob/c308a4c9de427d0bad467463049e6eb38a5d1f5a/src/main/java/dev/robocode/rumble/client/GitHubIssueOpsTransport.java) requests `result-submission` when creating the issue. [GitHub's create-issue API](https://docs.github.com/en/rest/issues/issues#create-an-issue) says labels are silently dropped for users without push access. The [ingestion workflow](https://github.com/robocode-dev/rumble-data/blob/0e40162a157776787ca237bc3217c5977f7c12dc/.github/workflows/ingest.yml) gates issue-triggered execution on that label and its scheduled fallback fetches only labeled issues. Scheduling therefore does not rescue an unlabeled outsider submission.

Choose and verify a repository-controlled intake mechanism that recognizes permitted submissions without granting participants push access. Then document the observable states: posted issue, accepted receipt, and published ranking. Authentication and label intake are separate problems; solving F1 alone does not solve F2. Completion evidence must include an outside account with no push access and no manual labeling by a maintainer.

### F3 — The bot-author instructions omit the required smoke protocol and executable bit

**Priority: high; blocks a straightforward copy of an otherwise working bot. Evidence: validator and example source.** The guide says to copy a working bot and run `--smoke`. [The validator](https://github.com/robocode-dev/rumble-bots/blob/c5bea8e9e3100b4241c5e54f44b7033d27c91f53/scripts/validate_bot.py) actually sets `RUMBLE_SMOKE=1`, invokes `sh`, expects a zero exit within 20 seconds, and requires `RUMBLE_SMOKE_READY <BotName>` in stdout. Neither the author guide nor `CONTRIBUTING.md` explains this protocol. A normal GUI bot is not required to implement it. The validator also requires executable `.sh` files, but the guide omits `git update-index --chmod=+x` or equivalent instructions, particularly relevant on Windows.

Document the smoke contract, shell prerequisites, Python minimum (the validator imports `datetime.UTC`, requiring Python 3.11+), actual Java/source-launch requirements, and executable-bit check. Explain what smoke proves: [Orbit's smoke branch](https://github.com/robocode-dev/rumble-bots/blob/c5bea8e9e3100b4241c5e54f44b7033d27c91f53/bots/python/Orbit/src/Orbit.py) exits before importing its API, so this is not proof of server connection or a completed battle. The workflow explicitly installs Python and Java; the guide's claim that CI has all four runtimes needs an explicit supported setup/verification for C# and TypeScript rather than reliance on runner defaults.

### F4 — The promised template is a PR checklist, not a runnable bot

**Priority: high. Evidence: direct link inspection.** The [bot README](https://github.com/robocode-dev/rumble-bots/blob/c5bea8e9e3100b4241c5e54f44b7033d27c91f53/README.md) says to copy “the bot template” into the bot directory, but the link targets `.github/PULL_REQUEST_TEMPLATE/bot-submission.md`, a Markdown checklist. Current catalog examples cover Java and Python only. There is no equivalent turnkey catalog example for C# or TypeScript, despite those languages being advertised for ranked participation.

Provide copy-and-rename templates for each supported language with both launchers, correct metadata, source-only dependencies, smoke support, and a TypeScript lockfile. Adapt the checklist for team submissions, whose JSON-only directory deliberately has no launchers. Validate each template through the same submission path before linking it as a starting point.

### F5 — The native client guide describes an obsolete dependency model

**Priority: high. Evidence: deployed text, current build, Maven response.** The contributor guide says the Battle Runner release does not exist and requires an adjacent Tank Royale checkout and `-PtankRoyaleSource` on every invocation. The [current client build](https://github.com/robocode-dev/rumble-client/blob/c308a4c9de427d0bad467463049e6eb38a5d1f5a/build.gradle.kts) uses the published dependency; [gradle.properties](https://github.com/robocode-dev/rumble-client/blob/c308a4c9de427d0bad467463049e6eb38a5d1f5a/gradle.properties) pins 1.3.1. The README already distinguishes an ordinary build from source-dependent integration coverage. Requiring an unpinned current sibling checkout also risks giving users a different engine from the supported client.

Rewrite the optional native section against the current client dependency model. Keep container setup first, and move integration/development builds out of the participant walkthrough. Reverify the hard-coded native Python API version instead of assuming it follows the client image automatically.

### F6 — The recommended container path is not a complete ordered walkthrough

**Priority: high usability correction. Evidence: guide ordering and configuration.** Container `validate`, `sync`, `run`, and `submit` commands appear before registration and configuration creation. The subsequent numbered run/submit instructions are native Gradle commands. The guide lacks a single uninterrupted sequence from clone to an accepted result using the recommended container path.

Use this order: prerequisites → clone → registration PR and wait for merge → supported authentication → copy/edit configuration → pull/check image → validate/sync → one battle → submit → observe receipt → observe ranking → repeat. Provide complete shell and PowerShell variants. Start with `myBots: []`, one game type, and one-battle wording. The [example configuration](https://github.com/robocode-dev/rumble-client/blob/c308a4c9de427d0bad467463049e6eb38a5d1f5a/rumble-client.example.json) currently lists three game types and 50 battles per session even though `--run` selects only the first sorted game type and executes one battle. The prose explains that limitation, but the copyable default encourages the opposite expectation.

## Corrections and improvements for announcement readiness

| Finding | Evidence and user impact | Recommended correction |
|---|---|---|
| F7: The Book contradicts availability | [Introduction](https://github.com/robocode-dev/book-of-robocode/blob/ef77ed9812f39104b9a0481c6c29433f988b8b57/book/introduction/what-is-robocode.md) says Tank Royale has no community competition system; the deployed page has the same text. [Competition formats](https://github.com/robocode-dev/book-of-robocode/blob/ef77ed9812f39104b9a0481c6c29433f988b8b57/book/energy-and-scoring/competition-formats-rankings.md) retains pre-Rumble positioning. | Add Tank Royale Rumble, link the authoritative participation/ranking guides, and distinguish classic RoboRumble/LiteRumble and their size classes from Tank Royale. Review glossary, migration and Twin Duel cross-links without erasing valid classic-specific information. |
| F8: Catalog readiness statements conflict | The bot README says server-connected smoke validation is the next increment before public ranked submissions open; its governance document calls `rumble-data` future and requires at least three moderators before opening. | Resolve whether these are actual outstanding launch conditions or stale wording. Do not remove a real gate merely to align marketing. Moderator staffing/permissions were not verified in this audit. |
| F9: Discovery stops short | Main Tank Royale README, homepage body and tutorial exit have no Rumble call to action; the org profile has none. VitePress navigation does link Rumble. The dashboard links rankings and contributing battles but has no direct submit-a-bot entry. | Add “Submit your bot”, “Contribute battles”, and “View rankings” at those entry points; link the bot repository back to the public quickstarts. A new standalone Participate page is optional if direct links suffice. |
| F10: No participant troubleshooting path | Guides describe successful operation but lack a symptom-to-resolution reference for rejected/absent receipts, missing labels, 401/403, unknown `myBots`, incompatible engines, insufficient opponents, or a new bot awaiting catalog sync. | Add a compact troubleshooting section with expected output and links to the correct support repository. Show where to find a submission issue and acceptance receipt, and distinguish submission success from score publication. Explain repeat submission and preservation of journal/evidence. |
| F11: Host and storage limits are incomplete | Anonymous registry inspection found only `linux/amd64`; Apple Silicon/ARM execution was not verified. Launchers fix CPU limit at 4, memory at 8 GiB and mount only `/work/.rumble-client`, although generic configuration docs describe arbitrary `workDirectory`. | State supported architecture and resource settings, verify or explicitly qualify ARM/emulation, and document the container's required state path or support configurable mounts. Explain upgrading/pulling a newer image after compatibility refusal and retaining state. |
| F12: Repeated contribution needs an explicit recipe | One battle per command is documented, but a newcomer arriving from classic RoboRumble may expect an unattended session. `battlesPerSession` is currently unused. | Give a tested bounded shell/PowerShell repeat recipe with failure handling, submission cadence and stopping instructions, or state clearly that users repeat the commands manually. Avoid promising unattended sessions the client does not implement. |

## Suggested delivery order and acceptance evidence

1. Resolve F1 and F2 as a full change if authentication, intake behavior, or the trust boundary changes. Demonstrate a registered external account creating a result, receiving acceptance, and appearing in published rankings without repository write access or maintainer repair.
2. Repair the author path: four runnable templates, smoke instructions, executable-bit handling, accurate prerequisites, and team-specific checklist. Demonstrate a new bot/version PR using only the published instructions, with no undocumented preparation. Label prepared demonstrations honestly and follow with a clean environment where feasible.
3. Consolidate the recommended container walkthrough and update native guidance, configuration defaults, troubleshooting and platform limitations. Show one successful run and receipt, a repeat contribution, a deliberate recoverable error, and recovery without losing pending results.
4. Correct Book/catalog contradictions and add discovery links. Review the public pages after deployment, including the path from the announcement to submitting a bot and from the dashboard back to participation.
5. Have a Robocoder without maintainer access follow both journeys. Record OS/architecture, exact image and source revisions, credentials/access class without secrets, manual interventions, and observed result/receipt URLs. A maintainer-account success is useful operational evidence but does not close the outsider findings.

Documentation corrections preserving accepted meaning fit the simple route. New templates, changed validation promises, public authentication/intake changes, or changed support policies need route reassessment by semantic scope. The aspirational `onboarding.md`, `faq.md`, and separate moderator handbook are explicitly not outstanding promises of P-003/P-004; prioritize working journeys over creating those particular filenames.

## Approaches considered

Reading only the main repository was insufficient because both contradictory public guidance and intake constraints live elsewhere. Source-only review was supplemented with deployed-page checks, registry metadata, Maven availability and live leaderboard reads. Using an existing privileged account to post a test result was not performed: it would alter public competition data and would not test the affected outsider path. This audit proposes corrective work; it does not choose a replacement authentication architecture, change governance, publish an announcement, or assert clean-machine readiness.
