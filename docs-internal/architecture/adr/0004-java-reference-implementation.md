# ADR-0004: Java as Authoritative Reference Implementation

**Status:** Accepted  
**Date:** 2026-02-14 (Documenting historical decision)

---

## Context

Tank Royale supports Bot APIs in Java, .NET/C#, and Python, with WebAssembly (WASM) planned. When implementations
diverge or ambiguity arises, a single authoritative source is needed.

**Problem:** Which platform's behavior is canonical when Bot APIs disagree?

---

## Decision

The **Java Bot API is the authoritative reference implementation**. All other Bot APIs (Python, .NET, Wasm) must be
semantically identical to Java.

**Rules:**

- 1:1 semantic equivalence with Java (methods, defaults, validation, event order)
- Language conventions allowed (Python snake_case, C# PascalCase, properties instead of getters/setters)
- No platform-specific additions to the public API
- When in doubt, Java's behavior is authoritative

---

## Rationale

- ✅ Classic Robocode was Java — Tank Royale preserves this heritage
- ✅ Java Bot API is the most battle-tested implementation
- ✅ Single source of truth simplifies maintenance, testing, and documentation
- ✅ Users can learn from any language's tutorial and apply it to another
- ❌ Not fully idiomatic per language (structure over convention)
- ❌ Maintenance burden (changes replicated across 3+ codebases)

---

## References

- [ADR-0003: Cross-Platform Bot API Strategy](./0003-cross-platform-bot-api-strategy.md)
- [Bot API Java](/bot-api/java/), [.NET](/bot-api/dotnet/), [Python](/bot-api/python/)
