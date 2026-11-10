---
id: CH-095-open-questions
type: open-questions
status: open
links: [CH-095]
title: The licenses on each level, the Download bots screen, and how team members are written
---

## Which licenses are on each level?

The maintainer asked for the lists to be checked online rather than written from the best-known licenses. The Blue Oak Council, a council of licensing lawyers, publishes a list of 225 permissive licenses rated Model, Gold, Silver, Bronze and Lead (version 16) and a guide to 61 copyleft licenses in four kinds, weak, strong, network and maximal (version 11), both as data under SPDX identifiers. The proposal now takes its levels from those lists.

Still open: the level of the 72 permissive licenses the council rates Lead, which "lack one or more essential elements of permissive open software licenses or impose unusually burdensome requirements", such as WTFPL, Beerware and the Artistic License 1.0. Recommendation: copy with conditions, because the reader must read them before copying. The alternatives are free to copy, which hides their odd terms, and do not copy, which is wrong for licenses meant to let people copy.

## Does the Download bots screen show the level too?

Recommendation: yes, beside each bot's license in the list and the results, as the mockups draw. It is a few words per row, and it reads the same as Bot Info. The alternative is Bot Info only; the download screen already has a window with a short explanation of each license the Rumble allows.

## How are a team's members written?

Decided by the maintainer: names in order, after checking how the original Robocode does it. The original Robocode keeps a team's members in its `team.members` setting as a comma-separated list of names, each repeated member written each time, such as `sampleteam.MyFirstLeader,sampleteam.MyFirstDroid,sampleteam.MyFirstDroid,sampleteam.MyFirstDroid,sampleteam.MyFirstDroid`, and its robot description panel does not show the members. Bot Info therefore lists the names exactly as the team's configuration file gives them, in order, a repeated member each time. The recommendation was to write a repeated member once with its count.
