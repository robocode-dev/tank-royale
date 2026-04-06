# ADR-0030: Template-based Booting and Base Convention

**Status:** Accepted
**Date:** 2026-04-05

## Context and Problem Statement

The Robocode Tank Royale booter needs a flexible way to start bots on various platforms (JVM, .NET, Python, etc.) without hardcoding the launch commands in the booter's core logic. 

**Problem:** 
1. How to support multiple platforms without creating OS-specific scripts (`.sh`, `.bat`) for every bot?
2. How to minimize redundancy in the bot configuration files where the entry point often matches the directory name?

## Decision Outcome

1.  **Template-based Booting:** Introduce `.boot` templates for each platform (JVM, .NET, Python). These templates define how to launch a bot using placeholders:
    - `${base}`: The main class, project, or script name.
    - `${botName}`: The display name of the bot.
    - `${classPath}`: Classpath for JVM bots (e.g., `../lib/*`).
2.  **`base` Property:** Use the `base` property in the bot's JSON configuration as the primary identifier for the entry point (replacing any previous platform-specific fields).
3.  **Convention-over-Configuration:** 
    - The `base` property is optional.
    - If missing, the Booter automatically uses the **name of the parent directory** as the default value for `base`.
    - Boot templates append the appropriate file extension (e.g., `.java`, `.py`) or command prefix based on the platform.

### Pros and Cons of the Options

#### Template-based Booting

*   **Good:** Decoupled logic — booting logic is moved from Kotlin code to maintainable templates.
*   **Good:** Scriptless bots — most bots no longer require companion shell/batch scripts.
*   **Good:** Flexibility — explicit `base` configuration still allows overriding the convention.
*   **Good:** Improved DX — simplifies bot creation and distribution.

#### Convention-over-Configuration (Base fallback)

*   **Good:** Reduces redundancy — removes the need for a `base` field when it matches the directory name.
*   **Bad:** Developers must follow the directory naming convention or provide an explicit `base` field if their entry point differs.

## Rationale

The decision to move towards template-based booting and a "convention-over-configuration" model for bot entry points was driven by the desire to simplify the developer experience and make the platform more extensible. By decoupling the launch logic from the booter's core code, we can easily add support for new platforms without modifying and recompiling the booter itself.

## Consequences

*   Cleaner bot distributions; easier to add support for new platforms by adding a `.boot` template.
*   Consistency across all bot APIs regarding how they are launched.

## References

*   [TemplateBooter.kt](/booter/src/main/kotlin/dev/robocode/tankroyale/booter/process/TemplateBooter.kt) (Implementation of template parsing)
*   [Command.kt](/booter/src/main/kotlin/dev/robocode/tankroyale/booter/commands/Command.kt) (Implementation of the `base` fallback logic)
*   [improve-booter-with-templates](/openspec/changes/archive/improve-booter-with-templates/) (Original feature proposal)
