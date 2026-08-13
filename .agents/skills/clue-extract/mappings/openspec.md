# clue-extract mapping: OpenSpec

Source mapping for [clue-extract](../skill.md) — the target contract in `skill.md` governs; this file only says what maps where for an OpenSpec corpus.

Layout: `openspec/config.yaml`, synced truth in `openspec/specs/<capability>/spec.md`, pending work in `openspec/changes/<name>/` (proposal.md, design.md, tasks.md, spec deltas), applied work in `openspec/changes/archive/`.

| OpenSpec | Cliewen |
|---|---|
| `specs/<cap>/spec.md` | `docs/capabilities/CAP-xxx-<cap>/` — one capability per spec file |
| Spec header + purpose prose | capability `README.md` (what/why, `goal:` link) |
| `### Requirement:` + SHALL + `#### Scenario: <name> [ID]` | Gherkin scenarios in `criteria.md`, tag line `@<ID>` — keep the ID, whatever bracket/backtick notation the source used |
| `Test-type:` line per scenario | first body line inside the Gherkin scenario; preserve it as the declared proof class that `checkACTests` enforces |
| Scenario ID prefix (e.g. `MG` or `SNAP-SQS`) | `ac-prefix:` in that criteria.md frontmatter; segmented uppercase prefixes and letter-suffixed numeric IDs stay canonical and a delta spanning several prefixes splits into one capability per prefix |
| Pending change (`changes/<name>/`) | a milestone in the repo's plan **plus** a `status: draft` capability holding its criteria (draft = exempt from the test contract until implemented) and its design decisions in `design.md` **plus** a durable `imported-change` record (`docs/imported-changes/`) pinning the source change's origin, intent, design rationale, dependency links, and a task-to-criterion proof-links table; extraction does not delete an incomplete pending change's in-flight work until its `imported-change` record's status reaches `complete` — `clue-delta` still regenerates the target's own `tasks.md` once implementation starts, but the source task graph and proof links survive in the record instead of dying with the source |
| `changes/archive/…` | git history only — no corpus artifact |
| Nygard/MADR ADRs in `docs/decisions` | see [madr.md](madr.md) for the conversion — MADR is a source format on its own, not an OpenSpec detail |
| Architecture docs | `docs/architecture/` artifacts (`status: draft` until reviewed) or capability `design.md` where they are capability-local |
| AC registry / scenario templates (`test/…`) | deleted — the corpus is the registry; next free ID per prefix is max + 1 over declared ACs |
| Project README purpose statements | `G-xxx` goal(s), `status: accepted` (the repo's existence is the acceptance) |
| Coverage/quality gates in build config | `C-xxx` constraints (`enforcement: machine`) referencing the enforcing tool |
| OpenSpec workflow skills (`openspec-*`) | deleted with the source corpus |
| Hand-maintained per-folder index (`INDEX.md`, `index.md`, a `## Contents` table) | absorbed into that folder's README `clue:index` rows, then deleted — the description, purpose, or scope column becomes the row's sentence, while a column that only restates a `links:` edge (a design or related-decision pointer) is dropped rather than carried |
| JUnit `@Tag("XX_NNN")` | keep on the executable together with its literal proof-type and direction tags — clue normalizes underscores to canonical hyphens at harvest for segmented or letter-suffixed IDs too. A method carrying several AC tags or a class-level AC tag is a rehearsal finding, never a positional deduplication or removal: after human review, split it into one-criterion methods, move a class tag only to the executable that proves it, or record the source location and named plan door with the explicit `@draft` disposition that applies when attributable test work is out of scope |
| Runner/type tags (`UNIT`, `INTEGRATION`, `E2E`, …) | kept untouched even when no pipeline filters on them yet — they are the runner's namespace, not the methodology's, and the only per-method type carrier where one file mixes test types |
| `openspec/config.yaml`'s revision (or the source repository's pinned commit) plus its path | the rehearsal's source manifest `source-revision`/`source-location`; each scenario's `Test-type` line, direction, and test file location seed its manifest entry's `proof-class`, `direction`, and `evidence-location` — a scenario the rehearsal resolves as `@draft`, `Human`, or retired records `disposition`, readable `justification`, its particular `disposition-source-location`, and its own existing target milestone as `plan-door` instead |

Preserve every existing local or external Markdown link and every referenced asset, including SVG diagrams. When a source link has a deterministic converted target, rewrite the target while preserving the link; when it does not, report the mapping gap and do not delete the source target. Diagram choice follows C-007: prefer embedded Mermaid, use embedded ASCII art when it is clearer, and retain SVG where neither is adequate.

## Carrier inventory

The carrier inventory needs one row per operational carrier the rehearsal finds; an OpenSpec source's usual carriers map like this:

| OpenSpec carrier | `kind` | Typical target |
|---|---|---|
| `openspec/AGENTS.md` (or a per-tool equivalent such as `CLAUDE.md`) | `instruction` | the repository's `AGENTS.md` routing hub |
| `.github/workflows/*.yml` running OpenSpec's own checks | `workflow` | the repository's `clue validate`/`clue carriers`/`clue parity` CI workflow |
| `openspec/config.yaml`'s pinned revision, a lockfile, or a last-checked date a CI job reads | `freshness-input` | the corresponding `clue`-managed pin, or `blocked` when no equivalent exists yet |
| A hand-maintained per-folder index or `INDEX.md` | `registry` | the absorbing folder's README `clue:index` block (see the target contract's link-preservation item — the index row itself is not a separate carrier once absorbed) |
| Every local or external Markdown link the source corpus carries | `link` | its rewritten target in the converted corpus, or `blocked` when the mapping gap is still open |
| An SVG, embedded Mermaid, or ASCII diagram the source references | `diagram-asset` | its retained or converted location, following the diagram-choice rule above |

A carrier with no target yet — an instruction file whose routing has no Cliewen equivalent, a freshness check with nothing to pin against — is recorded `blocked: true` with a `reason`; extraction does not delete the source path a `blocked` entry names until a later inventory revision converts it to a mapped entry. `clue carriers <inventory> [root]` reconciles the inventory the rehearsal wrote against the converted corpus the same way `clue parity` reconciles the source manifest.

Watch for: the same logical ID written three ways (`[MG-010]`, `` `PG-001` ``, `MG_010`); `## ADDED/MODIFIED Requirements` delta headers in pending changes (apply the delta meaning, don't copy the header); scenario WHEN/THEN bullets mapping to Gherkin When/Then/And.
