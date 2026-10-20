---
id: CH-058
type: change
status: open
links: [M-024, CAP-014, C-002]
title: Automate moderation and merge of clean Rumble bot submissions
---

# Automate moderation and merge of clean Rumble bot submissions

Bot authors currently wait for the sole human moderator even when a submission is routine. Replace that wait with a narrowly scoped GitHub moderation path: every incoming pull request is triaged, and a bot-only submission is merged automatically only after source and dependency checks, security screening, and a completed one-round battle. Failed submissions receive a repairable diagnosis; uncertain or non-bot changes go to Flemming. The bot catalog must publish after an automatic merge.

This serves the public-entry-point and launch-gate milestone (M-024) by replacing the unattainable three-moderator prerequisite with an automated first review and an explicit human escalation path. It changes the human integration boundary only for external bot-only submissions; tracked Cliewen changes and policy/workflow changes remain human-merged.

The assumption most likely to fail is that deterministic screening can identify a safe auto-merge subset without promoting malicious or broken source. A credible alternative is automated approval followed by human merge. The cheapest useful test is a dry run against known good starters and deliberately unsafe or broken submissions in a credential-free sandbox. If any bot code can access write credentials, or a failing/uncertain submission can reach merge, auto-merge must stay disabled. A technically green pipeline would still fail authors if catalog publication does not follow the merge, so that full path is part of acceptance.
