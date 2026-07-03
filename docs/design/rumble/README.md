# Tank Royale Rumble - Umbrella Design Document

> **Status: DRAFT** - design direction captured. The next step is to convert this design into
> ordered OpenSpec change proposals before implementation starts.

This is the coordinating umbrella document for the design of an automated, serverless,
decentralized, community-driven tournament system for Robocode Tank Royale. It holds the overall
idea, the design principles, and the map of the detailed design documents. Details live in the
sub-documents; this document should stay short and stable.

## Vision

A continuously running, community-driven league (in the spirit of the classic RoboRumble and
LiteRumble) where:

- Bot authors submit bot **source code** via pull requests.
- Community members run battles on **their own machines** using a small rumble client.
- Results are collected, aggregated into rankings (APS as the primary metric), and published on a
  static dashboard.
- The entire system (bots, results, ranking logic, dashboard) lives **inside Git repositories**,
  with automation provided by the forge's CI (GitHub Actions primarily, portable to GitLab CI and
  Forgejo/Gitea Actions).

## Design Principles

These principles apply to every sub-document. A design that violates one of them needs an explicit,
written justification.

| # | Principle | Meaning |
|---|-----------|---------|
| P1 | **Zero infrastructure cost** | No servers, no paid databases, no cloud functions. Only free-tier forge features (repos, CI, Pages, issues). |
| P2 | **Forkability** | A fork of the repositories plus "enable CI, enable Pages" is a fully functioning rumble. Nothing canonical may live outside Git. |
| P3 | **No secrets** | CI uses only the forge's built-in token. Nothing to rotate, nothing that expires with a person. Clients hold at most a token that can open issues. |
| P4 | **Single writer** | Only CI commits to the data repository. Clients and humans never do. Race conditions are eliminated by construction, not by locking. |
| P5 | **Event sourcing** | Raw match results are immutable, append-only facts. Everything else (leaderboard, pairing stats, match advice) is a derived projection that anyone can recompute locally with one script. |
| P6 | **Advice, not locks** | Matchmaking files tell clients what battles are *useful*, never reserve battles. Duplicate work produces extra samples, which are statistically welcome. |
| P7 | **Portability** | All logic is plain scripts (Python). CI YAML files are thin wrappers. The only forge-specific seam is "how a result payload reaches the validator". |
| P8 | **Bus-factor resilience** | Hosted in an organization with 3+ owners. Governance is a document in the repo. A quarterly "fork drill" verifies P2 actually holds. |

## System Overview

```mermaid
flowchart TB
    subgraph bots["Repo: rumble-bots"]
        botsrc["bots/&lt;platform&gt;/&lt;Name&gt;/<br/>source + config (booter convention)"]
        botindex["bots/index.json<br/>(generated: versions + source hashes)"]
    end

    subgraph data["Repo: rumble-data"]
        raw["results/raw/<br/>(immutable facts)"]
        proj["leaderboard.json · pairings.json<br/>matches_needed.json (projections)"]
        site["Static dashboard (Pages)"]
    end

    author["Bot author"] -->|"pull request"| botsrc
    botsrc -->|"CI validate + index"| botindex

    client["Rumble client<br/>(wraps Battle Runner)"] -->|"pull"| botindex
    client -->|"pull"| proj
    client -->|"submit results<br/>(issue-ops / fork-PR)"| inbox["Result inbox"]

    inbox -->|"scheduled CI drains,<br/>validates, commits"| raw
    raw -->|"aggregate.py<br/>(pure function)"| proj
    proj --> site
    viewer["Anyone"] --> site

    style raw fill:#2980B9,color:#fff
    style proj fill:#27AE60,color:#fff
    style inbox fill:#E67E22,color:#fff
```

## Repository Topology

Two repositories under a community organization:

| Repo | Contents | Who writes |
|------|----------|-----------|
| `rumble-bots` | Bot source in booter-convention folders, submission policy, validation CI | Community via PR, moderators merge |
| `rumble-data` | Raw results, projections, aggregation scripts, matchmaking output, dashboard | CI only (single writer) |

