---
id: CH-032-open-questions
type: open-questions
status: open
links: [CH-032]
title: Open questions for CH-032
---

# CH-032 — Open questions

No contract question is open. The bot-catalog and contributor-registration pull requests are merged, and the public catalog, 1v1 advice, local replay evidence, and ranked journal are ready. The corrected token created and labeled issue [rumble-data#9](https://github.com/robocode-dev/rumble-data/issues/9), but the data-repository workflow read the GitHub issue author from `author.login`; the Issues API provides it as `user.login`, so the batch was rejected with account `null`. Repair that implementation defect with focused evidence before resubmitting the retained journal. This prerequisite is not permission to weaken ADR-043's trust boundary.
