# Cross-Platform Bot API Guidelines

<!-- KEYWORDS: Bot API, Java, Python, .NET, C#, TypeScript, cross-platform, port, reference implementation, semantic equivalence -->

## 1:1 Semantic Equivalence (Hard Requirement)

All official Bot APIs MUST be 1:1 semantically equivalent to the Java Bot API. See CONTRIBUTING.md for the full policy.

**Acceptable differences** (language idioms only):
- `snake_case` (Python) vs `camelCase` (Java/TypeScript)
- Properties (C#/Python) vs getters/setters (Java)
- `@dataclass` (Python) vs immutable classes (Java)
- `async/await` (TypeScript) for blocking methods like `forward()` — see ADR-0028

**Unacceptable differences:**
- Different default values, event order, null/None handling, or validation rules
- Different blocking behavior (`forward()` must block on ALL platforms)
- Extra or missing public methods
- Different JSON/wire protocol behavior

## Source of Truth

Java Bot API is the reference implementation. When in doubt, Java is authoritative.

## Change Workflow

When modifying any Bot API:

1. Implement/verify in Java first
2. Port to Python — match names, defaults, behavior exactly
3. Port to .NET — match names, defaults, behavior exactly
4. Port to TypeScript — match names, defaults, behavior exactly (`camelCase`, `async/await`)
5. Update sample bots if change is user-visible
6. Build and test each module independently

**Checklist:**
- [ ] Java complete and tested
- [ ] Python matches Java semantics
- [ ] .NET matches Java semantics
- [ ] TypeScript matches Java semantics
- [ ] JSON serialization identical across platforms
- [ ] Event order and timing consistent
- [ ] Error messages aligned
- [ ] `./gradlew clean build` passes

## API Stability

- No breaking changes to public API without deprecation across all platforms
- Protocol (JSON/WebSocket): additive changes only (new optional fields)
- Update `/schema` and docs for any protocol change; verify backward compatibility
