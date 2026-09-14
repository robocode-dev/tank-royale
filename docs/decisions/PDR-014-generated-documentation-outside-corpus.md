---
id: PDR-014
type: decision
status: verified
links: [PDR-002, CAP-012]
title: Generated documentation stays outside the corpus
author: agent
accepted-by: Flemming N. Larsen (2026-09-14, Codex conversation)
---

# PDR-014 — Generated documentation stays outside the corpus

## Context

Repository-root `/docs` is the permanent Cliewen system-of-record. Using that same tree to stage generated VitePress pages and API references makes disposable build output look like corpus content and causes local documentation generation to invalidate corpus checks.

## Decision

All generated documentation is staged under the root build directory. `/docs` is never a documentation-generation target; user-facing sources remain under `web/docs`, and CI publishes the generated staging tree without copying it into the corpus.

## Consequences

Generated documentation is disposable with the rest of the build tree, corpus validation sees only permanent artifacts, and local verification uses the same Pages layout that CI publishes.

## More information

See the [cross-cutting design overview](../design/README.md) and [CAP-012 design](../capabilities/CAP-012-user-documentation/design.md).
