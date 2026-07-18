# The Tank Royale corpus

This directory is the **system-of-record**: the permanent, durable truth about the platform for developers working on it (user-facing docs live in `web/`), run under [Cliewen](https://github.com/cliewen/cliewen) conventions since CH-001 (2026-07-18, see [AN-001](analysis/AN-001-openspec-extraction.md)). Changes are transient deltas on branches, digested into this corpus at merge — `git log docs/` is the audit trail.

Every artifact carries YAML frontmatter (`id`, `type`, `status`, `links`, `title`); identity is the ID, the path is only the current address. Two optional fields: `ac-prefix` on a criteria.md namespaces its AC IDs (this repo mints per-capability prefixes: BR, BFD, BAU, GBP, GBC, PRO, PBA, RCV, TCS, TFS, TBA, UD, TNP), and `provenance: inferred|verified` marks extracted artifacts awaiting human verification. `clue validate` enforces the graph. Test purpose tags (AC ↔ test) are not yet wired — that is milestone M-002, a named door.

The red thread: G-xxx → P-xxx/M-xxx → CH-xxx → CAP-xxx → AC (namespaced) → test tag.

## Folders

<!-- clue:index:start -->
- [goals/](goals/README.md) — G-xxx: who wants it, why (proposed goals are the inbox)
- [plans/](plans/README.md) — P-xxx: campaign layer with verifiable milestones
- [capabilities/](capabilities/README.md) — CAP-xxx: one folder per capability (README / criteria / design)
- [architecture/](architecture/README.md) — system scope: C4 views, protocol flows, message schemas
- [design/](design/README.md) — cross-cutting design documents and drafts
- [decisions/](decisions/README.md) — ADR-xxxx: decisions with provenance, including superseded ones
- [constraints/](constraints/README.md) — C-xxx: laws, licenses, non-negotiable product rules
- [quality/](quality/README.md) — QS-xxx: quality scenarios (verifiable NFRs)
- [analysis/](analysis/README.md) — spike findings and extraction reports
- [ARCH-026 — Debugging Guide](DEBUGGING-GUIDE.md) · `draft` — developer manual, rides along at this level
<!-- clue:index:end -->
