---
id: TASKS-003
type: tasks
status: open
links: [CH-049]
title: Tasks for CH-049
---

# Tasks

- [x] Add CAP-006 acceptance criteria `PRO-010`, `PRO-010a`, `PRO-011`, `PRO-011a`, `PRO-012`, and `PRO-012a` for name assignment and API behavior, with positive and negative evidence requirements.
- [x] Extend the BotInfo and bot-handshake schemas, generated protocol models, flow documentation, and state reference for `teamMemberName` and the optional per-bot name map.
- [x] Preserve the `start-game.botAddresses` order through server participant selection and assign duplicate-name suffixes from that order without changing bot IDs for `PRO-010`.
- [x] Send each bot its own name and teammate names while omitting opponents; add server integration coverage for long names, versions, order, duplicates, and filtering for `PRO-010` and `PRO-010a`.
- [x] Implement and test Java `getBotName(botId)` as the reference behavior for `PRO-012` and `PRO-012a`.
- [x] Implement the same `teamMemberName` BotInfo property and handshake behavior in the Java reference API and all three other APIs for `PRO-011` and `PRO-011a`.
- [x] Port the same public API and positive/negative tests to .NET, Python, and TypeScript for `PRO-012`, `PRO-012a`, `PRO-011`, and `PRO-011a`; update the uniform test registry.
- [x] Update API references and team messaging documentation on robocode.dev, plus the Unreleased changelog.
- [x] Record the durable protocol choice to preserve full team identity separately from display names in `ADR-048`.
- [x] Build and test all affected modules and run protocol/schema validation.
