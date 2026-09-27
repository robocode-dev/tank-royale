---
id: TASKS-003
type: tasks
status: open
links: [CH-049]
title: Tasks for CH-049
---

# Tasks

- [ ] Add CAP-006 acceptance criteria `PRO-006` and `PRO-007` for battle-wide name assignment and API lookup, with positive and negative evidence requirements.
- [ ] Extend the game-start schema and flow documentation with the optional per-bot name map and roster-order suffix rule for `PRO-006`.
- [ ] Preserve the `start-game.botAddresses` order through server participant selection and assign duplicate-name suffixes from that order without changing bot IDs for `PRO-006`.
- [ ] Send each bot its own name and teammate names while omitting opponents; add server integration coverage for order, versions, duplicates, and filtering for `PRO-006`.
- [ ] Implement and test Java `getBotName(botId)` as the reference behavior for `PRO-007`.
- [ ] Port the same public API and positive/negative tests to .NET, Python, and TypeScript for `PRO-007`; update the uniform test registry.
- [ ] Update API references and team messaging documentation on robocode.dev, plus the Unreleased changelog.
- [ ] Build and test all affected modules and run protocol/schema validation.
- [ ] Digest verified behavior into `/docs`, remove the transient change workspace, and complete pre-merge verification.
