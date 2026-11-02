---
id: CH-082-open-questions
type: open-questions
status: open
links: [CH-082]
title: Two programs, and whether they are published
---

## Should the command line program also be the battle worker, or should they be two programs?

Resolved by the maintainer on 2026-11-02: two programs, following the recommendation below.

The participation design describes one executable called `rumble` with a controller mode and a worker mode. Since then the credential boundary check was added, and it requires that the worker's classpath holds no host library, no GitHub code and no saved sign-in code. One jar that can be both would carry all of that, so its worker mode would fail that check, and the point of the worker, that a bot's process cannot reach the credentials, would be lost.

Recommendation: two programs. `rumble` is the command line and the controller, with the host code. The worker stays its own small jar, started by the controller. The design is changed to say so. The user and an agent only ever run `rumble`; the worker is an internal detail it starts. The alternative is one jar where the worker mode is started with a restricted classpath, which cannot be done with a single fat jar and would have to be a second jar in the end anyway.

## Should these programs be published to Maven Central and the other places Tank Royale publishes?

What Tank Royale publishes today: the runnable programs (server, booter, recorder, the desktop program) and the battle runner, and the Java Bot API, each as a signed Maven Central artifact built by the shared publishing setup and released in lockstep with one version; the release attaches the desktop program, server and runner jars to the GitHub release; Bot API packages also go to NuGet, PyPI and npm. The shared libraries (`lib:common`, `lib:client`, `lib:rumble`, `lib:bot-project` and the rest) are not published on their own; they are bundled into the programs that use them. The battle runner shows the pattern for a program that needs other programs: it carries the server and the booter as resources inside its own jar and extracts them when it needs them.

Facts that bear on the choice. Anything published is a long-lived commitment: coordinates, a signed jar for every release, and users who may depend on its behavior, while the Rumble is in public Beta and its command line is new. The repository's own rule is that a release must give users something clear and is never made only to unblock internal work. And publishing needs no code in this change: it is switching on the shared publishing setup for a module later.

Recommendation: do not publish anything in this change, and shape the programs so that publishing is a small step later. Only `rumble` (the command line) would ever be a candidate. The worker is an internal part of it, so the command line carries the worker's jar as an embedded resource, off its own classpath so the credential boundary still holds, and extracts it when it needs it, as the battle runner does with the server and the booter. The libraries stay unpublished, as all of the shared libraries are today. The decision to publish `rumble` to Maven Central and attach it to the GitHub release is taken at the first release where the command line gives users something real (for example when sign-in, submission and running battles work), and it is recorded as a decision at that time. The alternative is to publish from the first release that contains the module, which costs a commitment before the command line is useful and before its contract is stable.
