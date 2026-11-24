---
id: CH-116
type: change
status: open
links: [P-007, M-039, CAP-016, G-004]
title: Let a contribution session run its battles on its own Java, check the platforms of its bots before its first battle, and let the installed desktop program's own Java hold what the Contribute battles screen needs
---

# Let a contribution session run its battles on its own Java, check the platforms of its bots before its first battle, and let the installed desktop program's own Java hold what the Contribute battles screen needs

This is a slice of the milestone that releases the complete Rumble menu (M-039) in the [Rumble participation plan](../../docs/plans/P-007-rumble-participation.md): the installed desktop program on Windows can start a contribution session. It proceeds under the [simple-participation goal](../../docs/goals/G-004-simple-rumble-participation.md).

## Why

On 2026-11-24 the agent ran the session program on the Java runtime that the Windows installer ships. With Java on the path, a session ran a battle, kept it and sent it. With no Java on the path, the worker could not start, because the worker, like the desktop program's own server and booter, runs `java` from the path. The session then tried five battles and gave up, without saying that Java was missing. The user documentation already says that Tank Royale needs Java 11 or newer to be installed, also with the native installers. The maintainer chose on 2026-11-24 to keep that requirement and have the session say so, rather than ship a full JDK with the installer. The same check showed a second fault: the installer's trimmed Java lacks the `java.management` module, which the Contribute battles screen uses to find the command that starts the desktop program again. In the installed program the screen fails there with `NoClassDefFoundError`.

## What changes

- **The installer's Java runtime holds `java.management`**, so the Contribute battles screen works in the installed desktop program.
- **A session runs its battle worker on its own Java**, started by the full path of the Java the session program runs on. The worker then needs no Java on the path, and neither do its server and booter. They are started by the bare name, and Windows starts a bare name from the starting program's own folder first. The session program runs on the desktop program's Java when the desktop program starts it, so a session from the installed program runs its battles without a Java installation.
- **Java for the bots is looked for on the path.** The Java bots' launchers call `java` from the path. The platform check therefore looks for `java` in the folders of the path and runs it by its full path. On Windows the program's own Java would otherwise answer for a Java that the path does not hold. This also corrects the Download bots screen, which uses the same check.
- **A session checks the platforms of its bots before its first battle**, as it already does for Python. It uses the same platform check as the Download bots screen and the versions from the Rumble's one runtime list. Bots of a platform that is missing or too old here are left out for the whole session from the start, with every team that holds one. The session says which platform, why and which version, in its log, its status and on the Contribute battles screen, in every language. A session whose chosen bots are all left out ends with the planner's reason, as it does today.
- **Found during the work and changed:** the first plan ended a session without Java on the path with an outcome of its own. A trial showed that the worker runs on the program's own Java, so the outcome was dropped. Without Java on the path, only the Java bots are left out.
- **The connect limit and the leaving out after two battles stay** as a guard for whatever a check cannot see, such as a Java without a compiler.

## Challenge

- **Assumption most likely to be wrong:** that the trimmed Java runtime the installer ships can run the worker, its server and its booter. The installed desktop program already runs its own server and booter on it, and a trial ran a whole session on it. A Java on the path without a compiler still passes the version check, and its Java bots then fail at their start and are left out after two battles, as before.
- **Credible alternative:** ship a full JDK with the installer and run the worker and the Java bots on it. The maintainer declined this on 2026-11-24, because it adds about 150 to 200 MB and ties the installer to the Rumble's Java version.
- **Cheapest test:** the path lookup and the session's checks with a fake tool runner for each platform state, the left-out reasons in the status and the screen's texts, and the session program run on the installer's runtime with and without Java on the path.
- **Result that revises the work:** if the Rumble's runtime list cannot be read, the check only requires each tool to be there, as the Download bots screen does. If that lets through bots that then fail, the list must be carried with the program.
- **How it could pass every criterion and still fail a contributor:** the check could take long on a slow computer, since each tool is started once. It runs once per session, before the first battle, and stops after 20 seconds per tool, as the existing check does.

## Out of scope

Shipping a JDK with any package, checking for a compiler in the Java found, the Linux and macOS packages beyond what the same code gives them, and recording the Windows installer trial, which needs the maintainer.

## Acceptance criteria

Test class Unit unless stated.

- **RCL-165: A session runs its battle worker on its own Java by its full path**, and looks for the bots' Java on the path as their launchers do, never taking the program's own Java for it; the Download bots screen checks Java the same way.
- **RCL-166: A session leaves out from the start the bots of every platform that is missing or too old here**, with the teams that hold them. It says which platform and version, in its log, status and screen, in every language, and a session whose platforms are all ready leaves nothing out.
- **RCL-167: The session program runs on the installer's Java runtime with no Java on the path, leaving the Java bots out and keeping a battle of other bots, and with Java on the path keeps battles of a Java bot.** (Human.) The Contribute battles screen's own Java settings can be read on that runtime.

## Identity allocation

The change and the criteria were allocated with the repository's local allocation. A clone that has not pushed is invisible to a check of the remote.

## Human evidence

**Seen by the agent on 2026-11-25** on Windows, with the session program built from this change, run on the Java runtime the Windows installer builds (`:gui:jlinkRuntimeWin`), against the development Rumble.

1. **The fault.** A runtime with the installer's old module list failed `ManagementFactory.getRuntimeMXBean()` with `NoClassDefFoundError`; the runtime with `java.management` answered.
2. **Before the change, without Java on the path.** The session program could not start its worker, and gave up after five battles without saying why.
3. **After the change, without Java on the path.** The session said "Java bots are left out of this session: Java was not found on this computer; the Rumble needs 25 or newer". It ran the Python bots Orbit and Vector on its own Java, kept the battle and sent it as issue 49, in 46 seconds.
4. **After the change, with Java on the path.** Two battles of the Java bot Corners against TrackFire were kept and sent as issue 50, the worker again on the installer's runtime.

The installed MSI itself was not built or installed, and the desktop program's Contribute battles screen was not opened on that runtime, because the agent's clicks reached another program while the maintainer was using the computer.
