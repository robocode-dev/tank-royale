yes## Why

The current Booter relies on OS-specific scripts (e.g., `.bat`, `.sh`, `.command`) within each bot directory to launch bots. This is fragile, hard to maintain, and requires bot developers to provide these scripts for every platform they want to support. By introducing a template-based booting mechanism, we can simplify bot deployment and provide a more consistent experience across different platforms and programming languages.

## What Changes

- **Template-based Booting**: Introduce a set of "boot templates" (stored in a shared library/folder) that define how to launch common bot types (e.g., JVM, .NET, Python, Node.js).
- **Bot Configuration Integration**: Update the Booter to look for a `boot.json` or extend the existing `{bot-name}.json` to include information needed to select and fill a boot template.
- **Fallback Mechanism**: Maintain support for existing OS-specific scripts as a fallback or override mechanism.
- **Shared Library for Templates**: Create a location for these templates that can be easily updated and shared across the Tank Royale ecosystem.
- **Sample Bot Build Update**: Update the Gradle build scripts for sample bots to omit generating OS-specific scripts (`.cmd`, `.sh`) when the bot can be booted using a standard template.
- **Bot Configuration Requirement**: Ensure that bots intended for template-based booting have the necessary metadata (`platform`, `programmingLang`, and `main`) in their JSON configuration.

## Capabilities

### New Capabilities
- `template-based-booting`: Ability to boot bots using predefined templates based on their platform and language.
- `boot-template-library`: A centralized collection of templates for common bot environments.
- `scriptless-sample-bots`: Sample bots can be distributed without platform-specific boot scripts.

### Modified Capabilities
- `battle-runner`: The battle runner's interaction with the booter may be updated to provide better feedback on the booting process.

## Impact

- `booter` module: Significant changes to `BotBooter`, `ScriptFinder`, and potentially `BootCommand`.
- `bot-api`: May need minor updates to standard bot configuration files if we decide to store booting hints there.
- `sample-bots`: Can be updated to remove redundant scripts if templates are used instead.
- `gui`: Might need updates to show more detailed information about how a bot is being booted.
