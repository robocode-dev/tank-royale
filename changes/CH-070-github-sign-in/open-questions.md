---
id: CH-070-open-questions
type: open-questions
status: resolved
links: [CH-070]
title: The public sign-in App and how the waiting is run
---

## Should the public Beta get its sign-in App now?

Only the development environment has a sign-in App, created for the experiment. The public Beta has none. The recommendation is to leave the public entry empty in this change and have the library refuse sign-in there with a clear message, because creating the public App is a maintainer task with a manual step (switching on the device sign-in setting in GitHub) and belongs with the public launch. The alternative is to create the public App now so the whole journey can be tried against the public Beta.

Resolved by the maintainer on 2026-11-01: follow the recommendation. The public entry stays empty and the library refuses sign-in there until the public App exists.

## How should the waiting for approval be run?

The person may take minutes to approve the code, and GitHub wants the program to check no faster than its interval. The recommendation is that the library offers one step at a time (ask once, check once) and a ready-made waiting loop that takes the clock, the waiting and a cancel signal as inputs, so a screen can run it on its own thread and cancel it, and tests can run it without real waiting. The alternative is to offer only the loop, which is simpler but harder to test and to cancel.

Resolved by the maintainer on 2026-11-01: follow the recommendation. The library offers one step at a time and a ready-made waiting loop that takes the clock, the waiting and a cancel signal as inputs.
