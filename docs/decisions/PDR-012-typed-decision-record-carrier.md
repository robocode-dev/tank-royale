---
id: PDR-012
type: decision
status: inferred
links: []
title: Typed decision records replace the legacy decision log
author: agent
accepted-by: []
supersedes: [LOG-001]
---

# PDR-012 — Typed decision records replace the legacy decision log

## Context

The legacy decision log mixed architecture, process, implementation, and routine history in one carrier that obscured the enduring subject of each choice.

## Decision

Future-shaping choices use concise ADR, PDR, or IDR records selected by subject; routine history and migration narrative stay in their natural carriers, and the legacy log is retired.

## Consequences

The decisions index exposes the subject of each durable choice while the migration proposal and Git history retain the classification and repair trail.
