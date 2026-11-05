---
id: CH-087
type: change
status: open
links: [P-007, M-029, M-031, CAP-024, CAP-016]
title: Let the desktop program fetch the Rumble command line from its own release
---

# Let the desktop program fetch the Rumble command line from its own release

The Rumble command line, `rumble`, is built and works, but nothing carries it to a person. The maintainer decided that the desktop program does not carry it inside its own jar, so the jar does not grow and the server and the booter do not lie inside it several times. Instead the desktop program fetches it the first time something in the Rumble menu needs it, from the release with the same version as the desktop program, and keeps it in the user's Tank Royale folder. That direction is written in the participation design and the plan. This change builds the part that makes it true: the release carries the file, and a library can find, fetch, check and place it.

## Why

Until the file is on the release and a tested library can fetch it safely, the Rumble menu has nothing to call, and an agent has no fixed place to find the program. Fetching code that will run on the computer is the part that most needs to be right, so it is built and proved before any screen uses it.

## What changes

- **The release carries `rumble`.** The release build adds the command line jar to the assets of the release, the release's smoke test runs it with `--version` like the other executable jars, and the release instructions name it. Nothing is published to Maven Central.
- **A library finds and fetches it.** In the shared Rumble library, a small part answers three things for a given desktop version. Where is the program, and is it the right version? What would fetching it involve (the release, the file name, its size and the hash GitHub reports for it), so a screen can ask the person first and show the size? And, once the person agrees, fetch it.
- **Exactly this release and exactly this file.** It reads the release `v<version>` of the Tank Royale repository, never "latest", and the one asset named for that version. The address it downloads from is built by the program from the version and the file name, not taken from GitHub's answer, and only over HTTPS from github.com.
- **A check before anything is placed.** The file is downloaded under a temporary name with a size ceiling, its SHA-256 is compared with the hash GitHub reports for the asset, and then the program asks the file for its version with `--version` and requires it to equal the desktop version. Only then is it moved into place. A failure leaves nothing behind and never touches an earlier good copy.
- **A fixed place.** `<user data folder>/rumble/rumble.jar`, so a person or an agent can run `java -jar <path>`. A desktop update to another version finds a file of the wrong version and fetches the right one. If the file is in use, which Windows does not allow replacing, the answer says so instead of failing in silence.
- **Plain answers for every way it can go wrong.** Offline, no such release, the release has no such file, GitHub reports no hash, GitHub's request limit, a hash that does not match, a file too big, a file of the wrong version, a file in use. Each is a typed result with a plain message, and none breaks the rest of the desktop program.
- **A way for developers.** `main` has the same version number as the latest release, so a local build would otherwise fetch an older program. A system property, `rumble.jar`, names a jar to use instead and skips the fetching, and says so. Nothing is fetched when it is set.
- **Documents.** The participation design and the command line capability describe the fetching, what the hash does and does not protect against, and the developer setting. The plan gets a progress note. New acceptance criteria are written before the code.

## Challenge

**This is code that will run on the computer.** The desktop program asks first and shows the size, which is a screen's job; this change makes the library refuse to fetch unless it is asked to, and gives the screen what it needs to ask (the size and the source). Nothing is fetched in silence, as the rule for bots already is.

**The hash protects against a broken or changed file, not against a release that has been taken over.** GitHub reports the hash for the asset, and the desktop program itself comes from the same release, so there is no new party to trust, but an attacker who could replace the asset could also replace the hash. A signature made outside GitHub would be stronger and is not in this change. The design says so plainly.

**Releases made before this change do not carry the file.** A desktop program of version 1.4.0 or earlier finds no asset, and the answer says the release has none. Only the next release will have it. Nothing in the existing releases is changed.

**The hash field is new on GitHub's side.** GitHub reports a SHA-256 digest for release assets, which the 1.4.0 release shows. A release whose assets have none is refused with a plain message, not accepted without a check.

**The real thing cannot be proved until a release holds the file.** Every outcome is proved against a fake GitHub and a fake download, and the real command line jar is used as the downloaded file to prove the version check. One real fetch from a real release needs a release with the asset, which only the maintainer can make; the handoff says it has not been done.

## Not in this change

No screens, no menu and no dialog that asks the person: those belong to the Rumble menu. The desktop program does not yet call the library. Attaching the file to the native installers is not needed and not done. No change to the old releases.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any criterion, code or document is changed.
