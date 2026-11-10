---
id: CH-095-open-questions
type: open-questions
status: open
links: [CH-095]
title: The licenses on each level, the Download bots screen, and how team members are written
---

## Are the licenses on the two lists the right ones?

Recommendation: the lists in the proposal. Free to copy: MIT, MIT-0, BSD-2-Clause, BSD-3-Clause, Apache-2.0, ISC, Zlib, BSL-1.0, Unlicense, 0BSD, CC0-1.0. Copy with conditions: GPL-2.0, GPL-3.0, LGPL-2.1, LGPL-3.0 and AGPL-3.0 in their `-only` and `-or-later` forms and the deprecated forms without a suffix, MPL-2.0, EPL-2.0, EUPL-1.2, CC-BY-4.0 and CC-BY-SA-4.0. Everything else is at the do-not-copy level. The lists cover the licenses the Rumble allows and the ones bot authors commonly use; a license missing from them only shows a more careful level.

## Does the Download bots screen show the level too?

Recommendation: yes, beside each bot's license in the list and the results, as the mockups draw. It is a few words per row, and it reads the same as Bot Info. The alternative is Bot Info only; the download screen already has a window with a short explanation of each license the Rumble allows.

## How are a team's members written?

Recommendation: the names the team's configuration file gives, in order, with a repeated member written once with its count: `MyFirstLeader, MyFirstDroid (4)`. The mockups say "by name and version", but the configuration file names members only by their bot folder, so the version would have to be looked up in each member's own folder. The alternative is every name as written, `MyFirstLeader, MyFirstDroid, MyFirstDroid, MyFirstDroid, MyFirstDroid`.
