---
id: LOG-001
type: log
status: active
links: []
title: Decision log
---

# Decision log

Decisions that are cheap and local to reverse, one row each — newest first. The cost of undoing a decision is the sole routing test: cheap and local to reverse → a row here; expensive to reverse → a full ADR. Rows are never deleted; a reversed decision gets a new row, and a row whose reversal turns out to be expensive after all is promoted to a full record citing this table.

| Date | Decision | Why | Change/PR |
|---|---|---|---|
| 2026-07-19 | Extracted criteria are born `status: draft`; a capability's criteria go `active` when its tests carry purpose tags | Draft criteria are exempt from the AC↔test wall, which turns the four-language tagging backlog (P-001/M-002) into per-capability increments instead of one blocking big bang | CH-001 |
| 2026-07-19 | Pre-Cliewen ADRs keep ids ADR-0001…ADR-0041 and become `verified` with `accepted-by: … (date, pre-Cliewen MADR acceptance)` | The MADR acceptance already happened and is preserved as fact, not re-judged; renumbering 41 cross-referenced records would break links for no gain | CH-001 |
| 2026-07-19 | `INDEX.md` files and `docs/decisions/template.md` deleted; README `clue:index` blocks are the only indexes | Two indexes drift; the validate-checked index block is the enforced one | CH-001 |
| 2026-07-19 | `docs/design/*` records are typed `architecture` (ARCH-xxx), including the debugging guide and health reports | The corpus vocabulary has one record type for system-description documents; a separate type per folder adds vocabulary without adding meaning | CH-001 |
| 2026-07-19 | The empty `browser-sample-bots` OpenSpec dir produced no capability; prefix BSB stays unregistered | An empty spec carries no requirements to preserve; a hollow CAP folder would be noise | CH-001 |
