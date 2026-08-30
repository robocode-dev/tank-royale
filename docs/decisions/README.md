# Decisions

Decision records use three subject types: ADR-xxx for software or corpus architecture, PDR-xxx for project, process, or methodology, and IDR-xxx for implementation choices. They answer: **"What future-shaping choice does this repository need to remember?"**

Each record keeps concise **Context**, **Decision**, and **Consequences** sections, adding alternatives only when they materially explain the choice. The frontmatter follows the Cliewen decision schema: `status: inferred` when a record is written by an agent and merged (merge ≠ approval), promoted to `verified` when a human explicitly stands behind it (`accepted-by:` records who, when, and in what context). ADR-001…ADR-041 predate Cliewen and were accepted under the MADR workflow; that acceptance is recorded in their `accepted-by` field.

New decisions are born inside a change (`/changes/CH-xxx/`), numbered sequentially within their subject series, and added to the index below. Routine facts, chronology, and implementation walkthroughs stay in their natural carriers rather than becoming decision records.

Writing guidelines: be specific (technical detail, not concepts); show your work (rejected alternatives and why); be honest (negative consequences too); use Mermaid diagrams where they clarify; link extensively to code, capabilities, and other records.

<!-- clue:index:start -->
- [ADR-001 — Monorepo Build Strategy](ADR-001-monorepo-build-strategy.md) · `verified`
- [ADR-002 — Standard Mathematical Coordinate System](ADR-002-standard-math-coordinate-system.md) · `verified`
- [ADR-003 — Cross-Platform Bot API Strategy](ADR-003-cross-platform-bot-api-strategy.md) · `verified`
- [ADR-004 — Java as Authoritative Reference Implementation](ADR-004-java-reference-implementation.md) · `verified`
- [ADR-005 — Independent Deployable Components](ADR-005-independent-deployable-components.md) · `verified`
- [ADR-006 — Schema-Driven Protocol Contracts](ADR-006-schema-driven-protocol-contracts.md) · `verified`
- [ADR-007 — Client Role Separation (Bot / Observer / Controller)](ADR-007-client-role-separation.md) · `verified`
- [ADR-008 — Server-Authoritative Deterministic Physics](ADR-008-server-authoritative-physics.md) · `verified`
- [ADR-009 — WebSocket Communication Protocol](ADR-009-websocket-communication-protocol.md) · `verified`
- [ADR-010 — Declarative Bot Intent Model](ADR-010-declarative-bot-intent-model.md) · `verified`
- [ADR-011 — Real-Time Game Loop Architecture](ADR-011-realtime-game-loop-architecture.md) · `verified`
- [ADR-012 — Turn Timing Semantics](ADR-012-turn-timing-semantics.md) · `verified`
- [ADR-013 — Bot Configuration via Environment Variables](ADR-013-bot-configuration-env-vars.md) · `verified`
- [ADR-014 — Two-Tier Shared-Secret Authentication](ADR-014-two-tier-authentication.md) · `verified`
- [ADR-015 — Participant ID as Unified Team Identifier](ADR-015-bot-id-team-id-namespace-separation.md) · `verified`
- [ADR-016 — Session ID for Bot Process Identification](ADR-016-session-id-bot-process-identification.md) · `verified`
- [ADR-017 — Recording Format (ND-JSON + Gzip)](ADR-017-recording-format.md) · `verified`
- [ADR-018 — Custom SVG Rendering for Bot API Graphics](ADR-018-custom-svg-rendering.md) · `verified`
- [ADR-019 — R8 Code Shrinking](ADR-019-r8-code-shrinking.md) · `verified`
- [ADR-020 — Teams Support in Observer Protocol](ADR-020-teams-support-observer-protocol.md) · `verified`
- [ADR-021 — Java Swing as GUI Reference Implementation](ADR-021-java-swing-gui-reference-implementation.md) · `verified`
- [ADR-022 — Event System for GUI Decoupling](ADR-022-event-system-gui-decoupling.md) · `verified`
- [ADR-023 — Robocode Tank Royale Platform Scope and Boundaries](ADR-023-robocode-tank-royale-platform-scope.md) · `verified`
- [ADR-024 — Battle Runner API](ADR-024-battle-runner-api.md) · `verified`
- [ADR-025 — Game Type Presets and Rule Configuration](ADR-025-game-type-presets-and-rule-configuration.md) · `verified`
- [ADR-026 — Identity-Based Bot Matching in Battle Runner](ADR-026-identity-based-bot-matching.md) · `verified`
- [ADR-027 — TypeScript Bot API for Web Platform Support](ADR-027-typescript-bot-api-architecture.md) · `verified`
- [ADR-028 — TypeScript Bot API Threading Model](ADR-028-typescript-bot-api-threading-model.md) · `verified`
- [ADR-029 — TypeScript Bot API Runtime Targets](ADR-029-typescript-bot-api-runtime-targets.md) · `verified`
- [ADR-030 — Template-based Booting and Base Convention](ADR-030-convention-over-configuration-bot-entry-points.md) · `verified`
- [ADR-031 — Optional Bot Configuration Files and Runtime Property Validation](ADR-031-optional-bot-config-and-runtime-validation.md) · `verified`
- [ADR-032 — Tank Color Display Mode](ADR-032-user-defined-visual-overrides-for-tanks.md) · `verified`
- [ADR-033 — Server Debug Mode](ADR-033-bot-debug-mode.md) · `verified`
- [ADR-034 — Breakpoint Mode](ADR-034-breakpoint-mode.md) · `verified`
- [ADR-035 — Bot API Debugger Detection](ADR-035-bot-debugger-detection.md) · `verified`
- [ADR-036 — Start-Game Debug Options](ADR-036-start-game-debug-options.md) · `verified`
- [ADR-037 — Functional Core Extraction for Bot API Testability](ADR-037-functional-core-bot-api-testability.md) · `verified`
- [ADR-038 — Cross-Platform Test Parity and Shared Test Definitions](ADR-038-shared-cross-platform-test-definitions.md) · `verified`
- [ADR-039 — Server Testability — Physics Core Extraction and Test Framework](ADR-039-server-testability.md) · `verified`
- [ADR-040 — Raise Default readyTimeout from 1 Second to 10 Seconds](ADR-040-ready-timeout-default.md) · `verified`
- [ADR-041 — Bot API Library Version Management in the GUI](ADR-041-bot-api-library-version-management.md) · `verified`
- [ADR-042 — Behavior Version as the Battle Compatibility Contract](ADR-042-behavior-version-epochs.md) · `verified`
- [ADR-043 — Rumble Client Trust Boundary](ADR-043-rumble-client-trust-boundary.md) · `verified`
- [ADR-044 — Durable Rumble Result Acknowledgement](ADR-044-durable-rumble-result-acknowledgement.md) · `verified`
- [ADR-045 — Official Bot API Language Set Is Closed](ADR-045-official-bot-api-language-set.md) · `verified`
- [ADR-046 — Battle Runner enforces an expected behavior version before bot boot](ADR-046-runner-behavior-version-precondition.md) · `verified` — Ranked Rumble execution must compare the synchronized engine epoch with the running server's handshake before untrusted bot code starts.
- [IDR-001 — npmPack is the TypeScript publish preview](IDR-001-npm-publish-dry-run.md) · `inferred` — The never-built `npmPublishDryRun` task duplicated the package artifact inspection already provided by `npmPack`.
- [IDR-002 — TypeScript npm publishing reads the Gradle credential property](IDR-002-npm-publish-credential.md) · `inferred` — Publishing credentials for the repository's package ecosystems are kept in user Gradle properties rather than repository environment configuration.
- [IDR-003 — The corpus identity ledger is repaired from its own history](IDR-003-identity-ledger-repair.md) · `inferred` — The `CH` counter had drifted from the change identities already bound by the corpus, causing the allocator to reserve an identity that was already in use.
- [IDR-004 — Rumble client uses the released Battle Runner dependency](IDR-004-rumble-client-battle-runner-dependency.md) · `verified` — CAP-016 requires the Rumble client to execute pinned battles through the Battle Runner API.
- [PDR-001 — Authorized simple pushes remain distinct from full changes](PDR-001-authorized-simple-pushes.md) · `inferred` — The review boundary must keep accepted-contract changes behind a human-merged pull request while allowing explicitly authorized maintenance work to follow a lighter route.
- [PDR-002 — Release tooling uses the repository version and verifies documentation locally](PDR-002-release-version-source-and-verification.md) · `inferred` — The Gradle build already owns the repository release version, while generated Pages output is not a checked-in artifact and direct `main` mutation is human-gated.
- [PDR-003 — The Rumble client waits for the official engine release](PDR-003-rumble-client-release-gate.md) · `inferred` — Ranked Rumble battles must target a published immutable engine distribution rather than unreleased source or a partial runtime contract.
- [PDR-004 — GUI TwinDuel follows the Rumble foundations](PDR-004-gui-twinduel-scheduling.md) · `inferred` — The TwinDuel preset exists after the Rumble foundation change, but the GUI follow-up was intentionally scheduled after the Rumble repositories, client, and documentation work.
- [PDR-005 — GUI TwinDuel ships before Rumble documentation](PDR-005-twinduel-before-rumble-docs.md) · `inferred` — The GUI is the primary product entry point, so the TwinDuel selection must be complete in the 1.1.0 experience before the remaining Rumble guides are published.
- [PDR-006 — The Rumble roadmap is an active plan](PDR-006-rumble-roadmap-plan.md) · `inferred` — The Rumble design roadmap names the next coordinated work and needs a plan item for each future change.
- [PDR-007 — Extracted criteria start as draft](PDR-007-draft-criteria-lifecycle.md) · `inferred` — The brownfield corpus contains many extracted criteria whose tests do not yet carry Cliewen purpose tags.
- [PDR-008 — Pre-Cliewen ADR acceptance provenance is retained](PDR-008-pre-cliewen-adr-identities.md) · `inferred` — The pre-Cliewen ADR series is already cross-referenced throughout the repository and carries prior MADR acceptance history.
- [PDR-009 — README index blocks are the canonical corpus indexes](PDR-009-readme-index-authority.md) · `inferred` — Parallel `INDEX.md` files and template indexes could drift from the index blocks checked by validation.
- [PDR-010 — Design records use the architecture type](PDR-010-design-record-typing.md) · `inferred` — The corpus needs one vocabulary for documents that describe system structure, including the debugging guide and health reports.
- [PDR-011 — Empty extracted specifications create no capability](PDR-011-empty-extraction-directory.md) · `inferred` — The `browser-sample-bots` OpenSpec directory contained no requirements to preserve.
- [PDR-012 — Typed decision records replace the legacy decision log](PDR-012-typed-decision-record-carrier.md) · `inferred` — The legacy decision log mixed architecture, process, implementation, and routine history in one carrier that obscured the enduring subject of each choice.
- [ADR-047 — Rumble catalog publishes immutable team membership](ADR-047-rumble-catalog-publishes-team-membership.md) · `verified` — The V1 engine pin counts the four bot processes in a TwinDuel battle, while Battle Runner starts two team entries and result ingestion receives two team results.
<!-- clue:index:end -->
