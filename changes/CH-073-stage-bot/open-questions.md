---
id: CH-073-open-questions
type: open-questions
status: open
links: [CH-073]
title: Build folders, launcher line endings and where the content hash lives
---

## Should build, editor and Git folders be left out of the copy, or reported as problems?

The bot repository refuses folders such as `bin`, `obj`, `node_modules` and `.venv`. An author who has built their bot locally will have them. The recommendation is to leave them out of the staged copy and list them as not included with the reason, since they are normal products of working on a bot and the author should not have to delete them from their own project; the same applies to Git, editor and cache folders. The alternative is to report each as a problem and ask the author to remove it, which is stricter but sends authors to clean a project that is theirs.

## Should the launcher script's line endings be changed in the copy?

A Windows author's `.sh` launcher often has Windows line endings, which a Unix shell cannot run, and the failure looks like a mystery. The recommendation is to convert the two launcher scripts named after the bot to Unix line endings in the staged copy only, to list the conversion in the description, and to leave every other file alone. The alternative is to report the line endings as a problem and make the author fix them, which they may not know how to do on Windows.

## Should the content hash move into the bot project library?

Staging must report the hash the bot repository will record, and the rule for it is today in the Rumble library. The recommendation is to move it into the bot project library, which the Rumble library already is planned to use, so there is one implementation and the dependency diagram holds. The alternative is to keep a second copy in the bot project library, which risks the two drifting apart.
