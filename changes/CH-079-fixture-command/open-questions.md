---
id: CH-079-open-questions
type: open-questions
status: resolved
links: [CH-079]
title: What the command is called and where it is documented
---

## What should the command be called, and where should it be documented?

The plan calls it the single fixture-integration command and says it must be documented. Tank Royale's other developer commands are Gradle tasks described in the development guide, and the Rumble reset tool is a script in the `scripts/rumble` folder described in the participation design.

Recommendation: the Gradle task is `rumbleFixtureCheck`, run as `./gradlew rumbleFixtureCheck`, because that is how every other Tank Royale check is run and it needs no extra installation beyond what the build already needs. It is documented in the participation design next to the reset tool, and later also in the development guide when the matrix is complete. The alternative is a script in `scripts/rumble` that calls Gradle, which adds a second way to run it for no gain.

Resolved by the maintainer on 2026-11-02, approving the recommendation: the Gradle task is `rumbleFixtureCheck`, documented together with the other Gradle commands in the development guide and in the participation design next to the reset tool.
