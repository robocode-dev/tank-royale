# Design: Template-based Booting

## Overview
Introduce a template-based mechanism to boot bots, reducing the need for platform-specific scripts (`.sh`, `.bat`, etc.) in each bot directory. This uses metadata from the bot's JSON configuration to select a template and inject necessary parameters.

## Template Selection Logic
The `BotBooter` will use `platform` and `programmingLang` from the `BootEntry` (bot configuration) to select an appropriate template.

### Proposed Templates Location
Templates will be stored in `booter/src/main/resources/templates/`.

### Mapping (Examples)
| Platform | ProgrammingLang | Template | Default Command Pattern |
|----------|-----------------|----------|-------------------------|
| `JVM`    | `Java *`        | `jvm.boot` | `java -cp "../lib/*" {main}.java` |
| `.NET *` | `C# *`          | `dotnet.boot` | `dotnet run -c Release --no-build` |
| `Python` | `Python *`      | `python.boot` | `python {main}.py` |

## Template Format
Templates will be simple property files or JSON containing the command line pattern.

### Example: `jvm.boot`
```properties
command=java -cp "${classPath}" ${mainClass}
```

### Example: `python.boot`
```properties
command=python ${mainFile}
```

## Injection Parameters
- `${base}`: Replaces with the `base` property from the bot's JSON.
- `${botName}`: Replaces with the `name` property from the bot's JSON.
- `${botDir}`: The directory of the bot.

## Core Components
1. **TemplateManager**: Handles loading and parsing templates from resources.
2. **TemplateBooter**: Uses `TemplateManager` to construct a `ProcessBuilder` based on the selected template and bot configuration.
3. **BotBooter**: Updated to try `TemplateBooter` if no script is found by `ScriptFinder`.

## Fallback Strategy
1. Look for OS-specific script (current behavior).
2. If no script is found, try template-based booting.
3. If template-based booting fails (no matching template or missing `base` field), log error.
