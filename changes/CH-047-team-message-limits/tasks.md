---
id: TASKS-002
type: tasks
status: open
links: [CH-047]
title: CH-047 implementation tasks
---

# Tasks

- [ ] Define CAP-018 criteria for count, byte budgets, atomic rejection, and delivery timing (TML-001 through TML-004).
- [ ] Align schema and server validation, including the pre-parse WebSocket text bound (TML-001, TML-002, TML-003, TML-004).
- [ ] Align Java, Python, .NET, and TypeScript enqueue validation and API documentation (TML-001, TML-002, TML-003).
- [ ] Run boundary, malformed-client, and delivery tests against matched local builds (TML-001, TML-002, TML-003, TML-004).
- [-] Run CombatTeam and five-bot stress at 30 TPS; record turn, skip, and traffic measurements and apply the acceptance gate (TML-004). The five-bot trial failed: delivery was incomplete and unordered, and recipients recorded skipped turns. A human decision on revised limits is required before further policy work.
- [ ] After the gate passes, update the Tank Royale article, bridge evidence, and book pages; run relevant checks (TML-001, TML-002, TML-004).
