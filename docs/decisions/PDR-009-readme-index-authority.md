---
id: PDR-009
type: decision
status: inferred
links: [P-001]
title: README index blocks are the canonical corpus indexes
author: agent
accepted-by: []
---

# PDR-009 — README index blocks are the canonical corpus indexes

## Context

Parallel `INDEX.md` files and template indexes could drift from the index blocks checked by validation.

## Decision

README `clue:index` blocks are the sole generated corpus indexes; standalone `INDEX.md` files and the decision template are not retained.

## Consequences

Index ownership is unambiguous and the validator checks the same representation that contributors read.
