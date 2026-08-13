---
id: C-001
type: constraint
status: active
links: []
title: Markdown prose is never hard-wrapped
source: Cliewen methodology (adopted at CH-001)
enforcement: machine
---

# C-001 — Markdown prose is never hard-wrapped

One line per paragraph and per list item; wrapping is the reader's IDE concern. Line breaks are structural only: headings, lists, tables, code fences.

Applies to new and rewritten prose in this corpus and the agent-instruction layer. Documents that predate CH-001 (the converted ADRs, architecture and design docs) keep their original wrapping until their content is next touched — conversion churn is not a change.

**Enforcement:** `clue validate` flags mid-paragraph line breaks in `docs/**` Markdown.
