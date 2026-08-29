---
id: CH-030-tasks
type: tasks
status: open
links: [CH-030]
title: Task breakdown for CH-030
---

# CH-030 — Tasks

- [x] Reserve CH-030 and branch from the current tip of `main`
- [x] Confirm the M-008 prerequisites: Tank Royale 1.1.0, `rumble-bots`, and `rumble-data`
- [x] Capture the resumed implementation proposal and full-route decision
- [x] Commit and push the proposal, then open the required draft PR before implementation
- [x] Inspect the current `rumble-client`, `rumble-bots`, and `rumble-data` contracts; the required Battle Runner dependency is recorded as a blocking approval question
- [x] Obtain approval to add the published Tank Royale Battle Runner dependency to `rumble-client`
- [ ] Implement and test RCL-001 configuration validation before side effects
- [ ] Implement and test RCL-002 synchronized ranked snapshots and immutable source-cache validation
- [ ] Implement and test RCL-003 seeded ranked selection for all V1 game types
- [ ] Resolve the published Battle Runner behavior-version visibility gap before RCL-004 execution
- [ ] Implement and test RCL-004 and RCL-005 ranked/practice separation, Battle Runner execution, result transcription, and replay evidence
- [ ] Implement and test RCL-006 and RCL-007 durable journaling, receipt-driven retry, and bounded issue-ops submission
- [ ] Implement and test RCL-008 reproducible container and documented bare-metal fallback across all supported bot platforms
- [ ] Implement and test RCL-009 end-to-end ranked submission into immutable `rumble-data` facts without manual handling
- [ ] Reassess CAP-016 criteria status and record verified evidence and M-008 completion in the corpus
- [ ] Run external focused checks, relevant Tank Royale checks, `clue validate`, and `clue-verify`; digest the accepted change
