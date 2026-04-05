## Why

The current bot discovery logic in the `booter` strictly requires a `.json` file matching the bot's directory name. This is inflexible for bots that prefer to set their configuration properties (name, version, authors, etc.) at runtime through the Bot API before connecting to the server. By making the `.json` file optional, we allow for a more streamlined developer experience where a bot can be started "as-is" without extra configuration files. This change is associated with version **0.39.0**.

## What Changes

- **Version Bump**: The project version is increased to **0.39.0** to reflect the major shift to "Convention-over-Configuration" and "Scriptless Bots."
- **Booter Discovery**: Modify `DirCommand` to discover bot directories even if they lack a `.json` config file.
- **Default Boot Entry**: Update `Command.getBootEntry` to provide a fallback `BootEntry` when the `.json` file is missing. The default name will be derived from the directory name.
- **Platform Detection**: Enhance `TemplateBooter` or `BotBooter` to detect the bot's platform (JVM, .NET, Python) by inspecting files in the directory if not specified in a `.json` file.
- **Runtime Validation**: Ensure that all Bot APIs (Java, Python, .NET) strictly enforce that required properties (`name`, `version`, `authors`) are set before a connection is established, throwing a `BotException` otherwise.
- **VitePress Documentation**: Update the documentation in `docs-build` (VitePress) to reflect that `.json`, `.sh`, and `.cmd` files are optional and explain the new discovery/validation rules.
- **Example Updates**: Update `MyFirstBot` examples for Java, Python, and C# across all repositories and documentation to demonstrate how to run without external configuration files, by setting required properties in code.
- **Test Bots**: Create minimal test bots for Java, Python, and C# in `bot-api/tests/bots` that demonstrate running without any external `.sh`, `.cmd`, or `.json` files. Use the existing `BattleRunner` as-is for automated verification of these bots.
- **Negative Testing**: Implement automated tests to verify that `BotException` is correctly thrown with a helpful message when required properties are missing at runtime.

## Capabilities

### New Capabilities
- `runtime-config-validation`: Specification for how Bot APIs must validate required properties at runtime and handle missing configurations.
- `booter-fallback-discovery`: Specification for how the booter discovers and classifies bots without explicit metadata files.
- `bot-api-test-verification`: Integration of `BattleRunner` to verify bots in `bot-api/tests/bots`.
- `scriptless-example-bots`: Standardized `MyFirstBot` examples that function without `.sh`, `.cmd`, or `.json` files.

### Modified Capabilities
- `user-documentation`: Update documentation in `docs-build` (VitePress) to reflect that `.json` files are optional and explain how to configure bots via code or environment variables.
- `bot-api-handshake`: Update the handshake process in all APIs to include runtime property validation.

## Impact

- `booter`: Changes to `DirCommand.kt`, `Command.kt`, and potentially `TemplateBooter.kt` for platform detection.
- `bot-api` (Java, Python, .NET): Ensure `BaseBot` and `EnvVars` consistently enforce required fields and update `MyFirstBot` examples.
- `bot-api/tests/bots`: New test bots for verification.
- `testing`: Integration of `BattleRunner` for testing the new discovery logic.
- `docs-build`: Updates to the VitePress documentation and bot development tutorials.
- `docs`: Updates to bot development guides.
