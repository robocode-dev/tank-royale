---
id: CH-040-tasks
type: tasks
status: open
links: [CH-040]
title: Task breakdown for CH-040
---

# CH-040 — Tasks

- [x] Reserve CH-040, branch from the accepted tip of `main`, and capture the full-route proposal.
- [x] Commit and push the proposal, then open draft PR https://github.com/robocode-dev/tank-royale/pull/268 before implementation.
- [x] Inventory executable test entry points and existing criterion mappings across JVM, .NET, Python, and TypeScript suites.
- [x] Define the repository's one-purpose-per-test mapping for each supported framework and record any non-blocking implementation choice in the change workspace.
- [x] Tag JVM tests and add a JVM architecture guard covering Java and Kotlin test executables.
- [x] Tag .NET tests and add an NUnit architecture guard covering discovered test cases.
- [x] Tag Python tests, register the purpose markers, and add a pytest architecture guard covering collected test items.
- [x] Tag TypeScript tests and add a Vitest architecture guard covering discovered test cases and their purpose groups.
- [x] Update the test registry and testing guidance to describe the enforced purpose contract.
- [x] Review criteria for promotion; no extracted criteria were promoted because the existing `TR-API-*` identifiers are legacy registry IDs rather than canonical namespaced corpus AC IDs. Update M-002 bookkeeping with platform evidence.
- [x] Run focused platform suites, the full repository build, `clue validate`, and `git diff --check`.
