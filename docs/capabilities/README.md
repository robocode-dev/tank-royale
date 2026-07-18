# Capabilities

CAP-xxx: what the system does, each with acceptance criteria (Gherkin, tagged ACs) and design notes kept next to them.

Each capability folder holds `README.md` (what and why), `criteria.md` (acceptance criteria; every scenario carries exactly one `@<prefix>-nnn` tag from the capability's `ac-prefix` namespace), and `design.md` (how, close to the criteria). All thirteen were extracted from the retired OpenSpec corpus at CH-001 — see [AN-001](../analysis/AN-001-openspec-extraction.md); OpenSpec scenarios carried no IDs, so AC IDs were minted fresh at extraction.

<!-- clue:index:start -->
- [CAP-001 — Battle Runner](CAP-001-battle-runner/README.md) · `active`
- [CAP-002 — Booter fallback bot discovery](CAP-002-booter-fallback-discovery/README.md) · `active`
- [CAP-003 — Bot API updater](CAP-003-bot-api-updater/README.md) · `active`
- [CAP-004 — GUI boot progress](CAP-004-gui-boot-progress/README.md) · `active`
- [CAP-005 — GUI bot console](CAP-005-gui-bot-console/README.md) · `active`
- [CAP-006 — WebSocket protocol](CAP-006-protocol/README.md) · `active`
- [CAP-007 — Python Bot API internal alignment](CAP-007-python-bot-api-internal-alignment/README.md) · `active`
- [CAP-008 — Runtime config validation](CAP-008-runtime-config-validation/README.md) · `active`
- [CAP-009 — Tank color settings](CAP-009-tank-color-settings/README.md) · `active`
- [CAP-010 — Test framework stack](CAP-010-test-framework-stack/README.md) · `active`
- [CAP-011 — TypeScript Bot API](CAP-011-typescript-bot-api/README.md) · `active`
- [CAP-012 — User documentation](CAP-012-user-documentation/README.md) · `active`
- [CAP-013 — TypeScript Bot API npm publishing](CAP-013-typescript-bot-api-npm-publish/README.md) · `draft`
<!-- clue:index:end -->
