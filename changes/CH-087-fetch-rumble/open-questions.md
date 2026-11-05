---
id: CH-087-open-questions
type: open-questions
status: resolved
links: [CH-087]
title: The file name and place, the version check, and older releases
---

## One fixed file name or a name with the version?

A person or an agent needs one path to run. With a fixed name, `<user data folder>/rumble/rumble.jar`, the path never changes and a new desktop version replaces the file. With the version in the name, an old file lies beside the new one and the path must be worked out.

Settled by the maintainer: the fixed name, `rumble.jar`. (Recommendation was the same.) The version is not lost: the program answers `--version`, and the library asks it.

## When is the downloaded file asked for its version?

The file has already been checked against the hash GitHub reports. Asking it for `--version` runs it, so it should only happen after the hash matches. It can be done on the temporary file before it is moved into place, or after.

Settled by the maintainer: yes, before. On the temporary file, so that a file of the wrong version is never left in the place where an agent looks for the program.

## What do older releases do?

Releases up to 1.4.0 have no `rumble` asset, so a desktop program of those versions finds nothing to fetch.

Settled by the maintainer: yes. Nothing is changed in them. The answer says that the release holds no such file, and the Rumble menu appears first in a release that has it.
