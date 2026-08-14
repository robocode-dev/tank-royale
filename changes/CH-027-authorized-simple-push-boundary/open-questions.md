---
id: CH-027-open-questions
type: open-questions
status: open
links: [CH-027]
title: Open questions for CH-027
---

# CH-027 — Open questions

## Q1 — Which CH identity, given that the ledger and the corpus disagree? (answered)

`clue id next CH` returned CH-027 only after a repair. The ledger's `CH` counter stood at `1`, so the command offered CH-002, which `docs/decisions/log.md`, `docs/capabilities/CAP-013-typescript-bot-api-npm-publish/criteria.md`, and that capability's `design.md` already bind to the TypeScript npm publishing change. The corpus documents CH-001 through CH-026, so the ledger, not the corpus, was wrong: CH-002 through CH-026 were never registered after CH-001.

**Answer (maintainer, 2026-08-14):** use CH-027, the next identity free in the corpus, and repair the counter to 26 before allocating, so the ledger now records CH-027 and the next allocation is CH-028. The intermediate identities stay unregistered; their change workspaces were deleted at digest and the corpus references are the record.
