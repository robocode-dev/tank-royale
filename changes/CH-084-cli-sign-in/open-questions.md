---
id: CH-084-open-questions
type: open-questions
status: open
links: [CH-084]
title: The sign-in flow, and carrying the program inside the desktop program
---

## Should the sign-in differ between programs and people?

Resolved by the maintainer on 2026-11-03: no. The review found that different behavior with and without `--json` broke the rule that the two forms give the same facts. `login` answers at once in both forms, `login --finish` completes in both forms, and `login --wait` is a text-only convenience.

## May an agent start the sign-in and register?

Resolved by the maintainer on 2026-11-03: yes, for the account its person approved. It never approves a sign-in and never creates an account. The decision is revised in this change.

## How should the program come inside the desktop program, and how does a terminal reach it?

The maintainer intends it to be carried like the server and the booter, so nobody has to fetch it. This does not block the commands in this change. The direction should be settled so that the design and plan can say it.

What was found. The desktop program carries the server, the booter and the recorder as jars, and when it needs one it copies it to a new temporary file and starts it. That suits a program the desktop program starts itself, but not a command someone types: a temporary file has no stable name, and an agent calling many commands would leave many copies of a 20 MB file.

Three further facts bear on it:

- The desktop program does not need the command line for its own work. The design has it use the shared library directly, and all it needs to start is the battle worker, which the command line already carries. Carrying the command line therefore gives it the worker as well, and gives terminal users a program.
- Size. The desktop jar is about 22 MB and the command line about 21 MB, of which 14 MB is the worker, and the worker holds its own server and booter, which the desktop program already carries. Done simply, the download nearly doubles to about 43 MB. It is an acceptable price, but it can be reduced later, and the server and booter are shrunk at build time while the command line is not yet.
- It reaches users at the next release, and today the public Rumble has no sign-in App. The step should therefore wait until the commands give something real, in line with the rule that a release must give users something clear.

Recommendation:

1. Carry the command line inside the desktop program, and let the desktop program take the battle worker from it, so there is one embedded jar and one version.
2. Reach it from a terminal through the desktop jar itself: `java -jar robocode-tankroyale-gui-<version>.jar rumble status`. The desktop program recognizes `rumble` as the first argument before it touches anything graphical, so it also works without a screen, as an agent on a server needs. The carried jar is extracted once, into the state folder and checked by its hash, as the worker is, not to a new temporary file for each call.
3. Later, a menu item in the Rumble menu can install a short `rumble` launcher, which is also when shell completion becomes worthwhile.
4. As a consequence, a separate Maven Central artifact for the command line is not needed for people to get it. That earlier open choice becomes a question only if someone wants the program without the desktop program.
5. Build this as its own change, close to the Rumble menu and before the public launch, and not now.

The alternative is to keep the command line as a separate download. It keeps the desktop jar smaller, but people must fetch a matching version themselves, which is what the maintainer wants to avoid.
