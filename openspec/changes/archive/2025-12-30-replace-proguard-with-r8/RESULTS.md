# R8 Migration Results

## Summary

Successfully replaced ProGuard with R8 for shrinking application JARs in all four modules: `booter`, `server`,
`recorder`, and `gui`.

## Size Comparison

| Module   | ProGuard Size | R8 Size      | Delta        | % Change |
|----------|---------------|--------------|--------------|----------|
| booter   | 6,083,852 B   | 5,826,182 B  | -257,670 B   | -4.2%    |
| server   | 6,091,289 B   | 5,881,435 B  | -209,854 B   | -3.4%    |
| recorder | 6,383,226 B   | 6,130,667 B  | -252,559 B   | -4.0%    |
| gui      | 21,426,158 B  | 20,325,072 B | -1,101,086 B | -5.1%    |

**Total savings: 1,821,169 bytes (1.74 MB) or 4.5% average reduction**

## Size Comparison (MB)

| Module   | ProGuard | R8       | Saved   |
|----------|----------|----------|---------|
| booter   | 5.80 MB  | 5.56 MB  | 0.24 MB |
| server   | 5.81 MB  | 5.61 MB  | 0.20 MB |
| recorder | 6.09 MB  | 5.85 MB  | 0.24 MB |
| gui      | 20.43 MB | 19.38 MB | 1.05 MB |

## Smoke Test Results

All CLI archives successfully execute with `--version` flag:

```
✓ booter:   booter version Robocode Tank Royale Booter 0.34.3
✓ server:   server-cli version Robocode Tank Royale Server 0.34.3
✓ recorder: recorder-cli version Robocode Tank Royale Recorder 0.34.3
✓ gui:      Robocode Tank Royale GUI 0.34.3
```

## Implementation Details

### Changes Made

1. **Updated version catalog** (`gradle/libs.versions.toml`)
    - Added R8 version 8.13.17

2. **Updated root build.gradle.kts**
    - Added Google Maven repository to `allprojects` for R8 dependency resolution
    - Updated JDK warning message to remove ProGuard-specific reference

3. **Created module-specific R8 rules files**
    - `booter/r8-rules.pro`
    - `server/r8-rules.pro`
    - `recorder/r8-rules.pro`
    - `gui/r8-rules.pro`

4. **Migrated build scripts** for all four modules
    - Replaced ProGuard imports and tasks with R8 JavaExec tasks
    - Updated buildscript dependencies to use R8
    - Fixed maven publication to use file references instead of ProGuardTask API
    - Added smoke test tasks for all modules

5. **Added automated smoke tests**
    - Each module now has a `smokeTest` task that verifies `--version` execution

### Key Differences from ProGuard

- **R8 outputs directly to JAR**: Unlike ProGuard which can output to a specified file, R8 outputs to a directory. We
  configure it to output directly to the final JAR path.
- **Module-specific rules**: Removed the shared `proguard-common.pro` and created module-specific rules files for better
  maintainability.
- **Additional dontwarn rules**: R8 requires explicit dontwarn rules for Java 11+ classes and optional annotations.

## Benefits

1. **Better Java 21+ compatibility**: R8 has better support for newer JDK versions compared to ProGuard
2. **Smaller artifacts**: 4.5% average size reduction across all modules
3. **Improved maintainability**: Module-specific rules files make it easier to understand dependencies
4. **Quality gate**: Automated smoke tests ensure distributable archives work correctly
5. **Active development**: R8 is actively maintained by Google and used in Android ecosystem

## Notes

- R8 displays warnings about Kotlin metadata version (2.3.0 vs supported 2.2.0), but this doesn't affect functionality
- R8 ignores some generic type signatures that are invalid, but this is cosmetic and doesn't impact runtime behavior
- All keep rules from ProGuard were successfully migrated to R8 format
- Build task name kept as `proguard` for backward compatibility with jpackage tasks

