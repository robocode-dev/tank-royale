---
id: CH-077-tasks
type: tasks
status: open
links: [CH-077]
title: Run one pinned battle in an isolated worker that speaks versioned messages
---

- [x] Settle the open questions with the maintainer: where the messages live and which capability holds the criteria.
- [x] Find out whether the running server reports its behavior version: it does, and the battle runner already refuses a battle when told to require a version.
- [ ] Reserve identities and create the new capability with acceptance criteria for the messages, the version refusal, the worker's events and exit codes, the engine pin and the dependency boundary, before any code, each with its test type.
- [ ] Create `lib:rumble-protocol` with the job, the events and the line codec, with tests for every message and for an unknown version.
- [ ] Create the executable module with worker mode and a battle engine interface, with tests for every event and exit code using a fake engine.
- [ ] Add the real-battle test following the runner's integration pattern, skipped unless asked for.
- [ ] Add the dependency rules check and the source scan for both new modules.
- [ ] Update the internal documentation: the participation design's module table, dependency diagram and message placement, the architecture and design indexes, the capability and decision indexes, and the plan note. Regenerate the evidence manifest and indexes and validate the corpus.
