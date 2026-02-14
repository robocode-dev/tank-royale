# ADR-0013: Bot Configuration via Environment Variables

**Status:** Accepted  
**Date:** 2026-02-14 (Documenting historical decision)

---

## Context

Bots need to discover the server and provide identity information. The configuration mechanism must work across Java,
Python, .NET, and containerized environments.

**Problem:** How should bots receive connection and identity configuration?

---

## Decision

Use **environment variables** as the primary configuration mechanism, with constructor parameters as override.

**Variables:** `SERVER_URL`, `SERVER_SECRET`, `BOT_NAME`, `BOT_VERSION`, `BOT_AUTHORS`, `BOT_GAME_TYPES`,
`BOT_INITIAL_POS`, `TEAM_ID`, `TEAM_NAME`, `BOT_BOOTED`, and others. System properties (`-Dserver.url`) serve as
fallback for `SERVER_URL` and `SERVER_SECRET`.

---

## Rationale

- ✅ Works identically across all languages (Java, Python, .NET)
- ✅ Enables Booter to inject identity when spawning bot subprocesses
- ✅ Container/cloud-friendly (12-factor app pattern)
- ✅ No config file format to standardize across platforms
- ❌ Not discoverable (must know variable names)
- ❌ Environment-level scope (no per-instance config without process isolation)

---

## References

- [EnvVars.java](/bot-api/java/src/main/java/dev/robocode/tankroyale/botapi/internal/EnvVars.java)
- [ADR-0005: Independent Deployable Components](./0005-independent-deployable-components.md) (Booter subprocess pattern)
