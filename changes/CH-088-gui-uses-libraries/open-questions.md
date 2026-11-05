---
id: CH-088-open-questions
type: open-questions
status: resolved
links: [CH-088]
title: In-process or process, and which file carries the worker
---

## Does the desktop program use the libraries in its own process, or run `rumble` as a program?

The libraries are small, the desktop jar would grow by well under one megabyte, and the existing reviewed diagram already shows the desktop program using shared services. A process route keeps one tested contract and keeps the token out of the desktop program, but it needs the fetched file for every Rumble screen and a streaming contract for contribution sessions.

Settled by the maintainer: in-process, as recommended.

## Which file carries the worker for the desktop program?

The worker is inside `rumble.jar`, which also holds the command line, 17 MB in all. The desktop program only needs the worker, and agents without the desktop program need the whole command line. The worker could instead be a release file of its own, 14 MB.

Settled by the maintainer: keep the one file, as recommended. The desktop program takes the worker out of `rumble.jar` the way the command line does, so there is one thing to release, check and fetch. A separate worker file is worth it only if the extra 3 MB or the single purpose starts to matter.
