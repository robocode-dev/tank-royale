---
id: CAP-013
type: capability
status: draft
links: [G-001]
title: TypeScript Bot API npm publishing
provenance: verified
---

# CAP-013 — TypeScript Bot API npm publishing

Publish the TypeScript Bot API package (`@robocode.dev/tank-royale-bot-api`) to the npm registry through Gradle, so every official Bot API is installable through its platform's native channel (Java via Maven Central, Python via PyPI, TypeScript via npm).

Implemented: the `npmPack` and `npmPublish` Gradle tasks live in `bot-api/typescript/build.gradle.kts`, the `/release` skill drives `npmPublish`, and the package is live on npm. Criteria stay `draft` until their tests are wired (P-001/M-002).

Extracted from `openspec/specs/typescript-bot-api-npm-publish/spec.md` at CH-001 and reconciled to the shipped implementation at CH-002; the source spec's original requirement prose is preserved as comments in [criteria.md](criteria.md).
