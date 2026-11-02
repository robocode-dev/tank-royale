---
id: CH-082-open-questions
type: open-questions
status: open
links: [CH-082]
title: One program with two modes or two programs
---

## Should the command line program also be the battle worker, or should they be two programs?

The participation design describes one executable called `rumble` with a controller mode and a worker mode. Since then the credential boundary check was added, and it requires that the worker's classpath holds no host library, no GitHub code and no saved sign-in code. One jar that can be both would carry all of that, so its worker mode would fail that check, and the point of the worker, that a bot's process cannot reach the credentials, would be lost.

Recommendation: two programs. `rumble` is the command line and the controller, with the host code. The worker stays its own small jar, started by the controller. The design is changed to say so. The user and an agent only ever run `rumble`; the worker is an internal detail it starts. The alternative is one jar where the worker mode is started with a restricted classpath, which cannot be done with a single fat jar and would have to be a second jar in the end anyway.
