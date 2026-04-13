# ADR-0027: TypeScript Bot API for Web Platform Support

**Status:** Accepted
**Date:** 2026-03-24

---

## Context

Tank Royale provides Bot APIs in Java, C#, and Python. All three target desktop/server environments. There is no way to write bots that run in a browser, which limits access for web developers and prevents browser-based competitive environments.

**Problem:** How should Tank Royale support JavaScript/TypeScript bot development and browser-based competitive environments, while maintaining 1:1 semantic equivalence with the Java reference implementation (ADR-0004)?

**Constraints:**

- Must maintain 1:1 semantic equivalence with the Java reference implementation (ADR-0003, ADR-0004)
- Must support JavaScript/TypeScript as the developer-facing language
- Must enable browser-based bot development (ADR-0023)
- Should work in Node.js for standalone bot processes (booter integration)

---

## Decision

Implement the Bot API as a **pure TypeScript library**, distributed via npm, targeting both Node.js and browsers.

```
[Bot code — TypeScript/JavaScript]    ← Bot developers write here
          │
[TypeScript Bot API library]          ← npm package, 1:1 Java semantics
          │
[WebSocket to Tank Royale server]     ← Native WebSocket (browser) or ws (Node.js)
```

**Architecture:**

- Single TypeScript codebase implementing the full Bot API
- Same class hierarchy as Java: `IBaseBot` → `BaseBot`, `IBot` → `Bot`
- Same event model, lifecycle, and dispatch semantics
- async/await for blocking methods (see ADR-0028)
- Runtime abstraction for Node.js vs browser differences (see ADR-0029)
- npm distribution: `@robocode.dev/tank-royale-bot-api`

**Build integration:**

- Gradle orchestrates the TypeScript build via the Node.js Gradle plugin (consistent with monorepo strategy, ADR-0001)
- Node.js is downloaded automatically by the Gradle plugin
- `./gradlew :bot-api:typescript:build` and `./gradlew :bot-api:typescript:test`

**Directory structure:**

```
bot-api/typescript/
├── src/                          ← TypeScript source
│   ├── bot/                      ← BaseBot, Bot, IBaseBot, IBot
│   ├── events/                   ← All event classes
│   ├── internal/                 ← BaseBotInternals, BotInternals, EventQueue, WebSocketHandler
│   ├── graphics/                 ← IGraphics, SvgGraphics, Color, Point
│   ├── mapper/                   ← EventMapper, GameSetupMapper, etc.
│   └── index.ts                  ← Public API exports
├── test/                         ← Tests
├── build.gradle.kts              ← Gradle build (Node.js plugin, npm tasks)
├── package.json                  ← npm configuration
├── tsconfig.json                 ← TypeScript configuration
└── README.md
```

**Naming conventions:** TypeScript uses `camelCase` for methods and `PascalCase` for classes — identical to Java, making the semantic mapping nearly 1:1.

**Multi-language strategy:** Future Bot APIs for other languages (Rust, Go, etc.) should be standalone native implementations — each written idiomatically in the target language, talking the same WebSocket protocol. This follows the established pattern of Java, C#, and Python APIs.

---

## Rationale

**Why TypeScript:**

- ✅ **Native web platform support**: Runs in browsers and Node.js without any compilation step beyond transpilation
- ✅ **Idiomatic for the target audience**: JS/TS developers get a library that works like every other npm package
- ✅ **Best debugging experience**: Source maps, async stack traces, full IDE support
- ✅ **Consistent with the cross-platform pattern**: Java, C#, and Python Bot APIs are each implemented natively in their target language. TypeScript follows the same pattern
- ✅ **The bot API is a thin layer**: JSON parsing, event sorting, and state tracking at 30 TPS — TypeScript handles this trivially
- ✅ **Gradle-managed build**: Node.js Gradle plugin keeps the TypeScript module consistent with the monorepo build strategy

---

## Alternatives Considered

### WebAssembly (WASM)

WASM was considered as the implementation technology (Kotlin/Wasm and Rust-to-WASM were both evaluated).

**Rejected because:**

- The bot API is a thin layer (JSON parsing, event sorting, state tracking at 30 TPS) — WASM provides no measurable performance benefit for this workload
- WASM cannot access WebSocket or environment variables directly — a JavaScript layer is always needed regardless
- A native TypeScript implementation gives better debugging, smaller bundles, and a more idiomatic developer experience

---

## Consequences

### Positive

- ✅ Bot developers write idiomatic TypeScript with full IDE support and type checking
- ✅ Works in both Node.js and browsers natively
- ✅ Gradle-managed build (Node.js downloaded via Gradle plugin), consistent with monorepo strategy
- ✅ npm distribution for easy adoption
- ✅ Best debugging experience (source maps, async stack traces)
- ✅ Follows the established cross-platform pattern (native implementation per language)
- ✅ Small bundle size

### Negative / Challenges

- ⚠️ Adds a 4th Bot API codebase to maintain (Java, C#, Python, TypeScript)

### Impact on ADR-0003

ADR-0003 should be amended to list TypeScript as a 4th supported language alongside Java, C#, and Python.

---

## References

- [ADR-0003: Cross-Platform Bot API Strategy](./0003-cross-platform-bot-api-strategy.md)
- [ADR-0004: Java as Authoritative Reference Implementation](./0004-java-reference-implementation.md)
- [ADR-0009: WebSocket Communication Protocol](./0009-websocket-communication-protocol.md)
- [ADR-0023: Platform Scope and Boundaries](./0023-robocode-tank-royale-platform-scope.md)
- [ADR-0028: TypeScript Bot API Threading Model](./0028-typescript-bot-api-threading-model.md)
- [ADR-0029: TypeScript Bot API Runtime Targets](./0029-typescript-bot-api-runtime-targets.md)