Rationale for the split: the bots repo has human-speed, review-gated history; the data repo
accumulates thousands of machine commits and needs periodic compaction. Mixing them would make the
bots repo unpleasant to fork and review.

## Design Documents

| Document | Scope | Status |
|----------|-------|--------|
| [Bot Submission and Handling](./bot-submission.md) | How bot source enters the system: PR flow, validation, review, versioning, governance | Draft |
| [Rumble Client: Battles and Result Upload](./client-battles-and-results.md) | The client loop, matchmaking consumption, own-bot priority, result submission, trust model from the client's side | Draft |
| [Result Aggregation and Dashboard](./aggregation-and-dashboard.md) | Ingestion of submitted results, validation and quarantine, ranking math, projection files, static dashboard, operations | Draft |
| [User Documentation and Onboarding](./user-documentation.md) | The document set for bot authors, battle contributors, and moderators; onboarding journeys; friction budget | Draft |

## Change Proposal Roadmap

OpenSpec changes should be created in this order. Each proposal must stop at Proposal, Design,
Specs, and Tasks until explicitly approved.

| Order | Change | Purpose | Depends on |
|-------|--------|---------|------------|
| 1 | `prepare-tank-royale-for-rumble` | Prepare this repository before any rumble-specific repositories exist: `behaviorVersion`, bot config license metadata, Battle Runner result support, Rumble game presets, and a deterministic replay-regression hook. | None |
| 2 | `create-rumble-bots-repo` | Define and scaffold the bot submission repository: source-only layout, validation, ownership, slot budget, templates, governance, and generated catalog. | Tank Royale prep |
| 3 | `create-rumble-data-repo` | Define and scaffold the data repository: result inbox, validation, raw facts, aggregation, compaction, matchmaking projections, dashboard, and GitHub Pages publishing. | Tank Royale prep |
| 4 | `create-rumble-client` | Build the ranked/practice client, local journal, replay evidence store, container, and issue-ops / fork-PR submission transports. | `rumble-bots` catalog and `rumble-data` engine/matchmaking files |
| 5 | `publish-rumble-docs` | Publish the user guides, onboarding flow, moderator handbook, FAQ, dashboard participation page, ADRs, and contributor-facing architecture docs. | Interfaces from the previous proposals |

The first proposal is intentionally a Tank Royale preparation change, not a Rumble implementation.
It should make the existing engine, schema, Battle Runner, and bot metadata ready for the later
repositories without creating those repositories.

## Building Blocks Already in This Repository

The rumble does not start from scratch. These existing modules are the foundation:

| Module | Role in the rumble |
|--------|--------------------|
| [`runner/`](/runner/README.md) | Battle Runner API: embedded server, bot process management, identity matching by name+version, `BattleResults`, multi-battle reuse. The rumble client is a thin wrapper around this. |
| [`booter/`](/booter/README.md) | Defines the bot directory convention (`<Name>/<Name>.json` + platform boot scripts) that the submission repo adopts unchanged. |
| [`recorder/`](/recorder/README.md) | Produces `.battle.gz` replays, usable as optional result evidence. |
| [`schema/schemas/`](/schema/schemas/) | `results-for-observer` defines the per-participant score fields; the raw result record maps 1:1 onto it. |

## Design Directions

These directions keep the sub-documents consistent and should be reflected in the OpenSpec
proposals:

