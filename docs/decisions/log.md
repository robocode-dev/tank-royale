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
| 2026-08-14 | Amend C-002: full changes keep the absolute prohibition on direct `main` mutation, while simple work may be pushed directly when the maintainer explicitly authorizes that push for that specific change, recorded in an `Authorized-Push:` commit trailer | The blanket prohibition adopted at CH-001 was stricter than Cliewen's own boundary, which already gates only full changes and leaves simple work to explicit user integration authority; the protection C-002 exists to give is unaffected because accepted-contract change is exactly what stays on the full route, and the trailer keeps a human-enforced permission auditable | CH-027 |
| 2026-08-14 | Repair the ledger's `CH` counter from `1` to the corpus's own history and register CH-027, leaving CH-002..CH-026 unregistered | `clue id next CH` was minting identities the corpus already binds to earlier changes; the corpus is the registry, and the intermediate workspaces were deleted at digest so their corpus references are the record | CH-027 |
| 2026-08-04 | Make `VERSION` the release-skill version source and verify documentation locally without a direct `main` mutation | The Gradle build already takes its version from `VERSION`; generated Pages output is intentionally untracked and `deploy-docs.yml` builds it from accepted `main`, while C-002 prohibits direct commits and pushes | CH-013 |
| 2026-08-04 | Resume and complete M-008 only after the official Tank Royale 1.1.0 release | The Rumble Client must target a published, immutable engine distribution rather than unreleased source or a partial runtime contract | CH-013 |
| 2026-08-04 | Move M-010 before M-009 and ship GUI TwinDuel selection in Tank Royale 1.1.0 | The GUI is the primary product experience, so TwinDuel must be complete where most users start battles instead of remaining a server-only Rumble feature | CH-013 |
| 2026-08-02 | Schedule GUI TwinDuel selection as M-010 after the Rumble repositories, client, and documentation | The preset exists after CH-009, but the maintainer wants the GUI follow-up after the Rumble foundation is in place | CH-010 |
| 2026-07-23 | The Rumble design roadmap becomes active plan P-003 (M-005..M-009), one milestone per roadmap change | The umbrella design names this as its next step; future rumble proposals need a plan item to serve | CH-005 |
| 2026-07-19 | Drop the never-built `npmPublishDryRun` task from CAP-013 (retire TNP-001) instead of building it | `npmPack` already produces the `.tgz` and shows what would be published without uploading, so a dry-run task is redundant | CH-002 |
| 2026-07-19 | TypeScript npm publishing authenticates via the `npmjs-api-key` Gradle property, not an `NPM_TOKEN` env var (retire TNP-002/003, remint TNP-005/006) | Reconciles the corpus to the shipped code and matches the Sonatype/PyPI credential pattern and the `/release` skill | CH-002 |
| 2026-07-19 | Extracted criteria are born `status: draft`; a capability's criteria go `active` when its tests carry purpose tags | Draft criteria are exempt from the AC↔test wall, which turns the four-language tagging backlog (P-001/M-002) into per-capability increments instead of one blocking big bang | CH-001 |
| 2026-07-19 | Pre-Cliewen ADRs keep ids ADR-0001…ADR-0041 and become `verified` with `accepted-by: … (date, pre-Cliewen MADR acceptance)` | The MADR acceptance already happened and is preserved as fact, not re-judged; renumbering 41 cross-referenced records would break links for no gain | CH-001 |
| 2026-07-19 | `INDEX.md` files and `docs/decisions/template.md` deleted; README `clue:index` blocks are the only indexes | Two indexes drift; the validate-checked index block is the enforced one | CH-001 |
| 2026-07-19 | `docs/design/*` records are typed `architecture` (ARCH-xxx), including the debugging guide and health reports | The corpus vocabulary has one record type for system-description documents; a separate type per folder adds vocabulary without adding meaning | CH-001 |
| 2026-07-19 | The empty `browser-sample-bots` OpenSpec dir produced no capability; prefix BSB stays unregistered | An empty spec carries no requirements to preserve; a hollow CAP folder would be noise | CH-001 |
