---
id: ADR-052
type: decision
status: verified
links: [G-004, P-007, ARCH-027, CAP-014, CAP-015, CAP-016]
title: Share desktop and headless Rumble logic while isolating development and untrusted execution
author: agent
accepted-by: Flemming N. Larsen (2026-10-27, Codex conversation)
---

# Share desktop and headless Rumble logic while isolating development and untrusted execution

## Context

The executable Rumble client lives outside the Tank Royale build, while the GUI already assembles the server, booter, and recorder. Separate command sequences and manual GitHub submission expose infrastructure to participants and complicate testing. Reviewed bot source remains untrusted code.

## Decision

Consolidate existing client implementation into the Tank Royale build, retaining its working code rather than translating it. Introduce a shared Rumble library, a headless controller/worker executable, and bot-project tooling. Extend the existing Swing GUI with a Rumble menu. The [architecture overview](../architecture/README.md) distinguishes current deployment from this target; the [participation design](../design/rumble/participation.md) owns boundaries and flows.

Host-side control owns GitHub authorization, synchronization, and uploads. Container workers receive pinned battle inputs and no GitHub credentials or general host access. GUI and CLI use the same session implementation and structured subprocess contracts. GitHub remains the review and result infrastructure; do not add a hosted application service under this decision.

Keep the existing public Rumble and add separately configured development catalog/results repositories, App installations, state, and dashboard. Test reset must fail closed for public targets. Share workflow implementation rather than maintaining a divergent test product.

## Consequences

Device authorization with narrowly scoped user permissions is the intended authentication direction, not an established fact. The early outside-account experiment must prove it before implementation depends on it. The existing browser-form and credential-boundary contracts remain operational until their replacement changes supply proof and retire the affected criteria and decisions. If a hosted service or broader credential is necessary, stop the dependent path for a new decision.

Catalog and result repositories remain separate. The old client repository becomes a migration entry point only after replacement artifacts and non-destructive state migration are verified. Windows is the first full acceptance platform; portable interfaces and existing supported CLI paths are preserved without claiming new Linux or macOS end-to-end verification.
