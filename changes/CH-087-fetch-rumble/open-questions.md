---
id: CH-087-open-questions
type: open-questions
status: open
links: [CH-087]
title: The file name and place, the version check, and older releases
---

## One fixed file name or a name with the version?

A person or an agent needs one path to run. With a fixed name, `<user data folder>/rumble/rumble.jar`, the path never changes and a new desktop version replaces the file. With the version in the name, an old file lies beside the new one and the path must be worked out.

Recommendation: the fixed name. The version is not lost: the program answers `--version`, and the library asks it.

## When is the downloaded file asked for its version?

The file has already been checked against the hash GitHub reports. Asking it for `--version` runs it, so it should only happen after the hash matches. It can be done on the temporary file before it is moved into place, or after.

Recommendation: before, on the temporary file, so that a file of the wrong version is never left in the place where an agent looks for the program.

## What do older releases do?

Releases up to 1.4.0 have no `rumble` asset, so a desktop program of those versions finds nothing to fetch.

Recommendation: nothing is changed in them. The answer says that the release holds no such file, and the Rumble menu appears first in a release that has it.
