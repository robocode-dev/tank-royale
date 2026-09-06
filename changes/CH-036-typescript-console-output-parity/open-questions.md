---
id: CH-036-open-questions
type: open-questions
status: draft
links: [CH-036-proposal]
title: Open questions for CH-036 (TypeScript console-output parity)
provenance: inferred
reversal-cost: low
---

# CH-036 open questions

## OQ-001 — Unmerged CH-035 base

**Question:** May CH-036 build on CH-035 before CH-035 is accepted into `main`?

**Answer:** Yes. The user request to “start on M-018” on 2026-09-06 authorizes this dependent change on open PR #259 at commit `913544170e3b8a3494442a07e424b64509b9a8af`. CH-036 remains dependent on CH-035 and must not be integrated unless CH-035 is accepted first; accepting CH-036 then binds the parity evidence and P-005 digest on top of that implementation.
