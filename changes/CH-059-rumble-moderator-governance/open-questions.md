---
id: OQ-009
type: open-questions
status: resolved
links: [CH-059]
title: Tank Royale Rumble moderator questions
---

# Open questions

The existing `robocode-dev/rumble-moderators` team and its Write access to `rumble-data` are verified. At the time of preparation, its only member is the project creator. The team was granted Write access to `rumble-bots` on 2026-10-22, and GitHub's team-repository listing confirmed the `write` role. The Moderator App's organization Members read permission was added and its installation update approved on 2026-10-22; GitHub's organization-installations API confirmed `members: read`. The team-based command and volunteer issue form merged in [`rumble-bots` PR 30](https://github.com/robocode-dev/rumble-bots/pull/30) on 2026-10-22 at merge commit `01e3d8d77ed08771178c558f1eaeb1f86e3c4661`. GitHub's `main` CODEOWNERS now assigns protected files to the team.

The exact GitHub REST membership lookup returned `active` for the current team maintainer under the maintainer's own token. Focused tests exercise active, absent, pending and failed membership responses; a live App installation-token lookup has not yet been observed. The command fails closed if GitHub denies that lookup. A second qualified volunteer can be added by a team maintainer or organization owner after the published criteria and example review are applied. The volunteer issue form is now available from the bot repository; the Tank Royale guide can link to it when this local change is accepted.
