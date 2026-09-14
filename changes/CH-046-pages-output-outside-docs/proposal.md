---
id: CH-046
type: change
status: open
links: []
title: Keep generated Pages output outside the Cliewen corpus
---

# CH-046 — Keep generated Pages output outside the Cliewen corpus

## What

Move every generated documentation artifact from repository-root `/docs` to a dedicated staging directory under the root build tree. Update the Gradle documentation tasks, API-documentation copy destinations, GitHub Pages workflow, release guidance, ignore rules, and durable documentation policy so local verification and CI deployment use the same isolated output tree. Add regression evidence that documentation generation and publication configuration cannot target the Cliewen corpus.

## Why

`/docs` is the Cliewen system-of-record, but the current documentation build also copies VitePress HTML, API references, assets, and intermediate generator output into that directory. A local `upload-docs` run therefore pollutes the corpus and causes `clue validate` to interpret generated website directories as malformed corpus artifacts. Generated output must be disposable build state and must never share the permanent corpus boundary.

## Scope

- Stage the complete generated Pages site under the root build directory.
- Update all Java, .NET, Python, TypeScript, Runner, and VitePress copy tasks to use that staging tree.
- Update Pages verification and upload steps to consume the staging tree.
- Update release and contributor guidance plus the documentation-methodology decision.
- Add acceptance criteria and automated regression evidence for the corpus boundary.
- Remove currently generated ignored output from `/docs` after the producers no longer target it.

## Non-goals

- Changing user-facing documentation content or URLs.
- Changing which API references or VitePress pages are published.
- Publishing a release or deploying GitHub Pages from this branch.
- Moving permanent Cliewen corpus artifacts out of `/docs`.

## Compatibility

The published site layout remains unchanged inside the Pages artifact. Only the repository-local and CI staging location changes, so external documentation URLs and release artifacts remain compatible.

## Plan

This is a plan-less correction to the repository's documentation-generation methodology. No active plan promises this work; the maintainer directly required that generated output never be written under `/docs`.
