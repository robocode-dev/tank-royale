---
id: ADR-047
type: decision
status: verified
links: [CAP-014, CAP-015, CAP-016, ARCH-022, P-003]
title: Rumble catalog publishes immutable team membership
author: agent
accepted-by: Flemming N. Larsen (2026-08-30, Codex conversation)
---

# ADR-047 — Rumble catalog publishes immutable team membership

## Context

The V1 engine pin counts the four bot processes in a TwinDuel battle, while Battle Runner starts two team entries and result ingestion receives two team results. The catalog previously identified only individual entry fields, so a client could not distinguish two valid teams from four unrelated bots before starting untrusted code. ARCH-022 already defines a TwinDuel team as an entry with exactly two `teamMembers`.

## Decision

The generated bot catalog publishes `teamMembers` as immutable catalog identities. Individual entries use an empty list. A TwinDuel team names exactly two distinct active individual entries, and catalog generation rejects missing, duplicate, inactive, unknown, or nested team members. Readers treat an absent field as an empty list for schema-version-one compatibility.

Ranked TwinDuel selection chooses two distinct active team entries and verifies that their expanded member count equals the engine pin. Cache preparation obtains and verifies the team entry and every member source tree before Battle Runner starts them. Individual game types select only entries without team members.

## Consequences

Selection, source preparation, execution identity, and result validation share one explicit team model. Existing individual-only catalogs remain readable, but they cannot satisfy TwinDuel selection until team entries are published.
