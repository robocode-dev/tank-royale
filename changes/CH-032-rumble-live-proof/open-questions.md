---
id: CH-032-open-questions
type: open-questions
status: open
links: [CH-032]
title: Open questions for CH-032
---

# CH-032 — Open questions

No contract question is open. The bot-catalog and contributor-registration pull requests are merged, and the public catalog, 1v1 advice, local replay evidence, and ranked journal are ready. The supplied token authenticates as `flemming-n-larsen` and can read Issues, but GitHub rejects issue creation with HTTP 403, so no submission issue or receipt exists and the journal remains intact. Replace or reconfigure the token with resource owner `robocode-dev`, access limited to `rumble-data`, and effective Issues read/write permission; complete organization approval if required. Supply it through the `RUMBLE_CLIENT_TOKEN` environment variable. This prerequisite is not permission to weaken ADR-043's trust boundary.
