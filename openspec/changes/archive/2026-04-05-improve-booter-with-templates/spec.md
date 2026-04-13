# Specification: Template-based Booting

## Changes to Data Models

### `BootEntry.kt`
Update the `BootEntry` class to include a `base` property. This property will hold the main class (JVM), main file (Python), or main DLL (.NET).

```kotlin
@Serializable
data class BootEntry(
    // ... existing fields ...
    // New property
    override val base: String? = null,
    // ...
)
```

## Template Definitions

### Template Selection logic
The `TemplateManager` will use the following case-insensitive rules for mapping:
- `platform == "JVM"` -> `jvm.boot`
- `platform.startsWith(".NET")` -> `dotnet.boot`
- `platform == "Python"` -> `python.boot`

### `jvm.boot`
```properties
command.windows=javaw -cp "${classPath}" ${base}.java
command.unix=java -cp "${classPath}" ${base}.java
```

### `dotnet.boot`
```properties
command.windows=dotnet run -c Release --no-build
command.unix=dotnet run -c Release --no-build
```

### `python.boot`
```properties
command.windows=python ${base}.py
command.unix=python3 ${base}.py
```

## BotBooter logic Update
The `BotBooter`'s `bootBot` method should be modified to try template-based booting as a second option:

```kotlin
private fun bootBot(botDir: Path, team: Team?, getBootEntry: (Path) -> BootEntry?): Process? {
    return try {
        val botEntry = getBootEntry(botDir) ?: return null
        val scriptPath = findBootScriptOrNull(botDir)
        if (scriptPath != null) {
            return createAndStartProcess(scriptPath, botDir, botEntry, team)
        }
        // New: Try template-based booting
        return templateBooter.boot(botDir, botEntry, team)
    } catch (ex: Exception) {
        Log.error(ex, botDir)
        null
    }
}
```

## Sample Bot Build Changes

### `sample-bots/java/build.gradle.kts`
Modify the `prepareBotFiles` task:
- Before creating a script file (`.cmd`, `.sh`), check if the bot has a `.json` configuration file.
- Check if the `.json` contains a `base` property.
- If it does, and it's a standard bot (not a team), skip generating the script files.
- The `base` property must be set programmatically or exist in the source JSON. Since we are already adding `base` to `Corners.json` and other sample bots, we can rely on its presence.

### `sample-bots/csharp/build.gradle.kts`
Modify the `prepareBotFiles` task:
- Similar to the Java build, skip script generation if `base` property is present in the bot configuration.

### `sample-bots/python/build.gradle.kts`
Modify the `processIndividualBot` task:
- Skip script generation if `base` property is present in the bot configuration.

## Template Replacement Logic
Replace `${base}` with the value of the `base` property from the bot's configuration.
For JVM: `${base}` = value of `base` (the .java file name), `${classPath}` = `../lib/*`.
For .NET: No parameters used (currently assumes standard `dotnet run`).
For Python: `${base}` = value of `base`.
`${botDir}` = Absolute path to the bot's directory.
`${botName}` = Value of `name` from bot configuration.
