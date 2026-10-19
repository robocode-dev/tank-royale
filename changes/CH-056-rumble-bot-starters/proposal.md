---
id: CH-056
type: change
status: open
links: [M-022, P-006, CAP-014, G-003, ADR-050]
title: Runnable bot starters with an explicit official-API runtime dependency boundary
---

# Runnable bot starters

The reproducible bot-submission milestone needs copy-and-rename starters that actually run through the catalog's source-only workflow. Deliver Java, C#, Python and TypeScript starters, a JSON-only TwinDuel team example, truthful smoke and executable-bit instructions, and checks using the real validator. The source-only catalog remains the reviewed source of eligible bots. This implements the existing milestone; it does not close it until a first bot and a version update have reached merged catalog entries through the published instructions.

The accepted dependency wording permits the official Bot API and standard library. The published TypeScript API delegates Node WebSocket transport to its declared `ws` runtime peer. Clarify the contract to permit the official API and its declared runtime dependencies, while continuing to reject unrelated bot libraries. The maintainer explicitly selected the tracked route with this clarification in the 2026-10-19 conversation. Record the enduring boundary in the decision on official API runtime dependencies; no new scoring format, client session feature or release is included.

## Challenge

The risky assumption is that loading an API proves a starter can join a battle. A smoke branch can print the expected marker while its normal path lacks a runtime peer. Keep the smoke promise bounded to startup and separately run real battles with every renamed starter. The cheapest useful TypeScript check is its real connection through the official API using only the API and its declared `ws` peer. If another library, custom transport or permission exception is necessary, stop and reassess the dependency boundary. The alternative is to withhold the TypeScript starter until the API bundles its transport; silently bypassing the current rule would give reviewers a false dependency claim.

A green validator can still leave authors unable to copy the directory, rename its entry point or commit the executable bit from Windows. Exercise the documented rename steps in temporary submissions and record native versus container conditions separately. Local catalog generation is useful evidence but is not a substitute for the milestone's human-merged first submission and version update.

## Scope and acceptance

The external bot repository owns templates, launchers and validation workflow changes. Tank Royale owns the dependency decision, catalog criteria/design and author guide. Add focused positive and negative integration checks for the dependency boundary (RBC-006); keep the catalog's existing external-evidence registration limitation explicit. Existing acceptance criteria for valid submissions, rejection, ownership and team identities remain unchanged. There is no corpus vision document; the change proceeds under the accepted learning-and-competition purpose and the outside-contributor participation goal.

Prepared direct work on three starters and their tests predates this tracked proposal; the dependency clarification and TypeScript implementation follow this proposal. No implementation depends on the unmerged milestone-bookkeeping PR 285: this branch starts from accepted main. The repository's human-merged PR constraint governs acceptance; no agent merges or pushes this tracked change to main.
