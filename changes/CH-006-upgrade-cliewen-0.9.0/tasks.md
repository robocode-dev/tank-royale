---
id: CH-006-tasks
type: change
status: active
links: [CH-006]
title: Tasks for CH-006
---

# CH-006 — Tasks

- [x] Re-vendor the five managed skills from the Cliewen v0.9.0 release tree, including the new `clue-extract/mappings/madr.md`
- [x] Bump `CLUE_VERSION` in `.github/workflows/clue.yml` to `0.9.0`
- [x] Restore change-scope detection in the wall, keeping the direct verified download and adding `fetch-depth: 0`
- [x] Restore the acceptance-brief gate in the wall, and add the `edited` pull-request trigger it needs
- [x] Add the shipped pull-request template so the brief the gate requires has a source
- [x] Set `reversal-cost` on the inferred artifacts whose reversal is cheap and local: 65 records — draft criteria and designs, descriptive architecture views and flows, message-schema notes, plans, guides, and the extraction analysis
- [x] Set `reversal-cost: high` on C-003 and C-004, whose promises (cross-platform parity, stable physics and bot backward compatibility) are not cheap to reverse
- [x] Move AN-001 off the retired `status: verified` to `status: active`
- [ ] Blocked on the open question: classify or verify G-001 — the last remaining validation issue
- [ ] Run the full local check set once G-001 is resolved: `clue validate --forbid-changes`, Gradle build, `git diff --check`
