# Cross-Platform Bot API Guidelines

<!-- METADATA: ~60 lines, ~650 tokens -->
<!-- KEYWORDS: Bot API, Java, Python, .NET, C#, cross-platform, port, reference implementation, semantic equivalence -->

## Source of Truth

Remember Java Bot API is the reference implementation.

- All Bot API platforms must align with Java's behavior
- API names, defaults, and event semantics must match
- When in doubt, Java implementation is authoritative

## Bot API Change Requirements

Always update ALL Bot APIs (Java, Python, .NET) when any single one is modified.

Always maintain 1:1 semantic equivalence across platforms.

Always keep JSON/wire protocol behavior consistent.

Always update sample bots if the change is user-visible.

**Platform-specific idioms are acceptable** if semantics are identical:

- Python uses `snake_case` while Java uses `camelCase` (naming convention)
- C# and Python use properties while Java uses getters/setters (language idiom)
- Python uses `@dataclass` while Java uses immutable classes (implementation)

**UNACCEPTABLE differences:**

- Different default values across platforms
- Events firing in different order
- Different null/None handling behavior
- Inconsistent validation rules

## Cross-Language Workflow

Always follow this sequence for Bot API changes:

1. Implement/verify in Java Bot API first (reference implementation)
2. Port to Python Bot API - match names, defaults, behavior exactly
3. Port to .NET Bot API - match names, defaults, behavior exactly
4. Verify 1:1 semantic equivalence across all three platforms
5. Update sample bots if change affects user-visible API
6. Build and test each module independently

**Verification checklist:**

- [ ] Java implementation complete and tested
- [ ] Python port matches Java semantics
- [ ] .NET port matches Java semantics
- [ ] JSON serialization identical across platforms
- [ ] Event order and timing consistent
- [ ] Error messages aligned
- [ ] Sample bots updated (if applicable)
- [ ] `./gradlew clean build` passes successfully

## API Stability Rules

**Public API:**

Always keep public API stable and avoid breaking changes.

Always coordinate across ALL ports if a breaking change is unavoidable.

Always deprecate gracefully (warnings, migration guides).

**Protocol (JSON/WebSocket):**

Remember NO breaking changes are allowed for the protocol.

Always use additive changes only (new optional fields).

Always update `/schema` and docs for any protocol change.

Always verify backward compatibility.

## Testing Requirements

Always add tests for new behavior when making Bot API changes.

Always ensure tests pass on ALL platforms (Java, Python, .NET).

Always validate protocol changes with JSON schema validation.

Always validate timing/state changes with sample bots.
