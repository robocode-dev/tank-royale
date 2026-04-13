# ADR-0017: Recording Format (ND-JSON + Gzip)

**Status:** Accepted  
**Date:** 2026-02-14 (Documenting historical decision)

---

## Context

Tank Royale needs a recording format for battle replay and analysis.

**Problem:** What file format should recordings use?

---

## Decision

Use **gzip-compressed Newline-Delimited JSON (ND-JSON)** with `.battle.gz` extension.

**Architecture:** The Recorder connects to the server as an Observer client, streams each game event as one JSON line
into a `GZIPOutputStream`. Recording is append-only and decoupled from the game engine.

---

## Rationale

- ✅ Human-readable (gunzip + any JSON parser)
- ✅ Streaming writes (no buffering entire game in memory)
- ✅ Simple tooling (grep, jq, standard libraries)
- ✅ Recorder is just an Observer — no special server support needed
- ❌ Larger than binary formats even with gzip
- ❌ No random access without decompressing

**Alternatives rejected:**

- **Custom binary format:** Hard to debug, requires custom tooling
- **Uncompressed JSON:** Too large for real-time game data

---

## References

- [GameRecorder.kt](/recorder/src/main/kotlin/dev/robocode/tankroyale/recorder/core/GameRecorder.kt)
- [ADR-0007: Client Role Separation](./0007-client-role-separation.md) (Recorder as Observer)
