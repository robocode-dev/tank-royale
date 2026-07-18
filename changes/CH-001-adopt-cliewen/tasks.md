---
id: CH-001-tasks
type: tasks
status: open
links: [CH-001]
title: Task breakdown for CH-001
---

# CH-001 tasks

- [x] Vendor clue 0.3.0 release binary + SHA256SUMS under `.github/tools/` (checksum verified)
- [x] Install the five clue skills (v0.3.0 pair) into `.agents/skills/` (`.claude/skills` symlink covers the mirror)
- [x] Align the seven pre-existing project skills to the skill-set version stamp (drift check finding recorded in AN-001)
- [x] Create corpus taxonomy: `docs/README.md` map + folder READMEs with index blocks (goals, plans, capabilities, constraints, quality, analysis, decisions, architecture, design)
- [x] Seed G-001 (accepted) and P-001 (active) with milestones incl. named doors (test purpose tags, clue 0.4.0 upgrade)
- [x] Extract `openspec/specs/*` into `docs/capabilities/CAP-xxx/` (README + criteria + design), minting per-capability AC prefixes (261 ACs across CAP-001…CAP-012)
- [x] Convert pending OpenSpec change `add-typescript-bot-api-npm-publish` into a draft capability (CAP-013) + plan milestone (P-002/M-004)
- [x] Delete `openspec/` (specs, changes, config, project.md — content absorbed into corpus and AGENTS.md)
- [x] Convert 41 ADRs to clue frontmatter (ids preserved as ADR-0001…ADR-0041, `verified` with pre-Cliewen MADR acceptance recorded); delete `template.md` and `INDEX.md` in favor of README index blocks
- [x] Add clue frontmatter to all `docs/architecture/` and `docs/design/` documents (ARCH-001…ARCH-026, draft/inferred); remove redundant INDEX.md files; add report folder README; rewrite architecture/design folder READMEs with index blocks
- [x] Add constraints C-001…C-004 (markdown wrapping, review boundary, cross-platform Bot API parity, stable physics/backward compatibility); QS candidates deferred to AN-001 until made measurable
- [x] Add `.github/workflows/clue.yml` CI wall (pinned 0.3.0, armed via vendored binary)
- [x] Rewrite `AGENTS.md` as routing hub + repo-local layer; delete `openspec.md` instruction; strip superseded gates from `core-principles.md` and `architecture.md`; re-point `.github/copilot-instructions.md`, `.junie/guidelines.md`, `DEVELOPMENT.md`, `CONTRIBUTING.md`; unbreak `/openspec/` links in ADR-0030/0031/0032 and c4-views README; update rumble design prose to the change loop
- [x] Write extraction report `docs/analysis/AN-001-openspec-extraction.md` (incl. findings for upstream cliewen)
- [x] `clue validate` green locally (116 artifacts, 0 issues); grep for dangling `openspec`/`opsx`/INDEX references — only historical provenance mentions remain; local-link check clean for corpus files
- [x] Gradle build still green (sanity: corpus change must not affect the product build) — full build green except `:bot-api:java:test`, which is flaky in full-suite runs on the local machine (different timing tests across runs, each passing in isolation; test code byte-identical to `main`, pre-existing)
