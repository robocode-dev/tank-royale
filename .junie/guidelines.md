# AI Contributor Guidelines for Tank Royale

## Core principles
- Use clean code principles: readability over cleverness, meaningful names, small focused functions, clear responsibilities, and minimal coupling.
- Minimal-change principle: implement only what the issue requires. Avoid drive-by refactors unless they directly reduce risk or are necessary for the fix.
- Prefer small, atomic commits with clear messages that explain the why, not just the what.

## Source of truth and parity
- The Bot API and sample bots for Java serve as the reference implementation for all other platforms/languages.
- When changing or fixing a non-Java port, first check the Java reference. Align behavior, API names, defaults, and event semantics with Java.
- Keep JSON/wire behavior consistent across languages. Platform-specific idioms are fine as long as semantics remain identical.

## Public API stability
- Treat the public Bot API as stable. Avoid breaking changes. If a breaking change is unavoidable, coordinate updates for all ports, update docs, and bump versions accordingly.

## Protocol/schema compatibility
- Do not break the server↔bot wire protocol. Prefer additive, backward-compatible changes. Never change the meaning of existing fields.
- If the protocol changes, update `/schema` and relevant documentation, and ensure both server and clients remain compatible.

## Coding conventions
- Java
  - Follow the existing style in this repository.
  - Prefer immutability; use `final` where possible; avoid exposing mutable internal state.
  - Use clear, intention-revealing names; eliminate magic numbers with named constants.
  - Be explicit about nullability and avoid NPEs; favor defensive programming where appropriate.
- Python
  - Follow PEP 8 and use PEP 484 type hints. Keep `py.typed` valid and run `mypy` for changes.
  - Avoid dynamic attribute creation; prefer dataclasses or simple classes with properties.
  - Keep public state objects (e.g., BotState) read-only from the consumer perspective when applicable.
- General
  - Apply SRP, DRY, YAGNI; prefer composition over inheritance.
  - Favor pure functions where practical; use early returns to reduce nesting.
  - Provide precise error messages and avoid swallowing exceptions.

## Workflow for cross-language changes
1. Implement/verify the change in the Java Bot API first.
2. Port the change to other languages, matching names, defaults, and behavior (allow idiomatic differences without semantic changes).
3. Update sample bots in each language if behavior changes are user-visible.
4. Build and run relevant tests/checks for each affected module.

## Testing and validation
- Build the affected module(s) and run existing tests. Add tests when fixing bugs or adding behavior.
- For protocol-affecting changes, update JSON examples, schema, and docs. Verify backward compatibility.
- For timing/tick/state changes, validate behavior with a sample bot to ensure no regressions in semantics.

## Documentation
- Update README/docs and VERSIONS.MD when user-visible behavior or API surface changes.
- Keep Javadoc and docstrings accurate and aligned across language ports.

## Review checklist
- Does the change match the Java reference implementation?
- Is backward compatibility preserved (API and wire protocol)?
- Is the diff minimal and focused (no unrelated refactors)?
- Are tests and documentation updated as needed?
- Are naming and types consistent across language ports?

## Project/tooling notes for this environment
- Use the provided search and file tools; avoid interactive terminal programs.
- Do not create files outside the repository.

## When in doubt
- Ask for clarification in the issue before making broad or potentially breaking changes.