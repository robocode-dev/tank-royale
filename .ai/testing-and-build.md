# Testing and Build Procedures

<!-- METADATA: ~45 lines, ~500 tokens -->
<!-- KEYWORDS: test, build, Gradle, gradlew, compile, validation, sample bot, backward compatibility -->

## Project Context

**Solo Developer, Open Source Project**

- Maintained by Flemming N. Larsen (single developer, not a team)
- Released via **GitHub as artifacts** (also Maven Central, NuGet, PyPI)
- Manual GUI smoke testing is acceptable and expected
- No performance benchmarking overhead — refactorings shouldn't degrade performance, but formal measurement is optional
- Users report bugs via GitHub issues

## Build Requirements

Always run `./gradlew clean build` after ANY of these changes:

- Code changes (Java, Kotlin, Python, C#)
- Configuration changes (build files, Gradle scripts)
- Build system changes (dependencies, plugins)

**Exception - skip build for:**

- Pure text/markdown documentation
- README updates (unless they affect build)
- Comment-only changes

**Build command:**

```bash
./gradlew clean build
```

## Testing Standards

Always add regression tests for bug fixes.

Always add tests covering new behavior for new features.

Always run tests on ALL platforms (Java, Python, .NET) for Bot API changes.

Always validate JSON schema compliance for protocol changes.

**Test execution:**

Always ensure all tests pass before completing task.

Remember the build system runs tests automatically.

If tests fail, fix or explain why failure is expected.

## Protocol Change Validation

Always follow this sequence when modifying JSON/WebSocket protocol:

1. Update JSON schema in `/schema`
2. Update documentation
3. Verify backward compatibility
4. Add JSON examples demonstrating new behavior
5. Test with existing bots to ensure no breakage

## Sample Bot Validation

Always test with sample bots when making:

- Timing changes (turn order, event sequencing)
- State management changes
- User-visible Bot API changes
- Event handling modifications

**How to validate:**

Always run at least one sample bot from each language.

Always verify expected behavior matches.

Always check console output for errors.

Always ensure cross-platform consistency.

## Module-Specific Builds

**Building individual modules:**

```bash
# Build specific module
./gradlew :bot-api:java:build
./gradlew :server:build
./gradlew :gui:build
```

**When to use:**

- Faster iteration during focused work
- Isolating build issues
- Testing single-platform changes
