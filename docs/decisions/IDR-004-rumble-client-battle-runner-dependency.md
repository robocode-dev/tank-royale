---
id: IDR-004
type: decision
status: verified
links: [CAP-016, ADR-024, ADR-046]
title: Rumble client uses the released Battle Runner dependency
author: agent
accepted-by: Flemming N. Larsen (2026-08-29, Codex conversation)
---

# IDR-004 — Rumble client uses the released Battle Runner dependency

## Context

CAP-016 requires the Rumble client to execute pinned battles through the Battle Runner API. The client repository currently has no Runner dependency, and its contribution rules require explicit maintainer approval before adding one.

## Decision

During development, `robocode-dev/rumble-client` may consume the merged BR-049 implementation from a local Tank Royale build. Before the client is distributed for ranked use, it pins the first released `dev.robocode.tankroyale:robocode-tank-royale-runner` version that provides the BR-049 behavior-version precondition.

## Consequences

Local end-to-end development can continue without forcing a Tank Royale release that offers no end-user value. Distributed clients still use an immutable released public API for server lifecycle, bot processes, compatibility enforcement, full-round execution, results, and replay recording. Runner upgrades become explicit client dependency updates and ranked compatibility remains governed by the synchronized behavior version rather than inferred from the dependency version.
