# ADR-0001: Monorepo Build Strategy

**Status:** Accepted  
**Date:** 2026-02-14 (Documenting historical decision)

---

## Context

Tank Royale consists of multiple components (server, GUI, booter, recorder, Bot APIs, sample bots, docs). These share
schemas, libraries, and versioning.

**Problem:** Use a monorepo or split into separate repositories?

---

## Decision

Use a **single Gradle multi-project monorepo** with Kotlin DSL (`build.gradle.kts`, `settings.gradle.kts`).

**Structure:** Shared modules (`lib:common`, `lib:client`), component modules (server, GUI, booter, recorder),
API modules (bot-api:java/dotnet/python), and sample bots — all in one repository with a unified version catalog
(`gradle/libs.versions.toml`).

---

## Rationale

- ✅ Atomic cross-component changes (protocol + server + Bot APIs in one commit)
- ✅ Shared version catalog and build plugins
- ✅ Single CI pipeline validates everything together
- ✅ Schema changes immediately visible to all consumers
- ❌ Larger clone size
- ❌ Build times grow with project size (mitigated by Gradle incremental builds)

**Alternatives rejected:**

- **Separate repos per component:** Schema/protocol changes require coordinated multi-repo PRs
- **Git submodules:** Complex dependency management, frequent sync issues

---

## References

- [settings.gradle.kts](/settings.gradle.kts)
- [gradle/libs.versions.toml](/gradle/libs.versions.toml)
