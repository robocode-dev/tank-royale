---
id: CH-069-open-questions
type: open-questions
status: open
links: [CH-069]
title: Transparency in team messages and where the criteria live
---

## Should a colour keep its transparency when it is sent in a team message?

Java, the reference, writes a colour in a team message as `#rrggbb` and drops transparency, and it cannot read eight digits. Keeping transparency means changing Java's writer to `#RRGGBBAA` for transparent colours, which is a change to the reference. The recommendation is to keep transparency: it loses nothing, opaque colours (nearly all) stay in the six-digit form that every API version can read, and it matches what every API's own hex colour function already returns for drawing. The alternative is to follow the reference exactly and let all APIs drop transparency in team messages.

## Where should the criteria live?

Team messages and their limits are already described by criteria in the protocol capability. The recommendation is to put the colour text rule there, next to them. The alternative is a new capability for the Bot API colour type, which seems too small to deserve one.