| Direction | Where detailed |
|-----------|----------------|
| **Ruleset and scoring = RoboRumble/LiteRumble, unchanged** (APS primary; Win%, Survival, Vote, NPP/ANPP, KNNPBI, Glicko-2). Battle tested for two decades; do not reinvent. | Aggregation doc |
| **V1 battle types are 1v1, TwinDuel, and Melee.** These follow the popular LiteRumble/RoboRumble categories for the original game. TwinDuel is the 2v2 twin-team format. Mini, micro, nano, and giga categories are out of v1 because bytecode-size limits do not translate cleanly to source-code bots across multiple languages. | Client + aggregation docs |
| **Own-bot priority: yes**, with the self-reported-only marker plus independent confirmation for trust. | Client doc |
| **Engine pinning by `behaviorVersion`.** Release versions stay lockstep across all Tank Royale artifacts (the right model for the product); a separate integer `behaviorVersion`, owned by the server and bumped only on game-observable changes (server physics/scoring/turn processing/RNG plus Bot API behavior), is the compatibility contract. Compatibility, client rollout, and result **epochs** all key on it; releases that do not bump it (e.g. GUI-only) cause no rollout and no epoch reset. Supersedes the earlier patch-vs-minor rule. | Client + aggregation docs |
| **`behaviorVersion` bump discipline is guarded.** The Tank Royale preparation proposal adds the behavior-version model and a deterministic replay-regression hook. The full replay corpus can land in a later proposal if it would make the preparation change too large. | Client + aggregation docs |
| **One active version per bot** (version bump supersedes the old one, RoboRumble style), plus a per-owner **bot slot** budget. | Submission doc |
| **Owner (forge account) is distinct from authors (display names)**; ownership drives permissions, slots, bans, and self-report detection. | Submission doc |
| **Banning** of owners/bots via an auditable list; enforced automatically in submission CI and result validation. | Submission + aggregation docs |
| **Practice mode** in the client: free local battles against rumble bots, never submitted; ranked mode auto-submits everything. | Client doc |
| **Replays stay client-side** as read-only evidence, bound to results by `battleId` (UUID) + SHA-256. | Client doc |
| **Batched submissions** from a local journal; clients never touch Git history (no commits, no amends). | Client doc |
| **Runtimes ship in the client container** (JVM, .NET, Python, Node.js + pinned engine), tagged by engine version; install scripts as bare-metal fallback. | Client doc |
| **Forge ToS reviewed**: this usage is a software project, not detached storage; design keeps traffic and repo size deliberately modest. | Aggregation doc |
| **Bot names are bound to their owner** at first merge; only the owner's registered accounts may submit new versions. Owners may register multiple forge accounts; account changes require a PR from an already-registered account. | Submission doc |
| **Official Bot APIs required** for ranked bots (Java, C#, Python, TypeScript); custom frameworks are not eligible. Resolves the dependency allowlist per platform. | Submission doc |
| **Run from source, never call a compiler**: runtimes compile behind the scenes on clients, and CI validates with a source-run smoke boot instead of a build step. | Submission doc |
| **Bot slots are configurable**, defaulting to 5 active bot entries per owner for launch. The value must be a named validation/governance constant, not a magic number. | Submission doc |
| **License required per bot**, validated in CI against a small permissive allowlist; missing or wrong license dismisses the PR. | Submission doc |
| **Evidence backups are the user's responsibility**, actively encouraged by the client; replays are never held centrally. | Client doc |
| **Submission at battle boundaries** (a battle = the game type's full round count); container gets a forge-only egress allowlist; journal staleness is bounded by the engine pin. | Client doc |
| **Issue-ops spelled out**: submissions are labeled forge issues carrying a JSON batch envelope, drained and closed by CI; spam is handled by strict format, per-account budgets, and forge-level blocking as last resort. | Aggregation doc |
| **Onboarding PR required from day one** for result submitters: a one-time, moderated registration under `clients/` in the data repo; unregistered submissions are closed unprocessed. | Aggregation doc |
| **GitHub Pages confirmed** for the dashboard; compaction policy settled (monthly rollups to the archive branch after three full months). | Aggregation doc |
| **License allowlist and mechanism settled**: MIT, Apache-2.0, BSD-3-Clause, GPL-3.0-or-later, declared via an SPDX field in the bot config, binding per `CONTRIBUTING.md`, submitter responsibility on the DCO model, notice-and-takedown for bad-faith uploads. | Submission doc |
| **Confusable-name check designed**: UTS #39 skeleton + leetspeak folding + normalization; identical skeleton fails validation, edit distance 1 flags for moderators. | Submission doc |
| **Documentation is a first-class design area**: published user docs are Markdown under `/web/docs/rumble/`, internal ADRs and architecture descriptions stay under `/docs`, one quickstart exists per audience, error messages link into the published docs, and onboarding friction is budgeted. | User documentation doc |
| **No per-client contribution cap**, matching the classic rumble (verified on the RoboWiki): saturation is handled by the priority mechanism alone; `targetSamplesPerPairing` plays the classic `BATTLESPERBOT` role. | Client + aggregation docs |
| **SPDX `license` field joins the general booter bot config schema** (not rumble-only); implementation lands in the Tank Royale repo when the design leaves draft. | Submission doc |
| **Docs and repos are versioned by the Tank Royale version**: both repos are tagged at every engine pin change; docs always describe the pinned engine, old tags serve old readers. Template bots per platform ship in `rumble-bots`; scoring explanations live in the Rumble FAQ, linked from dashboard column headers. | User documentation doc |

## Game Type References

Tank Royale Rumble should use the classic community names for ranked game types:

| Tank Royale Rumble name | Classic source | Notes |
|-------------------------|----------------|-------|
| `1v1` | LiteRumble `roborumble`; classic Robocode `roborumble.txt` | Standard duel category: two individual bots, 35 rounds, 800 x 600 battlefield in classic Robocode. |
| `Melee` | LiteRumble `meleerumble`; classic Robocode `meleerumble.txt` | Multi-bot category: 10 individual bots, 35 rounds, 1000 x 1000 battlefield in classic Robocode. |
| `TwinDuel` | LiteRumble `twinduel`; classic Robocode `twinduel.txt`; RoboWiki "Twin Duel" | Team category where each participant is a two-bot twin team; 75 rounds, 800 x 800 battlefield in classic Robocode. Use `TwinDuel` / `twinduel`, not `2v2`, so it is not confused with arbitrary two-bot teams. |

Classic Robocode also has `TeamRumble` (`teamrumble.txt`), but Tank Royale Rumble v1 should not
include a general team category. Mini, micro, nano, and giga variants are also excluded from v1
because their classic definitions depend on bytecode-size limits.

References:

- [LiteRumble home](https://literumble.appspot.com/) lists `roborumble`, `meleerumble`,
  `twinduel`, `teamrumble`, and size-limited variants.
- [RoboWiki: Contributing to RoboRumble](https://robowiki.net/wiki/RoboRumble/Contributing_to_RoboRumble)
  names the classic runners: `roborumble`, `meleerumble`, `teamrumble`, and `twinduel`.
- Classic Robocode local configs: `C:\Code\robocode\robocode.content\src\main\resources\roborumble\roborumble.txt`,
  `meleerumble.txt`, `teamrumble.txt`, and `twinduel.txt`.

## Glossary

| Term | Meaning |
|------|---------|
| APS | Average Percentage Score: a bot's mean, over all its pairings, of its mean score share per pairing. Order-independent, so the leaderboard is a pure function of the result set. |
| Pairing | An unordered pair of bot versions that can meet in battle. |
| Projection | A file derived entirely from the raw results (leaderboard, pairing stats, match advice). Disposable and reproducible. |
| Fact | An immutable raw result record. Never edited, only added. |
| Issue-ops | Using forge issues as a write-free submission inbox, drained by CI. |
| Single writer | The rule that only the aggregation CI commits to `rumble-data`. |
| Owner | The forge account that submitted a bot; drives permissions, slots, bans. Distinct from the display `authors` in the bot config. |
| Behavior version | Server-owned integer bumped only on game-observable changes (physics, scoring, turn processing, RNG, Bot API behavior). The compatibility contract between engine, clients, and results; independent of the lockstep release version. |
| Epoch | The result partition for one `behaviorVersion`. Releases that keep the behavior version stay in the epoch; a bump opens a new one and the ranked table restarts sampling. |
| Practice mode | Client mode for private tuning battles; results are never submitted. Ranked mode auto-submits everything. |
| Journal | The client's local append-only file of ranked results, submitted in batches. |
