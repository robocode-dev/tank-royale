---
id: CH-103-tasks
type: tasks
status: open
links: [CH-103]
title: Send batches and acknowledge receipts in the shared library
---

- [ ] Record the decision and link it from the client capability's design and the participation design.
- [ ] Write the criteria in the Rumble client capability, each with its identity and test class.
- [ ] Make batches from the journal's waiting records of the current epoch, bounded by count and size, in the result-data record shape.
- [ ] Send each batch as the signed-in contributor after handing its records over in the journal, and return them to waiting when the send fails.
- [ ] Read the receipts of the issues sent and accept, reject or return each record by its receipt line.
- [ ] Send nothing without a usable sign-in or client ID, and report why.
- [ ] Run sending and receipt collection between a session's battles.
- [ ] Prove each criterion in both directions with a fake GitHub API, and one real batch in the test Rumble.
- [ ] Add the cases to the fixture command.
- [ ] Record the slice in the plan item, which stays open.
