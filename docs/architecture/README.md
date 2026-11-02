---
type: index
title: Architecture
---

# Architecture

ARCH-xxx: how the system is structured — C4 views (What), domain models and flows (How), with the decision records in [decisions/](../decisions/README.md) carrying the Why.

Layout: [c4-views/](c4-views/README.md) holds the hierarchical diagrams (system context → containers → components) with Structurizr DSL sources and generated SVGs; [models/message-schema/](models/message-schema/README.md) documents the WebSocket message contracts (source of truth: `/schema/schemas/`); [models/flows/](models/flows/README.md) documents the processes (battle lifecycle, bot connection, turn execution, event handling); [report/](report/README.md) holds architectural health reports produced by the audit skill.

New readers: start with the [System Context](c4-views/system-context.md), then the [Container view](c4-views/container.md), then the flow or schema closest to what you are changing. When structure, protocol, or processes change, update the matching record in the same change that alters the code.

For Rumble, the [cross-repository design overview](../design/rumble/README.md) locates the catalog, client, result inbox and ranking projections. The accepted [browser-form submission boundary](../decisions/ADR-049-rumble-browser-form-submission.md) keeps GitHub sign-in in the contributor's browser: the client prepares complete batches and reads public receipts without holding a submission credential. The [client submission flow](../design/rumble/client-battles-and-results.md) and [ingestion flow](../design/rumble/aggregation-and-dashboard.md) own the detailed interactions; the [client capability](../capabilities/CAP-016-rumble-client/design.md) identifies the outstanding evidence for this replacement path.

The [Rumble participation architecture decision](../decisions/ADR-052-rumble-desktop-and-test-environments.md) selects shared desktop/headless modules and separate public/development environments as the target. Its [detailed boundary design](../design/rumble/participation.md) describes credential-free workers and the authentication feasibility gate. These are planned changes; the current client boundary above remains operational until its implementing changes are accepted. The [Rumble menu mockups](../design/rumble/gui-mockups.md) draft the planned desktop screens and the participant journeys. The first shared modules already exist in the build but are unpublished and show nothing to users: `lib:rumble` (the Rumble environments, reading of the bot catalog with hash-verified fetching, GitHub sign-in with a code, and a saved sign-in shared safely between programs) and `lib:bot-project` (inspecting and validating a bot folder against the bot repository's rules). A third, `lib:rumble-protocol`, holds the versioned messages between the host and the battle worker, and the executable `rumble` has a worker mode that runs one pinned battle on the battle runner (see the [battle worker capability](../capabilities/CAP-021-rumble-battle-worker/README.md)). The shared libraries may not depend on the desktop program or the battle runner, the worker may depend only on the runner and the messages, and the build checks that; the [module dependency rules](../design/rumble/participation.md) say which module may use which.

All records below were authored pre-Cliewen and absorbed at CH-001 (`status: draft`, `provenance: inferred`); they are promoted to `verified` as they are checked against the code.

<!-- clue:index:start -->
- [ARCH-001 — System Context Diagram](c4-views/system-context.md) · `draft`
- [ARCH-009 — Battle Lifecycle Flow](models/flows/battle-lifecycle.md) · `draft`
- [ARCH-018 — Architectural Health Report](report/architectural-health-report.md) · `draft`
<!-- clue:index:end -->
