# ADR-0031: Optional Bot Configuration Files and Runtime Property Validation

**Status:** Accepted
**Date:** 2026-04-05

## Context and Problem Statement

Traditionally, every Robocode Tank Royale bot required a `.json` configuration file to define its metadata (name, version, authors, etc.) and its boot instructions. This file was used by the `booter` to discover the bot and by the Bot API to identify the bot to the server.

**Problem:**
1. Requiring a `.json` file for every bot, even for simple test bots or scripts, adds boilerplate and extra files to manage.
2. Bots are already capable of setting their own properties programmatically at startup.
3. If a `.json` file is missing, the `booter` currently ignores the directory, making it impossible to "just run" a bot from a folder.

## Decision Outcome

1. **Optional `.json` Files:** The `.json` configuration file is now optional for all bot platforms (Java, Python, .NET).
2. **Booter Discovery Rules:** If no `.json` file is present, the booter will:
    - Derive the bot's display name from its directory name (e.g., "Corners" -> "Corners").
    - Use platform detection logic to identify the type of bot.
    - Provide default metadata values (version: "0.0.1", authors: "unknown").
    - Use the base convention defined in ADR-0030 to find the entry point.
3. **Property Priority:**
    - If a `.json` file is present, its properties are used as the primary source of metadata.
    - If no `.json` file is present, the bot **must** set all required properties (`name`, `version`, `authors`) programmatically at runtime before connecting to the server.
    - Property precedence: Bot (Runtime) > Environment variables > JSON file.
4. **Runtime Validation:**
    - Bot APIs (Java, Python, .NET) must validate that all required properties are set during the connection handshake.
    - If any required property is missing AND no `.json` file was provided, the API must throw a `BotException`.
5. **Standard Error Message:** The `BotException` should clearly state which fields are missing and mention that a `.json` file can be provided as an alternative to setting them programmatically.
    - Example: `Required bot property 'name' is missing. You must set this property in your bot code or provide a .json configuration file.`

### Pros and Cons of the Options

#### Optional JSON Files

*   **Good:** Reduced boilerplate — simplifies bot creation by removing the need for a mandatory sidecar file.
*   **Good:** Improved DX — allows for "naked" bot scripts that are fully self-contained.
*   **Bad:** Errors that were previously caught at "discovery time" by the booter may now only appear at "runtime" when the bot attempts to connect.

#### Runtime Property Validation

*   **Good:** Clearer errors — runtime validation ensures that bots provide necessary identity information, with helpful guidance on how to fix missing data.
*   **Good:** Flexibility — bots can set properties dynamically based on runtime conditions.

## Rationale

Allowing bots to run without a `.json` file aligns with modern "convention-over-configuration" principles. It lowers the barrier to entry for new developers. 

The requirement for `name`, `version`, and `authors` is maintained even for config-less bots for the following reasons:
1.  **Identity and Matching:** `name` and `version` are critical for the server and battle runner to uniquely identify and match bot processes (see ADR-0026).
2.  **Authorship:** The `authors` field is required to ensure credit is given to the creator and provides a fallback for uniqueness if two bots share the same name and version.
3.  **Protocol Compliance:** The Tank Royale protocol expects a complete `BotInfo` object; omitting these fields would break the handshake schema and GUI display.

## Consequences

*   Easier bot prototyping and testing; more flexible bot distribution.
*   Maintains backward compatibility as existing bots with `.json` files continue to work as-is.

## References

*   [ADR-0030: Template-based Booting and Base Convention](0030-convention-over-configuration-bot-entry-points.md)
*   [Support Bot Discovery Without JSON](/openspec/changes/support-bot-discovery-without-json/proposal.md) (Feature proposal)
