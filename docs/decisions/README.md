# Decisions

Decision records use three subject types: ADR-xxx for software or corpus architecture, PDR-xxx for project, process, or methodology, and IDR-xxx for implementation choices. They answer: **"What future-shaping choice does this repository need to remember?"**

Each record keeps concise **Context**, **Decision**, and **Consequences** sections, adding alternatives only when they materially explain the choice. The frontmatter follows the Cliewen decision schema: `status: inferred` when a record is written by an agent and merged (merge ≠ approval), promoted to `verified` when a human explicitly stands behind it (`accepted-by:` records who, when, and in what context). ADR-0001…ADR-0041 predate Cliewen and were accepted under the MADR workflow; that acceptance is recorded in their `accepted-by` field.

New decisions are born inside a change (`/changes/CH-xxx/`), numbered sequentially within their subject series, and added to the index below. Routine facts, chronology, and implementation walkthroughs stay in their natural carriers rather than becoming decision records.

Writing guidelines: be specific (technical detail, not concepts); show your work (rejected alternatives and why); be honest (negative consequences too); use Mermaid diagrams where they clarify; link extensively to code, capabilities, and other records.

<!-- clue:index:start -->
- [ADR-0001 — Monorepo Build Strategy](ADR-0001-monorepo-build-strategy.md) · `verified`
- [ADR-0002 — Standard Mathematical Coordinate System](ADR-0002-standard-math-coordinate-system.md) · `verified`
- [ADR-0003 — Cross-Platform Bot API Strategy](ADR-0003-cross-platform-bot-api-strategy.md) · `verified`
- [ADR-0004 — Java as Authoritative Reference Implementation](ADR-0004-java-reference-implementation.md) · `verified`
- [ADR-0005 — Independent Deployable Components](ADR-0005-independent-deployable-components.md) · `verified`
- [ADR-0006 — Schema-Driven Protocol Contracts](ADR-0006-schema-driven-protocol-contracts.md) · `verified`
- [ADR-0007 — Client Role Separation (Bot / Observer / Controller)](ADR-0007-client-role-separation.md) · `verified`
- [ADR-0008 — Server-Authoritative Deterministic Physics](ADR-0008-server-authoritative-physics.md) · `verified`
- [ADR-0009 — WebSocket Communication Protocol](ADR-0009-websocket-communication-protocol.md) · `verified`
- [ADR-0010 — Declarative Bot Intent Model](ADR-0010-declarative-bot-intent-model.md) · `verified`
- [ADR-0011 — Real-Time Game Loop Architecture](ADR-0011-realtime-game-loop-architecture.md) · `verified`
- [ADR-0012 — Turn Timing Semantics](ADR-0012-turn-timing-semantics.md) · `verified`
- [ADR-0013 — Bot Configuration via Environment Variables](ADR-0013-bot-configuration-env-vars.md) · `verified`
- [ADR-0014 — Two-Tier Shared-Secret Authentication](ADR-0014-two-tier-authentication.md) · `verified`
- [ADR-0015 — Participant ID as Unified Team Identifier](ADR-0015-bot-id-team-id-namespace-separation.md) · `verified`
- [ADR-0016 — Session ID for Bot Process Identification](ADR-0016-session-id-bot-process-identification.md) · `verified`
- [ADR-0017 — Recording Format (ND-JSON + Gzip)](ADR-0017-recording-format.md) · `verified`
- [ADR-0018 — Custom SVG Rendering for Bot API Graphics](ADR-0018-custom-svg-rendering.md) · `verified`
- [ADR-0019 — R8 Code Shrinking](ADR-0019-r8-code-shrinking.md) · `verified`
- [ADR-0020 — Teams Support in Observer Protocol](ADR-0020-teams-support-observer-protocol.md) · `verified`
- [ADR-0021 — Java Swing as GUI Reference Implementation](ADR-0021-java-swing-gui-reference-implementation.md) · `verified`
- [ADR-0022 — Event System for GUI Decoupling](ADR-0022-event-system-gui-decoupling.md) · `verified`
- [ADR-0023 — Robocode Tank Royale Platform Scope and Boundaries](ADR-0023-robocode-tank-royale-platform-scope.md) · `verified`
- [ADR-0024 — Battle Runner API](ADR-0024-battle-runner-api.md) · `verified`
- [ADR-0025 — Game Type Presets and Rule Configuration](ADR-0025-game-type-presets-and-rule-configuration.md) · `verified`
- [ADR-0026 — Identity-Based Bot Matching in Battle Runner](ADR-0026-identity-based-bot-matching.md) · `verified`
- [ADR-0027 — TypeScript Bot API for Web Platform Support](ADR-0027-typescript-bot-api-architecture.md) · `verified`
- [ADR-0028 — TypeScript Bot API Threading Model](ADR-0028-typescript-bot-api-threading-model.md) · `verified`
- [ADR-0029 — TypeScript Bot API Runtime Targets](ADR-0029-typescript-bot-api-runtime-targets.md) · `verified`
- [ADR-0030 — Template-based Booting and Base Convention](ADR-0030-convention-over-configuration-bot-entry-points.md) · `verified`
- [ADR-0031 — Optional Bot Configuration Files and Runtime Property Validation](ADR-0031-optional-bot-config-and-runtime-validation.md) · `verified`
- [ADR-0032 — Tank Color Display Mode](ADR-0032-user-defined-visual-overrides-for-tanks.md) · `verified`
- [ADR-0033 — Server Debug Mode](ADR-0033-bot-debug-mode.md) · `verified`
- [ADR-0034 — Breakpoint Mode](ADR-0034-breakpoint-mode.md) · `verified`
- [ADR-0035 — Bot API Debugger Detection](ADR-0035-bot-debugger-detection.md) · `verified`
- [ADR-0036 — Start-Game Debug Options](ADR-0036-start-game-debug-options.md) · `verified`
- [ADR-0037 — Functional Core Extraction for Bot API Testability](ADR-0037-functional-core-bot-api-testability.md) · `verified`
- [ADR-0038 — Cross-Platform Test Parity and Shared Test Definitions](ADR-0038-shared-cross-platform-test-definitions.md) · `verified`
- [ADR-0039 — Server Testability — Physics Core Extraction and Test Framework](ADR-0039-server-testability.md) · `verified`
- [ADR-0040 — Raise Default readyTimeout from 1 Second to 10 Seconds](ADR-0040-ready-timeout-default.md) · `verified`
- [ADR-0041 — Bot API Library Version Management in the GUI](ADR-0041-bot-api-library-version-management.md) · `verified`
- [ADR-0042 — Behavior Version as the Battle Compatibility Contract](ADR-0042-behavior-version-epochs.md) · `verified`
- [ADR-0043 — Rumble Client Trust Boundary](ADR-0043-rumble-client-trust-boundary.md) · `verified`
- [ADR-0044 — Durable Rumble Result Acknowledgement](ADR-0044-durable-rumble-result-acknowledgement.md) · `verified`
- [ADR-0045 — Official Bot API Language Set Is Closed](ADR-0045-official-bot-api-language-set.md) · `verified`
- [IDR-001 — npmPack is the TypeScript publish preview](IDR-001-npm-publish-dry-run.md) · `inferred` — The never-built `npmPublishDryRun` task duplicated the package artifact inspection already provided by `npmPack`.
- [IDR-002 — TypeScript npm publishing reads the Gradle credential property](IDR-002-npm-publish-credential.md) · `inferred` — Publishing credentials for the repository's package ecosystems are kept in user Gradle properties rather than repository environment configuration.
- [IDR-003 — The corpus identity ledger is repaired from its own history](IDR-003-identity-ledger-repair.md) · `inferred` — The `CH` counter had drifted from the change identities already bound by the corpus, causing the allocator to reserve an identity that was already in use.
- [PDR-001 — Authorized simple pushes remain distinct from full changes](PDR-001-authorized-simple-pushes.md) · `inferred` — The review boundary must keep accepted-contract changes behind a human-merged pull request while allowing explicitly authorized maintenance work to follow a lighter route.
- [PDR-002 — Release tooling uses the repository version and verifies documentation locally](PDR-002-release-version-source-and-verification.md) · `inferred` — The Gradle build already owns the repository release version, while generated Pages output is not a checked-in artifact and direct `main` mutation is human-gated.
- [PDR-003 — The Rumble client waits for the official engine release](PDR-003-rumble-client-release-gate.md) · `inferred` — Ranked Rumble battles must target a published immutable engine distribution rather than unreleased source or a partial runtime contract.
- [PDR-004 — GUI TwinDuel follows the Rumble foundations](PDR-004-gui-twinduel-scheduling.md) · `inferred` — The TwinDuel preset exists after the Rumble foundation change, but the GUI follow-up was intentionally scheduled after the Rumble repositories, client, and documentation work.
- [PDR-005 — GUI TwinDuel ships before Rumble documentation](PDR-005-twinduel-before-rumble-docs.md) · `inferred` — The GUI is the primary product entry point, so the TwinDuel selection must be complete in the 1.1.0 experience before the remaining Rumble guides are published.
- [PDR-006 — The Rumble roadmap is an active plan](PDR-006-rumble-roadmap-plan.md) · `inferred` — The Rumble design roadmap names the next coordinated work and needs a plan item for each future change.
- [PDR-007 — Extracted criteria start as draft](PDR-007-draft-criteria-lifecycle.md) · `inferred` — The brownfield corpus contains many extracted criteria whose tests do not yet carry Cliewen purpose tags.
- [PDR-008 — Pre-Cliewen ADR identities are retained](PDR-008-pre-cliewen-adr-identities.md) · `inferred` — The pre-Cliewen ADR series is already cross-referenced throughout the repository and carries prior MADR acceptance history.
- [PDR-009 — README index blocks are the canonical corpus indexes](PDR-009-readme-index-authority.md) · `inferred` — Parallel `INDEX.md` files and template indexes could drift from the index blocks checked by validation.
- [PDR-010 — Design records use the architecture type](PDR-010-design-record-typing.md) · `inferred` — The corpus needs one vocabulary for documents that describe system structure, including the debugging guide and health reports.
- [PDR-011 — Empty extracted specifications create no capability](PDR-011-empty-extraction-directory.md) · `inferred` — The `browser-sample-bots` OpenSpec directory contained no requirements to preserve.
- [PDR-012 — Typed decision records replace the legacy decision log](PDR-012-typed-decision-record-carrier.md) · `inferred` — The legacy decision log mixed architecture, process, implementation, and routine history in one carrier that obscured the enduring subject of each choice.
<!-- clue:index:end -->
