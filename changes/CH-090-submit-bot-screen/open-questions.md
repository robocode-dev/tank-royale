---
id: CH-090-open-questions
type: open-questions
status: open
links: [CH-090]
title: The slice, the confirmations and the folder chooser
---

## Should this change build only the first three steps?

The six steps are one wizard in the mockups, but the last three publish something and depend on the account, the copy and the App. The first three need none of that and stand alone as a way to check a bot and see what would be published.

Recommendation: build steps one to three now, behind the developer switch, and steps four to six in the next change.

## One confirmation or two?

The mockup has a single "I agree, Next". But there are two different statements: that the source becomes public, and that the person is the author with the right to publish it under the license it declares. The first is a fact the person must understand, the second is a statement the person makes about themselves.

Recommendation: two separate boxes, both required, so the author's own statement is never hidden inside a general "I agree".

## How does the person choose the folder?

A standard folder chooser limited to folders is the simplest, and it is what the other screens of the desktop program use for folders. It could remember the last folder, but that is one more thing saved on the computer.

Recommendation: the standard folder chooser, starting in the desktop program's bot directories if any are set, and remembering nothing new.
