# clue-extract mapping: OpenSpec

Source mapping for [clue-extract](../skill.md) — the target contract in `skill.md` governs; this file only says what maps where for an OpenSpec corpus.

Layout: `openspec/config.yaml`, synced truth in `openspec/specs/<capability>/spec.md`, pending work in `openspec/changes/<name>/` (proposal.md, design.md, tasks.md, spec deltas), applied work in `openspec/changes/archive/`.

| OpenSpec | Cliewen |
|---|---|
| `specs/<cap>/spec.md` | `docs/capabilities/CAP-xxx-<cap>/` — one capability per spec file |
| Spec header + purpose prose | capability `README.md` (what/why, `goal:` link) |
| `### Requirement:` + SHALL + `#### Scenario: <name> [ID]` | Gherkin scenarios in `criteria.md`, tag line `@<ID>` — keep the ID, whatever bracket/backtick notation the source used |
| `Test-type:` line per scenario | plain body text inside the Gherkin scenario (per-AC required-types enforcement is a deliberate door, not yet built) |
| Scenario ID prefix (e.g. `MG`) | `ac-prefix:` in that criteria.md frontmatter; a delta spanning several prefixes splits into one capability per prefix |
| Pending change (`changes/<name>/`) | a milestone in the repo's plan **plus** a `status: draft` capability holding its criteria (draft = exempt from the test contract until implemented) and its design decisions in `design.md`; its tasks.md dies — `clue-delta` regenerates tasks when implementation starts |
| `changes/archive/…` | git history only — no corpus artifact |
| Nygard/MADR ADRs in `docs/decisions` | `ADR-xxx` born `status: inferred`, `author: agent`; original acceptance dates preserved in the body |
| Architecture docs | `docs/architecture/` artifacts (`status: draft` until reviewed) or capability `design.md` where they are capability-local |
| AC registry / scenario templates (`test/…`) | deleted — the corpus is the registry; next free ID per prefix is max + 1 over declared ACs |
| Project README purpose statements | `G-xxx` goal(s), `status: accepted` (the repo's existence is the acceptance) |
| Coverage/quality gates in build config | `QS-xxx` quality scenarios referencing the enforcing tool |
| OpenSpec workflow skills (`openspec-*`) | deleted with the source corpus |
| JUnit `@Tag("XX_NNN")` | untouched — clue normalizes underscores to hyphens at harvest |
| Runner/type tags (`UNIT`, `INTEGRATION`, `E2E`, …) | kept untouched even when no pipeline filters on them yet — they are the runner's namespace, not the methodology's, and the only per-method type carrier where one file mixes test types |

Watch for: the same logical ID written three ways (`[MG-010]`, `` `PG-001` ``, `MG_010`); `## ADDED/MODIFIED Requirements` delta headers in pending changes (apply the delta meaning, don't copy the header); scenario WHEN/THEN bullets mapping to Gherkin When/Then/And.
