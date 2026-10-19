---
id: ADR-050
type: decision
status: inferred
links: [CAP-014, M-022, ARCH-022]
title: Source-only bots may use the official API's declared runtime dependencies
author: agent
accepted-by: []
---

# Official Bot API runtime dependencies

## Context

Ranked submissions use an official Tank Royale Bot API and the standard library. Official APIs themselves have runtime dependencies; the TypeScript API declares `ws` as a runtime peer and requires it to establish a Node connection. Excluding that peer would prevent the official source entry point from running while an early-exit smoke branch could still appear healthy.

## Decision

The official Bot API and the runtime dependencies declared by that API are permitted. A bot may declare an API-required runtime peer explicitly when its package manager does not install the peer automatically. Other bot libraries remain outside the allowlist. A peer is allowed because the official API declares it, not because the bot author labels it required.

Dependency declarations, lockfiles and launchers stay reviewable source inputs. Generated dependencies and compiled output remain outside the submitted source tree. Templates pin the API and required peers; changing that dependency set requires the same validation and moderator review as any source change. Startup smoke and real server-connected verification remain distinct evidence.

This preserves the review and containment split described in the [cross-cutting design overview](../design/README.md): catalog review constrains dependencies, and the client container provides execution isolation. It does not permit a custom protocol implementation or unrelated convenience library.
