# ADR-0012: R8 Code Shrinking

**Status:** Accepted  
**Date:** 2026-02-14

---

## Context

All distributable JVM components (Server, GUI, Booter, Recorder) produce fat JARs with all dependencies bundled.
These JARs need shrinking to remove unused code and reduce artifact size.

**Problem:** Which code shrinking tool to use?

---

## Decision

Use **R8** (Google's code shrinker) in **shrink-only mode** (`-dontoptimize -dontobfuscate`) for all distributable JARs.

**Configuration:** ProGuard-compatible rule files (`r8-rules.pro`) per component. R8 runs as a Gradle task after
Shadow JAR creation. Version pinned in `gradle/libs.versions.toml`.

---

## Rationale

- ✅ Faster than ProGuard with equivalent shrinking results
- ✅ ProGuard-compatible rule syntax (easy migration)
- ✅ Actively maintained by Google (Android toolchain)
- ✅ Shrink-only mode preserves debuggability (stack traces, class names)
- ❌ Can constrain library versions (e.g., Clikt 5.1.0 incompatible with R8)
- ❌ Android-oriented tool used outside its primary ecosystem

---

## References

- [server/r8-rules.pro](/server/r8-rules.pro), [gui/r8-rules.pro](/gui/r8-rules.pro)
- [gradle/libs.versions.toml](/gradle/libs.versions.toml) (R8 version pin)
