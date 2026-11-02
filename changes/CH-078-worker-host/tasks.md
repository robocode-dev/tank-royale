---
id: CH-078-tasks
type: tasks
status: open
links: [CH-078]
title: Start the battle worker from the host with a scrubbed environment and limits
---

- [ ] Settle the open question with the maintainer: how the host finds the command that starts the worker.
- [ ] Reserve identities and add acceptance criteria to the battle worker capability for the clean environment, the limits, stopping the whole process tree, the typed outcomes and the honest statement of what the boundary does not cover, before any code, each with its test type.
- [ ] Implement the clean environment from an allowlist with its own empty working and temporary folders, with tests that put a fake token and home folder in the test's own environment and prove a real child process cannot read them.
- [ ] Implement the worker host with the job on standard input, events from standard output, the output and line ceilings and the bounded discarding of standard error, with tests using a small fake worker process for every outcome.
- [ ] Implement the time limit that stops the whole process tree, with a test whose worker starts a long-running child and proves nothing is left after the timeout.
- [ ] Add the live check that runs the real worker with sample bots through the host, skipped unless asked for, and record which environment variables the runtimes really needed.
- [ ] Extend the no-token-in-text tests and the source scan to the host code, and extend the dependency rules so the host library still does not depend on the runner.
- [ ] Update the participation design and the plan note, including what the boundary does not cover, regenerate the evidence manifest and indexes and validate the corpus.
