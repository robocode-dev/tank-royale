---
id: OQ-005
type: questions
status: active
links: [CH-055]
title: Browser-form contract questions
---

# Open questions

No blocking question for aligning the contract with the already accepted browser-form decision. The maintainer selected the tracked workflow on 2026-10-19. The existing direct-route branches are preserved; this branch starts from accepted main and takes no unmerged base dependency.

The accepted base removed `.clue/acceptance.yaml` in an earlier session, while the latest supplied repository instructions explicitly require PR acceptance for tracked work. Those human instructions govern this change. It will end at a ready PR for human merge; it does not alter the acceptance configuration or claim that the CLI's default local acceptance is a protected PR gate.

The implementation milestone still needs outside-account acceptance, anonymous receipt correlation, publication, size and retry evidence. Those unknowns do not block declaring draft criteria and must not be reported as verified here.
