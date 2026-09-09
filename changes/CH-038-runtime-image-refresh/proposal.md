---
id: CH-038
type: change
status: open
links: [P-004, M-016, CAP-016]
title: Refresh Rumble container runtimes and automate updates
---

# CH-038 — Refresh Rumble container runtimes and automate updates

## What

Move the Rumble client container to the LTS-first runtime lanes Java 25, .NET 10, Python 3.14, and Node.js 24, and add a scheduled workflow that proposes reviewed runtime refreshes.

## Why

The current image stays on older Java, .NET, Python, and Node.js lines even though bots are likely to use newer stable language features and libraries. Runtime pins are also maintained manually, so they can become stale or diverge between the image, native preflight check, CI, and user guides.

The image should provide a current stable execution environment without changing the minimum versions supported by the Tank Royale Bot APIs. A monthly, reviewable refresh pull request keeps the image current while requiring the complete four-language container smoke test to pass before a human merges it.

## Route

Recommended route: full. This change materially revises the Rumble runtime-support policy and introduces a new scheduled maintenance workflow. Discovery would change the route only if the work proved to be a defect correction against an unchanged policy; the requested LTS-first policy and automatic refresh process are new behavior.

## Plan

Serves [P-004/M-016](../../docs/plans/P-004-rumble-hardening.md) and adds the runtime-refresh evidence needed by [CAP-016](../../docs/capabilities/CAP-016-rumble-client/README.md).

## Scope

- Make `src/main/resources/runtime-versions.properties` the runtime policy source, update the Dockerfile to use Ubuntu 26.04 packages and the selected LTS lanes, and keep Python dependencies in the image-owned virtual environment.
- Derive native Python preflight command names from the configured Python minor version and update runtime-check tests and messages for the new lanes.
- Add a checked-in release-metadata updater and monthly GitHub Actions workflow. It updates only configured LTS lanes and exact Node.js patches, records the official source URLs in the pull request, fails closed on malformed metadata, and never auto-merges.
- Extend Docker CI to build and run Java, C#, Python, and TypeScript sample bots through the image, using the real `BattleResults(numberOfRounds=1)` output as the success assertion while retaining the hardened container checks.
- Update the Rumble README, Tank Royale Rumble guides, CAP-016 design, and P-004 bookkeeping so runtime pins, the container boundary, and the refresh process are described consistently.

## Non-goals

- Changing the minimum Python, Node.js, Java, .NET, or TypeScript versions supported by the public Bot APIs.
- Installing a global TypeScript compiler in the image; TypeScript remains project-local and Node.js is the container runtime.
- Making Podman a second hosted CI target or auto-publishing a production image.
- Automatically merging runtime update pull requests.
