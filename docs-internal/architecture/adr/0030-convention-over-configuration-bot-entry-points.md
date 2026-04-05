# ADR-0030: Template-based Booting and Base Convention

**Status:** Proposed
**Date:** 2026-04-05

---

## Context

The Robocode Tank Royale booter needs a flexible way to start bots on various platforms (JVM, .NET, Python, etc.) without hardcoding the launch commands in the booter's core logic. 

**Problem:** 
1. How to support multiple platforms without creating OS-specific scripts (`.sh`, `.bat`) for every bot?
2. How to minimize redundancy in the bot configuration files where the entry point often matches the directory name?

---

## Decision

1.  **Template-based Booting:** Introduce `.boot` templates for each platform (JVM, .NET, Python). These templates define how to launch a bot using placeholders:
    - `${base}`: The main class, project, or script name.
    - `${botName}`: The display name of the bot.
    - `${classPath}`: Classpath for JVM bots (e.g., `../lib/*`).
2.  **`base` Property:** Use the `base` property in the bot's JSON configuration as the primary identifier for the entry point (replacing any previous platform-specific fields).
3.  **Convention-over-Configuration:** 
    - The `base` property is optional.
    - If missing, the Booter automatically uses the **name of the parent directory** as the default value for `base`.
    - Boot templates append the appropriate file extension (e.g., `.java`, `.py`) or command prefix based on the platform.

---

## Rationale

- ✅ **Decoupled Logic:** Booting logic is moved from Kotlin code to maintainable templates.
- ✅ **Scriptless Bots:** Most bots no longer require companion shell/batch scripts.
- ✅ **Reduces Redundancy:** Removes the need for a `base` field when it matches the directory name.
- ✅ **Improved DX:** Simplifies bot creation and distribution.
- ✅ **Flexibility:** Explicit `base` configuration still allows overriding the convention.

---

## Consequences

- **Positive:** Cleaner bot distributions; easier to add support for new platforms by adding a `.boot` template.
- **Negative:** Developers must follow the directory naming convention or provide an explicit `base` field if their entry point differs.

---

## References

- [TemplateBooter.kt](/booter/src/main/kotlin/dev/robocode/tankroyale/booter/process/TemplateBooter.kt) (Implementation of template parsing)
- [Command.kt](/booter/src/main/kotlin/dev/robocode/tankroyale/booter/commands/Command.kt) (Implementation of the `base` fallback logic)
- [improve-booter-with-templates](/openspec/changes/archive/improve-booter-with-templates/) (Original feature proposal)
