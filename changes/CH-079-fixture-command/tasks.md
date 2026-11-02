---
id: CH-079-tasks
type: tasks
status: open
links: [CH-079]
title: One command that runs the Rumble fixture checks and writes a redacted report
---

- [ ] Settle the open question with the maintainer: what the command is called and where it is documented.
- [ ] Create the new capability and write its acceptance criteria for the cases, the result, the report, the redaction, the fail-closed self-check, the empty-case failure and the live-check guard, before any code, each with its test type.
- [ ] Write the report generator in Python with unit tests for reading results, grouping into cases, the result file, the report, and every redaction rule including a secret of an unknown shape.
- [ ] Add the Gradle command that runs the offline checks of all Rumble modules and the Python checks with every failure collected, then runs the generator, and refuses when the live switch is on.
- [ ] Run the command for real, read both files, and confirm that nothing secret and no absolute path of this machine is in them.
- [ ] Add the Python tests to the evidence producers and regenerate the manifest.
- [ ] Document the command, its files and what a green run does not claim in the participation design, update the plan note, and update the architecture and capability indexes.
