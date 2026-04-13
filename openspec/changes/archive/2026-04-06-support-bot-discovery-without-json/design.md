## Context

Currently, the `booter` strictly requires a `.json` configuration file for each bot. This file contains metadata like name, version, and authors, as well as the platform (JVM, .NET, Python). If this file is missing, the `booter` ignores the directory. 
Furthermore, while Bot APIs have fallback mechanisms for some properties (e.g., via environment variables), they don't strictly enforce that these properties are set before connecting to the server if the `.json` file was missing or incomplete.

## Goals / Non-Goals

**Goals:**
- Allow the `booter` to discover and boot bots from directories without a `.json` file.
- Implement platform detection in the `booter` for bots without a `.json` file.
- Enforce required bot properties (`name`, `version`, `authors`) at runtime in all Bot APIs (Java, Python, .NET).
- Provide minimal test bots that demonstrate "config-less" operation.

**Non-Goals:**
- Removing support for `.json` files (they remain the primary and recommended configuration method).
- Automatically fixing bots that are missing required properties (they must fail with a clear error).
- Supporting complex platform detection (e.g., multi-module projects or custom build systems) beyond basic file extensions.

## Decisions

### 1. Booter Discovery Logic Update
**Decision**: Modify `DirCommand.listBotDirectories()` to include all directories, then filter them in `processDirectory()`.
**Rationale**: Currently `listBotDirectories()` only adds directories that contain a `.json` file. By moving the logic to `processDirectory()`, we can provide a default `BootEntry` when the `.json` is missing.
**Alternatives**: 
- Keep `listBotDirectories()` as is and add a new command for "unstructured" bots. *Rejected* because it complicates the `booter` API.

### 2. Default Metadata Generation
**Decision**: When `.json` is missing, set `name` and `base` to the directory name. `version` will default to "0.0.0" and `authors` to an empty list in the `booter` (to be filled by the bot at runtime).
**Rationale**: The directory name is the most reliable piece of information available.

### 3. Heuristic Platform Detection
**Decision**: Use file extensions in the bot directory to guess the platform:
- `.jar`, `.class` -> `jvm`
- `.py` -> `python`
- `.dll`, `.exe` -> `dotnet`
**Rationale**: This covers the majority of use cases for simple bots.
**Alternatives**: 
- Always require platform in `.json`. *Rejected* as it defeats the purpose of the change.

### 4. Runtime Validation in Bot APIs
**Decision**: Add a `validate()` method (or equivalent) in `BaseBotInternals` that is called during the connection handshake. It checks if `name`, `version`, and `authors` (non-empty list) are present in the `BotInfo`.
**Rationale**: This ensures that even if the `booter` allows starting the bot, the server still receives valid metadata. If metadata is missing, the bot fails fast locally with a `BotException`.

### 5. BattleRunner Integration & Test Bots
**Decision**: Use the existing `bot-api/tests/bots` directory to house minimal test bots for all supported platforms (Java, Python, .NET). These bots will be configured entirely in code and lack any `.json`, `.sh`, or `.cmd` files. Use the existing `BattleRunner` as-is to programmatically discover, boot, and verify these bots in a controlled test environment.
**Rationale**: Reusing `bot-api/tests/bots` keeps all test-related bots in a single, well-known location. Using `BattleRunner` as-is ensures we verify the "real-world" discovery and booting logic without introducing additional testing layers or complexity.
**Negative Testing**: Automated tests will be added to `bot-api/tests` that intentionally omit required properties and assert that a `BotException` with the correct message is thrown during the handshake.

## Risks / Trade-offs

- **[Risk] Multiple platforms in one directory** -> **[Mitigation]** The `booter` will use a priority order (e.g., JVM > Python > .NET) or simply pick the first match. Users should use a `.json` file if they have complex setups.
- **[Risk] Directory name contains spaces or special characters** -> **[Mitigation]** The `booter` already handles directory names; using them as bot names should be safe as long as they are valid strings.
- **[Risk] Bot fails at runtime instead of boot time** -> **[Mitigation]** This is a trade-off for flexibility. The `BotException` will provide a clear message to the developer.

## MyFirstBot & Documentation Updates

### 1. VitePress Documentation (docs-build)
- **Rules**: If `.json` is present, its properties are used. If missing, properties set in code are used. If properties are missing, throw `BotException` listing missing fields. The documentation should explicitly state that `.json` is now a fallback/optional mechanism.
- **Boot Templates**: Clarify that script files (`.sh`/`.cmd`) are optional due to template-based booting. The `base` property (entry point) defaults to the directory name.
- **Discovery**: Update the explanation of how the booter finds bots to include directory-based discovery without `.json` for version 0.39.0+.

### 2. MyFirstBot Example Updates
- **Strategy**: Update `MyFirstBot` examples in Java, Python, and C# to show they can run without any companion `.json`, `.sh`, or `.cmd` files. This involves updating the code in the respective API repositories and the snippets used in the documentation.
- **Action**: Modify the code to set `name`, `version`, and `authors` directly in the bot constructor or main method. Remove references to `BotInfo.fromFile("MyFirstBot.json")` where appropriate and show it's optional. Update tutorials to emphasize this new capability as of version 0.39.0.
