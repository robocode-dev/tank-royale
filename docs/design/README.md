---
type: index
title: Design documents
---

# Design documents

Detailed design specifications that complement the decision records in [../decisions/](../decisions/README.md): ADRs stay focused on the decision, context, and rationale; the specs, flow details, diagrams, and examples that would bloat an ADR live here and are linked from the ADR's "More Information".

The [Rumble umbrella design](rumble/README.md) maps the serverless community tournament across the client, bot catalog, result-data repository and dashboard. Its design directions are read alongside the accepted capability contracts and decisions; the original implementation plan is complete, and the [replacement participation campaign](../plans/P-007-rumble-participation.md) retains its completed evidence while planning simpler public-Beta journeys and isolated development testing. The [target participation design](rumble/participation.md) describes future shared modules, account boundaries, withdrawal and readable bot pages; it does not claim those features have shipped. The [Rumble menu mockups](rumble/gui-mockups.md) draft the planned desktop screens, their waiting and failure states, and the participant journeys. The accepted [browser-form submission decision](../decisions/ADR-049-rumble-browser-form-submission.md) makes the contributor responsible for posting complete prepared batches while the client holds no submission credential. The [client flow](rumble/client-battles-and-results.md) and [ingestion flow](rumble/aggregation-and-dashboard.md) describe that handoff and publication-first receipts. Bot-source review follows the [official API runtime-dependency boundary](../decisions/ADR-050-official-bot-api-runtime-dependencies.md), while the client container provides execution isolation; the [catalog capability design](../capabilities/CAP-014-rumble-bot-catalog/design.md) owns starter and validation details.

Capability-local design (how one capability works) belongs in that capability's `design.md` under [../capabilities/](../capabilities/README.md); records here cover cross-cutting design.

Documentation publishing keeps permanent and generated material on opposite sides of an explicit boundary: `/docs` contains only the Cliewen corpus, user-facing sources live under `web/docs`, and the disposable GitHub Pages tree is assembled under `build/pages`. See [CAP-012 design](../capabilities/CAP-012-user-documentation/design.md) and [PDR-014](../decisions/PDR-014-generated-documentation-outside-corpus.md).

<!-- clue:index:start -->
- [ARCH-020 — Real-Time Game Loop — Design Specification](game-loop-architecture.md) · `draft`
- [ARCH-021 — WebSocket Protocol — Design Specification](websocket-protocol.md) · `draft`
- [ARCH-022 — Rumble Design: Bot Submission and Handling](rumble/bot-submission.md) · `draft`
<!-- clue:index:end -->
