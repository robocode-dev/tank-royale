---
id: CH-060-open-questions
type: open-questions
status: resolved
links: [CAP-014, C-002]
title: Acceptance workflow for distinct Rumble entries
---

# Acceptance workflow

The maintainer requested a participation rule allowing moderators to reject near-duplicate bots without a substantial contribution to battle strategy, documented for authors and moderators. The tracked route is recommended because eligibility policy changes.

Resolved: the supplied AGENTS.md and the human integration constraint require a human-merged PR for every tracked change, but `.clue/acceptance.yaml` on accepted `origin/main` (`47aabff1e59dacd6be2a414286ab0d0aaad59a84`) selects local acceptance on `main`. Asked which mechanism should govern this change, the maintainer answered "PR" in the Codex conversation. This change therefore uses a hosted PR with human merge; it does not change the repository's acceptance configuration or accept itself locally.

The coordinated allocator reserved CH-060. The reservation must be preserved when work resumes.
