---
id: IDR-003
type: decision
status: inferred
links: []
title: The corpus identity ledger is repaired from its own history
author: agent
accepted-by: []
---

# IDR-003 — The corpus identity ledger is repaired from its own history

## Context

The `CH` counter had drifted from the change identities already bound by the corpus, causing the allocator to reserve an identity that was already in use.

## Decision

The ledger counter is aligned with the corpus history and CH-027 is registered while the unregistered intermediate change identities remain absent.

## Consequences

Future `clue id next CH` allocations start after the corpus's own history, and the repair is represented by the ledger rather than a recurring decision-log row.
