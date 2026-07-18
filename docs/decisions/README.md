# Decisions

ADR-xxxx: architecture decision records — the context, alternatives, and rationale behind architectural choices. They answer: **"Why did we design it this way?"**

Each ADR keeps [Michael Nygard's](http://thinkrelevance.com/blog/2011/11/15/documenting-architecture-decisions) core **Context**, **Decision**, and **Consequences** sections, extended with **Rationale**, **Alternatives Considered**, **Future Work**, and **References**. Since CH-001 the frontmatter follows the clue decision schema: `status: inferred` when a record is written by an agent and merged (merge ≠ approval), promoted to `verified` when a human explicitly stands behind it (`accepted-by:` records who, when, and in what context). ADR-0001…ADR-0041 predate Cliewen and were accepted under the MADR workflow; that acceptance is recorded in their `accepted-by` field.

New decisions are born inside a change (`/changes/CH-xxx/`), numbered sequentially, product-centered (no tooling/process ADRs), and added to the index below.

Writing guidelines: be specific (technical detail, not concepts); show your work (rejected alternatives and why); be honest (negative consequences too); use Mermaid diagrams where they clarify; link extensively to code, capabilities, and other records.

<!-- clue:index:start -->
- [ADR-0001 — Monorepo Build Strategy](0001-monorepo-build-strategy.md) · `verified`
- [ADR-0002 — Standard Mathematical Coordinate System](0002-standard-math-coordinate-system.md) · `verified`
- [ADR-0003 — Cross-Platform Bot API Strategy](0003-cross-platform-bot-api-strategy.md) · `verified`
- [ADR-0004 — Java as Authoritative Reference Implementation](0004-java-reference-implementation.md) · `verified`
- [ADR-0005 — Independent Deployable Components](0005-independent-deployable-components.md) · `verified`
- [ADR-0006 — Schema-Driven Protocol Contracts](0006-schema-driven-protocol-contracts.md) · `verified`
- [ADR-0007 — Client Role Separation (Bot / Observer / Controller)](0007-client-role-separation.md) · `verified`
- [ADR-0008 — Server-Authoritative Deterministic Physics](0008-server-authoritative-physics.md) · `verified`
- [ADR-0009 — WebSocket Communication Protocol](0009-websocket-communication-protocol.md) · `verified`
- [ADR-0010 — Declarative Bot Intent Model](0010-declarative-bot-intent-model.md) · `verified`
- [ADR-0011 — Real-Time Game Loop Architecture](0011-realtime-game-loop-architecture.md) · `verified`
- [ADR-0012 — Turn Timing Semantics](0012-turn-timing-semantics.md) · `verified`
- [ADR-0013 — Bot Configuration via Environment Variables](0013-bot-configuration-env-vars.md) · `verified`
- [ADR-0014 — Two-Tier Shared-Secret Authentication](0014-two-tier-authentication.md) · `verified`
- [ADR-0015 — Participant ID as Unified Team Identifier](0015-bot-id-team-id-namespace-separation.md) · `verified`
- [ADR-0016 — Session ID for Bot Process Identification](0016-session-id-bot-process-identification.md) · `verified`
- [ADR-0017 — Recording Format (ND-JSON + Gzip)](0017-recording-format.md) · `verified`
- [ADR-0018 — Custom SVG Rendering for Bot API Graphics](0018-custom-svg-rendering.md) · `verified`
- [ADR-0019 — R8 Code Shrinking](0019-r8-code-shrinking.md) · `verified`
- [ADR-0020 — Teams Support in Observer Protocol](0020-teams-support-observer-protocol.md) · `verified`
- [ADR-0021 — Java Swing as GUI Reference Implementation](0021-java-swing-gui-reference-implementation.md) · `verified`
- [ADR-0022 — Event System for GUI Decoupling](0022-event-system-gui-decoupling.md) · `verified`
- [ADR-0023 — Robocode Tank Royale Platform Scope and Boundaries](0023-robocode-tank-royale-platform-scope.md) · `verified`
- [ADR-0024 — Battle Runner API](0024-battle-runner-api.md) · `verified`
- [ADR-0025 — Game Type Presets and Rule Configuration](0025-game-type-presets-and-rule-configuration.md) · `verified`
- [ADR-0026 — Identity-Based Bot Matching in Battle Runner](0026-identity-based-bot-matching.md) · `verified`
- [ADR-0027 — TypeScript Bot API for Web Platform Support](0027-typescript-bot-api-architecture.md) · `verified`
- [ADR-0028 — TypeScript Bot API Threading Model](0028-typescript-bot-api-threading-model.md) · `verified`
- [ADR-0029 — TypeScript Bot API Runtime Targets](0029-typescript-bot-api-runtime-targets.md) · `verified`
- [ADR-0030 — Template-based Booting and Base Convention](0030-convention-over-configuration-bot-entry-points.md) · `verified`
- [ADR-0031 — Optional Bot Configuration Files and Runtime Property Validation](0031-optional-bot-config-and-runtime-validation.md) · `verified`
- [ADR-0032 — Tank Color Display Mode](0032-user-defined-visual-overrides-for-tanks.md) · `verified`
- [ADR-0033 — Server Debug Mode](0033-bot-debug-mode.md) · `verified`
- [ADR-0034 — Breakpoint Mode](0034-breakpoint-mode.md) · `verified`
- [ADR-0035 — Bot API Debugger Detection](0035-bot-debugger-detection.md) · `verified`
- [ADR-0036 — Start-Game Debug Options](0036-start-game-debug-options.md) · `verified`
- [ADR-0037 — Functional Core Extraction for Bot API Testability](0037-functional-core-bot-api-testability.md) · `verified`
- [ADR-0038 — Cross-Platform Test Parity and Shared Test Definitions](0038-shared-cross-platform-test-definitions.md) · `verified`
- [ADR-0039 — Server Testability — Physics Core Extraction and Test Framework](0039-server-testability.md) · `verified`
- [ADR-0040 — Raise Default readyTimeout from 1 Second to 10 Seconds](0040-ready-timeout-default.md) · `verified`
- [ADR-0041 — Bot API Library Version Management in the GUI](0041-bot-api-library-version-management.md) · `verified`
<!-- clue:index:end -->
