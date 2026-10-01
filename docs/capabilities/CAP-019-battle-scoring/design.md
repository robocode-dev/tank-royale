---
id: CAP-019-design
type: design
status: active
links: [CAP-019]
title: Design notes for CAP-019 (battle scoring)
---

# CAP-019 design

`ScoreTracker` stores a score per bot and identifies scoring groups with typed keys: `Team(TeamId)` or `UnteamedBot(BotId)`. Do not use the compact integer in `ParticipantId.id` as a group key because negative team IDs can collide with the negated IDs used for unteamed bots. On a death update, the tracker first removes every newly defeated bot from the alive set, then awards 50 survival points per defeated opponent bot to each remaining bot on another group. Intersecting the update with the alive set makes a repeated death report idempotent.

When the alive set contains bots from one group, each living member receives a one-time last-survivor count equal to the number of bots in all other groups. `ResultsView` continues to sum those individual scores when it creates a team result.
