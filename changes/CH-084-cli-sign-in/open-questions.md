---
id: CH-084-open-questions
type: open-questions
status: resolved
links: [CH-084]
title: The sign-in flow, and how the program reaches people
---

## Should the sign-in differ between programs and people?

Resolved by the maintainer on 2026-11-03: no. The review found that different behavior with and without `--json` broke the rule that the two forms give the same facts. `login` answers at once in both forms, `login --finish` completes in both forms, and `login --wait` is a text-only convenience.

## May an agent start the sign-in and register?

Resolved by the maintainer on 2026-11-03: yes, for the account its person approved. It never approves a sign-in and never creates an account. The decision is revised in this change.

## Which environment do the commands use when none is named?

Resolved by the maintainer on 2026-11-03: the public Rumble, with `-e`, `--environment` or `RUMBLE_ENVIRONMENT` choosing another, and every answer naming the environment.

## How does the program reach people?

Resolved by the maintainer on 2026-11-03: the desktop program fetches it at first use. It is not carried inside the desktop program, so the desktop jar stays at about 22 MB and the server and the booter do not lie in it several times. The fetching is built in a separate change; this direction goes into the participation design and the plan.

- **When.** The first time something in the Rumble menu needs the program. The desktop program asks first and shows the size, because this is code that will run on the computer. Nothing is fetched silently.
- **From where.** Exactly the release `v<version>` in the Tank Royale repository and exactly the file for that version, over HTTPS only and never "latest", so the version always matches the desktop program.
- **Check.** SHA-256 against the hash GitHub reports for the file in its API. The file is fetched under a temporary name with a size ceiling, checked, and only then given its real name. The program is then asked for its version, which must match.
- **Where.** `<user data folder>/rumble/rumble.jar`, a fixed place, so a person or an agent can run it with `java -jar`. A new desktop version fetches a new one. A file in use on Windows is reported, not silently left.
- **Failures.** Being offline, a missing release, GitHub's rate limit or a wrong hash give a clear message and leave nothing half done. Everything else in the desktop program keeps working. The Rumble needs the network anyway, so fetching adds no new offline loss; battles outside the Rumble are untouched.
- **Development builds.** `main` carries the version number of the latest release, so a local build would otherwise fetch an older program. A setting, `-Drumble.jar=<path>`, which Gradle sets for local runs, points at the local build.
- **Without the desktop program.** The same jar can be fetched from the release page. A separate Maven Central artifact is not needed, which answers the earlier open choice about publishing.

Two facts were checked. A hash built into the desktop program cannot work: the installers are built separately on each operating system and the jars are not identical between builds, as the 1.4.0 desktop jar on GitHub (22072821 bytes) and a local build (22072115 bytes) show. And the program is about 20 MB, of which 13.8 MB is the worker with its own server and booter, so for people who use the Rumble those two still lie on the disk twice, though not inside the desktop jar. Removing that would make the program unable to stand alone, so it stays.

The hash from GitHub protects against interrupted and damaged files, not against a compromised release. The desktop program comes from the same place, so no new party is trusted; a stronger check with a signature can be added later. Whether the desktop program's screens use the library directly and take only the worker from the fetched jar, or run the program as a process with JSON, is decided together with the Rumble menu.
