---
type: agent-hub
title: Tank Royale agent instructions
---

# Tank Royale — AI Agent Instructions

This repository runs **Cliewen**. The `/docs` corpus is the system of record and the permanent truth about this system.

## Critical rules

1. **Before your first tool call, whatever the request is, run `clue latest --quiet`.** It prints nothing when you are current. If it says you are behind, or reports `latest` as an unknown command, ask whether to run the coordinated [`clue-upgrade`](.agents/skills/clue-upgrade/skill.md) workflow; nothing upgrades without the maintainer's answer.
2. **Before editing, recommend a route.** Tell the user `Recommended route: direct` or `Recommended route: tracked`, why, and what discovery would change that recommendation. One question decides the route: does the work change the accepted contract? Direct is the default. Paths and diff size never decide the route. See [Routes](#routes).
3. **A route does not authorize a push.** Acceptance is local (`.clue/acceptance.yaml`), so work stays on a local branch: never push a branch, open a PR, or push accepted `main` unless the maintainer asks for that action in the conversation. A request is honoured for the change it names and nothing beyond it, and asking to push is not asking for a PR, nor the reverse. Direct work may be pushed to `main` only with the maintainer's explicit authorization for that specific change, and the commit carries an `Authorized-Push:` trailer naming it; earlier authorizations do not carry over.
4. **Only the human accepts a tracked change.** It reaches `main` only through `clue accept <candidate> --base <base>` on the exact candidate and base. The agent runs the command with `--check` first; the maintainer then runs it, or approves that exact candidate and base, and the agent may execute that recorded approval with `--approval <record>`. Never infer, reuse or fabricate an approval, and never accept your own change. `clue accept` never pushes. The [human integration boundary](docs/constraints/C-002-review-boundary.md) applies.
5. **`clue validate` judges the corpus.** Markdown prose is never hard-wrapped: one line per paragraph and per list item.
6. **Releases are repository-owned administrative work, not a Cliewen route.** A release must provide clear end-user value. Corrective releases for defects, security, compatibility, or urgent operational problems may ship without a new user-facing capability. Never release solely to unblock internal or downstream development; use local builds until a value-bearing or corrective release is warranted.

## System of record

When the request names or resolves to an artifact, run `clue context <id>` and read the bounded slice it prints. Otherwise read [`docs/README.md`](docs/README.md) to find the closest artifact, then run `clue context` on it. When the user asks what is next, run `clue next --all`, report the first actionable milestone and the alternatives, and read the selected plan with `clue context` before asking whether to start. The corpus follows Cliewen conventions since its extraction from OpenSpec ([extraction analysis](docs/analysis/AN-001-openspec-extraction.md)).

Requirements live as Gherkin acceptance criteria under `docs/capabilities/*/criteria.md`. AC IDs are namespaced per capability via `ac-prefix` frontmatter (registry in `docs/README.md`); the corpus is the registry, IDs are meaning-immutable. Test purpose tags (every test carries exactly one AC ID or Unit/Sanity/Arch tag) are being introduced capability by capability, see P-001/M-002. Extracted criteria sit at `status: draft` until their tests are wired.

Write the meaning of referenced goals, decisions, criteria, and rules in the sentence itself, so a person understands it without looking up identities. Before writing human prose, reports or handoffs, follow [Readable references](.agents/skills/clue-analysis/references/readable-references.md). Canonical IDs stay unchanged where they are literal or machine-facing data.

## Routes

**Direct** leaves the accepted contract unchanged: observational analysis with a named consumer, a defect correction restoring an unchanged criterion, regression evidence for an unchanged criterion, in-contract configuration, refactoring, maintenance, and editorial work. It uses no change identity, workspace, plan declaration, digest, or mandatory agentic review. Run the checks relevant to the changed surfaces.

**Tracked** changes the accepted contract: acceptance-criterion, capability, decision, policy, plan-promise, methodology, or uncovered-behavior meaning changes. Uncertainty makes tracked the honest recommendation. Run the change loop in the [`clue-delta`](.agents/skills/clue-delta/skill.md) skill: local branch from accepted `main` → `/changes/CH-xxx-slug/` proposal committed before implementation → implement → digest → verify and review the exact committed candidate → `clue accept --check` handoff. There is no acceptance brief: for each `Test-type: Human` criterion the proposal carries a `## Human evidence` section with what the maintainer actually observed, written before the digest. The handoff tells the maintainer in a few plain sentences what the change does, what was checked and what remains uncertain, with the exact revisions and the accepting command.

Reassess on semantic discovery and against the complete diff before integration. If direct work grows into tracked work, pause and recommend tracked. If the user explicitly declines a tracked recommendation, proceed as direct, keep the repository truthful, and add `Cliewen-Route: direct`, `Cliewen-Recommendation: tracked`, and `Cliewen-Override: user chose direct; <concise risk>` to the final authored commit. Coordinated identity reservations use the configured allocator independently of publishing the change.

Every change assesses documentation impact before it closes. Keep `docs/architecture/README.md` current for system structure and `docs/design/README.md` current for cross-cutting behaviour; capability `design.md` holds local detail. Add or update a document only when it answers a reader question that existing material cannot answer without duplication, and state in the handoff which durable documentation changed, or why none was needed.

## Skills

Reusable task instructions live in `.agents/skills/`. When the user runs a slash command, read the skill file and follow it exactly.

| Skill | Purpose |
|---|---|
| [`clue-delta`](.agents/skills/clue-delta/skill.md) | The change loop, used for every tracked change |
| [`clue-plan`](.agents/skills/clue-plan/skill.md) | Create or revise a plan with verifiable milestones |
| [`clue-analysis`](.agents/skills/clue-analysis/skill.md) | Spikes and findings; every analysis ends in a `docs/analysis` document |
| [`clue-verify`](.agents/skills/clue-verify/skill.md) | Pre-acceptance verification and bounded agentic review, alongside `clue validate` |
| [`clue-upgrade`](.agents/skills/clue-upgrade/skill.md) | Check for and, with the maintainer's approval, carry out a coordinated Cliewen upgrade (direct work) |
| [`clue-extract`](.agents/skills/clue-extract/skill.md) | Brownfield adoption (already executed here) |
| `/release` · `/update-deps` · `/deploy-sample-bots [dir]` · `/structurizr` | Release, dependency, sample-bot, and diagram workflows |

## Repo-local conventions

These conventions extend the Cliewen methodology and never override it. When a rule here would contradict a skill, that conflict is an open question for the maintainer, not a silent choice. A convention that binds every change is also registered as a constraint in [`docs/constraints/`](docs/constraints/README.md).

### Change proposal identity

For local acceptance, `proposal.md` uses the canonical change identity in frontmatter (`id: CH-xxx`, `type: change`, `status: open`); task and question files use suffixed identities (`CH-xxx-tasks`, `CH-xxx-open-questions`). This differs from the `clue-delta` skill, which suffixes the proposal too (`CH-xxx-proposal`), but `clue accept` 0.33.1 finds a change only by a proposal with `type: change` and the canonical `id: CH-xxx`, and refuses a suffixed proposal (verified 2026-11-21). The canonical form is therefore required here until Cliewen makes the skill and the command agree. Commit the proposal before implementation and retain its history after digest.

### Task-specific instructions

Load the file(s) matching your task type before starting work:

| Task | File(s) |
|------|---------|
| Debugging, timing, race conditions | `.agents/instructions/debugging.md` · `docs/DEBUGGING-GUIDE.md` |
| Bot API (Java / Python / .NET / TS) | `.agents/instructions/cross-platform.md` |
| Protocol, WebSocket, server comms | `docs/architecture/models/flows/README.md` · `docs/architecture/models/message-schema/README.md` |
| Testing, builds, Gradle | `.agents/instructions/testing-and-build.md` |
| Documentation, README, Javadoc | `.agents/instructions/documentation.md` |
| Changelog, release notes | `.agents/instructions/changelog.md` |
| Code style, naming, conventions | `.agents/instructions/coding-conventions.md` |
| File encoding, UTF-8 | `.agents/instructions/standards.md` |
| Architecture, decisions, C4 | `.agents/instructions/architecture.md` |
| Product facts, clean-code baseline | `.agents/instructions/core-principles.md` |

### GitHub PR descriptions

When creating or updating pull requests through the GitHub API or CLI, send real newline characters in the Markdown body; never send literal `\n` escape sequences. Prefer a real multiline body or `--body-file`, and inspect the stored body before considering the PR description complete.

<!-- CODEGRAPH_START -->
## CodeGraph

In repositories indexed by CodeGraph (a `.codegraph/` directory exists at the repo root), reach for it BEFORE grep/find or reading files when you need to understand or locate code:

- **MCP tool** (when available): `codegraph_explore` answers most code questions in one call — the relevant symbols' verbatim source plus the call paths between them, including dynamic-dispatch hops grep can't follow. Name a file or symbol in the query to read its current line-numbered source. If it's listed but deferred, load it by name via tool search.
- **Shell** (always works): `codegraph explore "<symbol names or question>"` prints the same output.

If there is no `.codegraph/` directory, skip CodeGraph entirely — indexing is the user's decision.
<!-- CODEGRAPH_END -->
