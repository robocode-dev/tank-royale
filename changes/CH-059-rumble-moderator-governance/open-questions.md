---
id: OQ-009
type: open-questions
status: open
links: [CH-059]
title: Tank Royale Rumble moderator questions
---

# Open questions

The existing `robocode-dev/rumble-moderators` team and its Write access to `rumble-data` are verified. At the time of preparation, its only member is the project creator. The team was granted Write access to `rumble-bots` on 2026-10-22, and GitHub's team-repository listing confirmed the `write` role. The Moderator App's organization Members read permission was added and its installation update approved on 2026-10-22; GitHub's organization-installations API confirmed `members: read`. The team-based command and volunteer issue form are prepared in [`rumble-bots` PR 30](https://github.com/robocode-dev/rumble-bots/pull/30); they are not live until that pull request is reviewed and merged.

Before activating the team-based command, verify with an App installation token that the active membership lookup succeeds while an absent or pending member cannot approve. The exact GitHub REST membership lookup returned `active` for the current team maintainer under the maintainer's own token; an App installation-token lookup remains to be exercised after the trusted workflow contains the new code. A second qualified volunteer can then be added by a team maintainer or organization owner after the published criteria and example review are applied. The bot repository change must be integrated before publishing the Tank Royale guide's volunteer-form link.
