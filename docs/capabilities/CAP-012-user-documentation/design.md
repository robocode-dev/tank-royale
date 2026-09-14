---
id: CAP-012-design
type: design
status: draft
links: [CAP-012]
title: Design notes for CAP-012 (user-documentation)
provenance: inferred
reversal-cost: low
---

# CAP-012 design

User-facing documentation sources live under `web/docs`; API reference sources remain with their Java, .NET, Python, TypeScript, and Runner implementations. The `upload-docs` Gradle task generates those independent sources into one disposable `build/pages` staging tree whose internal layout is the GitHub Pages layout. The deployment workflow verifies required API entry points there and uploads that directory directly.

Repository-root `/docs` is exclusively the Cliewen corpus. Documentation generators derive their destinations through the guarded root-build layout and never use `/docs` as staging, so local verification cannot introduce generated website files into corpus validation.
