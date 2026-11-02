---
id: CH-077-tasks
type: tasks
status: open
links: [CH-077]
title: Run one pinned battle in an isolated worker that speaks versioned messages
---

- [ ] Settle the open questions with the maintainer: where the messages live and which capability holds the criteria.
- [ ] Find out whether the running server reports its behavior version, and record the answer in the proposal before the pin criterion is written.
- [ ] Reserve identities and write acceptance criteria for the messages, the version refusal, the worker's events and exit codes and the dependency boundary, before any code, each with its test type.
- [ ] Create the message module with the job, the events and the line codec, with tests for every message and for an unknown version.
- [ ] Create the executable module with worker mode and a battle engine interface, with tests for every event and exit code using a fake engine.
- [ ] Add the real-battle test following the runner's integration pattern, skipped unless asked for.
- [ ] Add the dependency rules check and the source scan for the worker.
- [ ] Update the participation design's module table and dependency diagram and the plan note, regenerate the evidence manifest and indexes and validate the corpus.
