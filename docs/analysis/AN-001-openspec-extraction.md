---
id: AN-001
type: analysis
status: verified
links: [G-001, P-001]
title: Extraction report — OpenSpec corpus to Cliewen /docs (CH-001)
provenance: inferred
---

# AN-001 — Extraction report: OpenSpec → Cliewen

The record of CH-001 (2026-07-19), this repository's adoption of Cliewen via the `clue-extract` skill on the v0.3.0 binary+skills pair. Verified by human review of the CH-001 PR.

## What was found

- **Two overlapping methodologies**: OpenSpec (`openspec/` — 12 non-empty capability spec dirs, one pending change `add-typescript-bot-api-npm-publish`, an archive of applied changes, `project.md`, `config.yaml`) and 41 MADR-frontmatter ADRs (`docs/decisions/0001`–`0041`, all `accepted`), plus a bespoke agent-instruction system (`.agents/instructions/`, 12 files) with three assistant entry points (`AGENTS.md`, `.github/copilot-instructions.md`, `.junie/guidelines.md`; `CLAUDE.md` re-exports `AGENTS.md`).
- **OpenSpec scenarios carried no IDs** — unlike a tagged corpus, there was nothing to inherit; all AC IDs were minted at extraction.
- Architecture documentation without frontmatter: C4 views, message-schema and flow models, two health reports, cross-cutting design specs, and the draft Rumble design set under `docs/design/rumble/`.
- 7 project skills (`release`, `update-deps`, `deploy-sample-bots`, `structurizr`, `dot-scout`, `dot-prime`, `dot-audit`) beside the `.claude/skills → .agents/skills` symlink; the `/dot-*` principles catalog (`.principles` files).
- Test suites in four languages (Java, C#, Python, TypeScript) with **no purpose tags** — far larger than any prior adoption.

## What mapped where

- The 12 synced specs → capabilities [CAP-001…CAP-012](../capabilities/README.md), their scenarios → 257 Gherkin ACs under minted prefixes (BR, BFD, BAU, GBP, GBC, PRO, PBA, RCV, TCS, TFS, TBA, UD). Requirement prose and non-Gherkin scenario lines survive as comments inside the criteria. The empty `browser-sample-bots` spec dir produced no capability (its would-be prefix BSB is unregistered).
- The pending change → draft capability [CAP-013](../capabilities/CAP-013-typescript-bot-api-npm-publish/README.md) (TNP, 4 ACs) + plan [P-002](../plans/P-002-typescript-bot-api-npm.md) milestone M-004; its proposal's What/Impact detail was absorbed into CAP-013's design.md.
- **All extracted criteria are born `status: draft`** — deliberately: draft criteria are exempt from the AC↔test wall, and promotion to `active` per capability is exactly the M-002 test-tagging door in [P-001](../plans/P-001-cliewen-adoption.md). This is the phasing mechanism for large multi-language suites.
- ADRs keep their identity as ADR-0001…ADR-0041, converted to `status: verified` with `accepted-by: Flemming N. Larsen (<date>, pre-Cliewen MADR acceptance)` — the MADR acceptance predates the corpus and is preserved, not re-judged.
- Architecture and design docs → ARCH-001…ARCH-026, `status: draft`, `provenance: inferred`, promoted to `verified` as they are checked against code. The Rumble draft set stays under `docs/design/rumble/` with its roadmap re-expressed as future CH-xxx changes.
- `openspec/project.md` split: Purpose → [G-001](../goals/G-001-programming-game-for-learning-and-competition.md); Bot API Rules → [C-003](../constraints/C-003-cross-platform-bot-api-parity.md); Stability Rules → [C-004](../constraints/C-004-stable-physics-and-backward-compatibility.md); conventions → the repo-local layer of `AGENTS.md`.
- Quality-scenario candidates (population-level bot compatibility, protocol version tolerance) were observed but **not** minted as QS records — they enter [quality/](../quality/README.md) when made measurable, per the AN-003 calibration upstream (population-level promises are QS material, not ACs).

## What was dropped and why

- `openspec/` in its entirety, including the archive — git history is the archive; two systems of record is zero.
- `.agents/instructions/openspec.md`, the OpenSpec approval gate, the ADR Review Gate in `architecture.md`, and the git-workflow/process half of `core-principles.md` — superseded by the change loop and [C-002](../constraints/C-002-review-boundary.md) (branch + human-merged PR is the gate). Product facts, the `/dot-*` principles system, and the topic instruction files (testing, conventions, debugging, changelog, documentation, standards, cross-platform) survive untouched.
- `docs/decisions/template.md` and every `INDEX.md` — README index blocks (`clue:index` markers) are the indexes now.

## Code touched

None. No production code and no test changed; the Gradle build is unaffected by design. Test purpose tagging was deliberately deferred to M-002 rather than half-done here.

## Known doors

- **M-002** (P-001): purpose tags across the four language suites, capability by capability; each capability's criteria go `draft → active` as its tests are wired, arming the wall incrementally.
- **M-003** (P-001): cliewen goes public → upgrade pin v0.3.0 → v0.4.0, replace the vendored binary with a direct release download.
- Rumble: converting `docs/design/rumble/` into an active plan with ordered changes when work starts.
- Re-review of the `draft` ARCH records; promotion of `provenance: inferred` artifacts to `verified` as the human confirms meaning.
- Whether to fold the `/dot-*` principles system into the methodology — explicitly out of scope for CH-001.

## Findings for upstream cliewen (candidate CH in its corpus)

Adoption friction observed with the v0.3.0 pair, to be proposed as a skills/tooling change upstream (tank-royale is public, so citing it is allowed):

1. **Skill-set version drift check scopes too wide**: `clue validate` requires *all* skills under `.agents/skills` to share one version stamp, so 7 pre-existing project skills had to be stamped `0.3.0` — a foreign version claim. The check should scope to `clue-*` skills (or a declared set).
2. **`skill.md` case sensitivity**: clue looks for lowercase `skill.md`; this repo's project skills use `SKILL.md`, which matches only on case-insensitive filesystems — a Linux-CI divergence waiting to happen.
3. **No `init` in the released 0.3.0 binary**: layout had to be hand-materialized from the model2diagram precedent; the quickstart contract assumes `clue init` exists in a released version.
4. **`.claude/skills` symlink**: `clue init` (when released) should detect a symlinked skills dir and skip mirroring instead of fighting it.
5. **No MADR mapping in `clue-extract`**: `mappings/openspec.md` exists, but a 41-ADR MADR corpus needed ad-hoc conversion rules (status vocabulary, `accepted-by` for pre-existing acceptance). A `mappings/madr.md` would make this mechanical.
6. **Multiple assistant entry points**: the extract contract says "AGENTS.md re-pointed", but real repos have Copilot/Junie/CLAUDE entry files too; the contract should name them as a class.
7. **Phasing guidance for large corpora**: born-`draft` criteria as the test-tagging phasing lever (finding above) worked well and should be documented in the extract skill rather than rediscovered.
8. **Sources without scenario IDs**: the openspec mapping assumes IDs survive verbatim; guidance for minting IDs (and the non-Gherkin-line → comment rule) was improvised here.
