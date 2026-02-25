# Testing and Build

<!-- KEYWORDS: test, build, Gradle, gradlew, compile, validation, sample bot, backward compatibility -->

## Build Requirements

Run `./gradlew clean build` after any code, config, or build system change.

**Skip for:** pure markdown, README, or comment-only changes.

**Module builds (faster iteration):**

```bash
./gradlew :bot-api:java:build
./gradlew :server:build
./gradlew :gui:build
```

## Testing Standards

- Add regression tests for bug fixes; add tests for new behavior
- Run tests on ALL platforms (Java, Python, .NET) for Bot API changes
- Validate JSON schema compliance for protocol changes
- Fix test failures before completing a task (or explain if expected)

## Protocol Change Sequence

1. Update JSON schema in `/schema`
2. Update documentation
3. Verify backward compatibility
4. Add JSON examples
5. Test with existing bots

## Sample Bot Validation

Test with sample bots when making timing, state, user-visible API, or event handling changes.
Run at least one bot per language; verify expected behavior and check console output for errors.
