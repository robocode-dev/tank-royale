---
id: P-002
type: plan
status: active
links: [G-001]
title: TypeScript Bot API reaches npm
provenance: inferred
---

# P-002 — TypeScript Bot API reaches npm

The TypeScript bot API package (`@robocode.dev/tank-royale-bot-api`) builds but cannot be published to the npm registry through the standard Gradle workflow, unlike Java (Maven Central via Gradle) and Python (PyPI via Gradle). Serves G-001: cross-platform play only counts when every official Bot API is installable through its platform's native channel.

Carried over from the pending OpenSpec change `add-typescript-bot-api-npm-publish` at extraction (CH-001); its criteria live in draft capability CAP-013.

| ID | Milestone | Exit criterion | Status | Evidence |
|---|---|---|---|---|
| M-004 | npm publishing via Gradle | `npmPack`, `npmPublishDryRun`, `npmPublish` Gradle tasks exist and a dry-run publish of the package succeeds from a clean checkout; CAP-014 flips to `active` with its ACs tested | todo | |
