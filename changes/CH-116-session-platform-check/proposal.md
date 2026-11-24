---
id: CH-116
type: change
status: open
links: [P-007, M-039, CAP-016, G-004]
title: Let a contribution session check Java and every bot platform before its first battle, and let the installed desktop program's own Java hold what the Contribute battles screen needs
---

# Let a contribution session check Java and every bot platform before its first battle, and let the installed desktop program's own Java hold what the Contribute battles screen needs

This is a slice of the milestone that releases the complete Rumble menu (M-039) in the [Rumble participation plan](../../docs/plans/P-007-rumble-participation.md): the installed desktop program on Windows can start a contribution session. It proceeds under the [simple-participation goal](../../docs/goals/G-004-simple-rumble-participation.md).

## Why

On 2026-11-24 the agent ran the session program on the Java runtime that the Windows installer ships. With Java on the path, a session ran a battle, kept it and sent it. With no Java on the path, the worker could not start, because the worker, like the desktop program's own server and booter, runs `java` from the path. The session then tried five battles and gave up, without saying that Java was missing. The user documentation already says that Tank Royale needs Java 11 or newer to be installed, also with the native installers. The maintainer chose on 2026-11-24 to keep that requirement and have the session say so, rather than ship a full JDK with the installer. The same check showed a second fault: the installer's trimmed Java lacks the `java.management` module, which the Contribute battles screen uses to find the command that starts the desktop program again. In the installed program the screen fails there with `NoClassDefFoundError`.

## What changes

- **The installer's Java runtime holds `java.management`**, so the Contribute battles screen works in the installed desktop program.
- **A session checks Java before its first battle.** With no Java on the path, or a Java that cannot be read, it ends at once with a new outcome word, `java-missing`. It says that Java is needed for the battles and which version the Rumble needs, and runs no battle. The Contribute battles screen shows this outcome with its own text in every language.
- **A session checks the platforms of the other bots before its first battle**, as it already does for Python. The checks use the same platform check as the Download bots screen and the versions from the Rumble's one runtime list. Bots of a platform that is missing or too old here are left out for the whole session from the start, with every team that holds one. The session says which platform and which version is needed, in its log, its status and on the Contribute battles screen. Java bots are left out when the Java found is older than the Rumble needs, even when it can run the worker. A session whose chosen bots are all left out ends with the planner's reason, as it does today.
- **The connect limit and the leaving out after two battles stay** as a guard for whatever a check cannot see, such as a Java without a compiler.

## Challenge

- **Assumption most likely to be wrong:** that `java -version` on the path is the Java the worker and the Java bots will use. The worker and the booter run the same `java` from the path, and the bots' launchers call `java` too, so they agree. A Java runtime without a compiler still passes the version check, and then its Java bots fail at their start. They are left out after two battles, as before.
- **Credible alternative:** ship a full JDK with the installer and run the worker and the Java bots on it. The maintainer declined this on 2026-11-24, because it adds about 150 to 200 MB and ties the installer to the Rumble's Java version.
- **Cheapest test:** the session's checks with a fake tool runner for each platform state, the outcome and the left-out reasons in the status and on the screen's texts, and the session program run on the installer's runtime with and without Java on the path.
- **Result that revises the work:** if the Rumble's runtime list cannot be read, the check only requires each tool to be there, as the Download bots screen does. If that lets through bots that then fail, the list must be carried with the program.
- **How it could pass every criterion and still fail a contributor:** the check could take long on a slow computer, since each tool is started once. It runs once per session, before the first battle, and stops after 20 seconds per tool, as the existing check does.

## Out of scope

Shipping a JDK with any package, checking for a compiler in the Java found, the Linux and macOS packages beyond what the same code gives them, and recording the Windows installer trial, which needs the maintainer.

## Acceptance criteria

Test class Unit unless stated.

- **RCL-165: A session with no usable Java on the path ends before its first battle** with the outcome `java-missing`, says that Java is needed and which version, and runs no battle; the Contribute battles screen has a text for it in every language.
- **RCL-166: A session leaves out from the start the bots of every platform that is missing or too old here**, with the teams that hold them. It says which platform and version, in its log, status and screen, in every language, and a session whose platforms are all ready leaves nothing out.
- **RCL-167: The session program runs a battle on the installer's Java runtime with Java on the path, and ends at once with `java-missing` without it.** (Human.) The Contribute battles screen's own Java settings can be read on that runtime.

## Identity allocation

The change and the criteria were allocated with the repository's local allocation. A clone that has not pushed is invisible to a check of the remote.

## Human evidence

To be written after the trial.
