---
id: CH-080-tasks
type: tasks
status: open
links: [CH-080]
title: Prove the credential boundary between the host and the battle worker end to end
---

- [ ] Settle the open question with the maintainer: whether the host sets the home and temporary folders through the environment variable every Java process reads.
- [ ] Confirm by experiment that a Java process on Windows learns the real home folder when only the environment variable and not the Java setting is changed, and record the result in the proposal.
- [ ] Write the new acceptance criteria in the battle worker capability, before any code, each with its test type.
- [ ] Make the host set the Java settings in the clean environment, with a test where a worker started by a command without the Java settings still sees the empty folders, also in a Java process the worker starts.
- [ ] Write the end-to-end discovery check with a real saved sign-in in a stand-in data folder, covering every ordinary means and the loading through the store, and the check that a copy opened from another home folder cannot be decrypted.
- [ ] Write the check of the command line and standard input, the classpath scan and the message-field guard.
- [ ] Add the credential boundary case to the fixture check generator and its tests, and rerun the command.
- [ ] Update the participation design with what is closed and what remains open, update the plan note, regenerate the evidence manifest and indexes and validate the corpus.
