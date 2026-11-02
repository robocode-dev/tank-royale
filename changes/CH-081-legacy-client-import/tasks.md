---
id: CH-081-tasks
type: tasks
status: open
links: [CH-081]
title: Copy the old Rumble client's state into the new Rumble state without changing or losing any of it
---

- [x] Settle the open question with the maintainer: whether the import may ever move or delete the old files.
- [ ] Read the old client's configuration example and its journal source once more and record, in the new capability's design notes, the files and folders the import knows about.
- [ ] Create the new capability and write its acceptance criteria for the configuration reader, the environment rule, the preview, the verified copy, the record, the repeat run, the change-during-import case and the typed results, before any code, each with its test type.
- [ ] Implement the configuration reader and the environment rule in the shared Rumble library, with tests for a known version, an unknown version, public, test, mixed and unknown addresses.
- [ ] Implement the preview and the verified copy with the temporary name, the hash check and the record written last, with tests that prove the source is untouched byte for byte and timestamp for timestamp, and that an interrupted run completes on the next run.
- [ ] Implement the typed preferences record and its reading of the client ID and the own-bot list, with a test that a client ID is never copied into the other environment's state.
- [ ] Add the old client import case to the fixture check generator and its tests, and rerun the command.
- [ ] Update the participation design, the plan note and the capability index, regenerate the evidence manifest and indexes and validate the corpus.
