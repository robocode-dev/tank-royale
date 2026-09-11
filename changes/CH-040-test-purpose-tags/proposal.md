---
id: CH-040
type: change
status: open
links: [P-001, M-002]
title: Complete test-purpose tagging across the repository
---

# CH-040 — Complete test-purpose tagging across the repository

## Acceptance brief

- Plan: P-001/M-002 remains wanted and is complete. Every discovered executable test in JVM, .NET, Python, and TypeScript has one effective purpose, and a platform architecture guard enforces it.
- Vision: this change proceeds under `docs/vision.md` and preserves the product's cross-platform purpose.
- Criteria: no acceptance-criterion meaning changed or was added, and no draft criterion was promoted. Existing `TR-API-*` identifiers remain legacy registry identifiers; active GTD-001 evidence retains its AC, Unit, Positive, and Negative metadata.
- Proof: the four focused architecture guards, platform suites, `./gradlew clean build`, `clue validate`, and `git diff --check` pass on the reviewed candidate.
- Merge: accepting CH-040 binds the test-purpose declarations, guard methodology, registry guidance, and M-002 bookkeeping. No unmerged base or superseded behavior is involved. Documentation updated: `docs/README.md`, `docs/plans/P-001-cliewen-adoption.md`, `bot-api/tests/TEST-REGISTRY.md`, and Python test guidance; architecture/design overviews are unchanged because this is test/governance infrastructure only.
## What

Complete P-001/M-002 by making every executable test in the repository declare exactly one effective purpose: its canonical acceptance-criterion ID or `Unit`, `Sanity`, or `Arch`. Apply the platform-appropriate tag or grouping mechanism to the JVM, .NET, Python, and TypeScript suites, and add one architecture guard per platform that fails when a discovered test has zero or multiple effective purpose declarations.

Where an existing test already proves a registered criterion, preserve that canonical ID. Tests that are not evidence for a criterion receive the narrowest honest generic purpose. Existing framework category labels required by an active evidence contract remain as supporting metadata; an acceptance ID takes precedence over a generic purpose when determining the effective purpose. Update the test registry and testing guidance where the enforced contract differs from the current migration notes, and close M-002 only after all platform guards and focused suites pass.

## Why

The corpus currently names test-purpose tagging as the remaining adoption door, but the suites contain a mixture of criterion tags, framework category tags, and untagged tests with no executable guard. That leaves the acceptance-criterion-to-test trace dependent on human inspection and makes newly added untagged tests easy to miss.

This change is test and governance infrastructure only; it does not change product runtime behavior or acceptance-criterion meaning.

## Route

Full, because completing M-002 makes the repository's test-purpose methodology enforceable and changes when extracted criteria may be treated as wired evidence.

## Plan

Serves [P-001/M-002](../../docs/plans/P-001-cliewen-adoption.md).

## Scope

- Inventory test entry points and map existing criterion evidence without inventing new criterion IDs.
- Add exactly-one-purpose declarations to the JVM, .NET, Python, and TypeScript test suites.
- Add platform architecture guards that validate the discovered executable tests.
- Update test registry/guidance and promote only criteria whose existing evidence is now wired without changing their meaning.
- Run the relevant platform suites, the repository build, and `clue validate` before digesting the change.

## Non-goals

- Changing production behavior or acceptance-criterion wording.
- Creating new product capabilities or filling unrelated test-coverage gaps.
- Treating a generic purpose tag as evidence for an acceptance criterion.
