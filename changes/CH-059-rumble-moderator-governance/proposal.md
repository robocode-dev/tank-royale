---
id: CH-059
type: change
status: open
links: [M-025, CAP-014, ADR-051]
title: Let the Tank Royale Rumble moderators own human review
---

# Tank Royale Rumble moderator role

## Why

An outside reader of the bot submission guide sees one person's name as the destination for difficult submissions. The public moderator guide and the bot repository repeat that assignment. The existing `robocode-dev/rumble-moderators` GitHub team has access to `rumble-data` but not `rumble-bots`, and the bot moderation command checks one account name. This blocks additional qualified volunteers from sharing human review even if they join the team. The finding arose while a non-maintainer was following the published onboarding guides for the announcement-readiness walkthrough.

## Change

Use “Tank Royale Rumble moderators” for the human role in public guidance, the bot catalog criterion and the moderation decision. Give volunteers a clear public contact path and state the skills and conduct expected before access is granted. Make the bot repository's escalation command authenticate active membership in the existing moderator team, with no expansion of the automatic merge path or bypass of required checks. Update bot repository governance and team access together with the code. Keep individual names only where historical authorship or an exact GitHub account is essential to an operator action.

This serves the outside Robocoder walkthrough in the announcement-readiness plan (M-025). The Rumble moderator policies in `rumble-bots` and `rumble-data` remain authoritative for operational permissions.

## Challenge

The assumption most likely to fail is that the repository-scoped moderator App can verify a closed GitHub team's current membership without broader authority than the review workflow needs. An alternative is a maintained reviewer roster, but that would drift from the GitHub team the organization already manages. The cheapest useful test is to inspect the team's current repository access and GitHub's permission requirement for its membership endpoint, then exercise the check with active, absent and pending members under a token with only the required permissions. If the App cannot receive organization Members read access, stop the team-based command change and return for a different authorization design. A technically green check would still fail contributors if volunteers cannot discover the application path or if moderators lack the judgment to review untrusted source; the public guide and governance must address both.
