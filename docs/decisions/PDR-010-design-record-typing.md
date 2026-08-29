---
id: PDR-010
type: decision
status: inferred
links: [P-001]
title: Design records use the architecture type
author: agent
accepted-by: []
---

# PDR-010 — Design records use the architecture type

## Context

The corpus needs one vocabulary for documents that describe system structure, including the debugging guide and health reports.

## Decision

Records under `docs/design/` use the `architecture` type and ARCH identities rather than a separate design-specific type.

## Consequences

System-description documents share one validator vocabulary without adding a folder-specific identity system.
