# ADR-0029: TypeScript Bot API Runtime Targets

**Status:** Accepted
**Date:** 2026-03-24

---

## Context

The TypeScript Bot API (ADR-0027) is a pure TypeScript library targeting the web platform. It must interact with runtime-specific APIs for:

- **WebSocket connections** — `ws` library (Node.js) vs native `WebSocket` (browser)
- **Environment variables** — `process.env` (Node.js) vs not available (browser)
- **Process lifecycle** — `process.exit()` (Node.js) vs page lifecycle (browser)

ADR-0023 envisions both server-side bot execution and "browser-based development and competitive environments." Both are first-class goals.

**Problem:** How should the TypeScript Bot API support both Node.js and browser runtimes?

---

## Decision

**Support both Node.js and browsers** via a runtime abstraction layer. Node.js is the initial implementation target (simpler to develop and test), with browser support designed in from the start and delivered as part of the same API.

**Runtime abstraction layer:**

```typescript
interface RuntimeAdapter {
  createWebSocket(url: string): WebSocketLike;
  getEnvVar(name: string): string | undefined;
  exit(code: number): void;
}

class NodeRuntimeAdapter implements RuntimeAdapter {
  createWebSocket(url: string) { return new (require('ws'))(url); }
  getEnvVar(name: string) { return process.env[name]; }
  exit(code: number) { process.exit(code); }
}

class BrowserRuntimeAdapter implements RuntimeAdapter {
  createWebSocket(url: string) { return new WebSocket(url); }
  getEnvVar(name: string) { return undefined; }  // config via constructor
  exit(code: number) { /* no-op */ }
}
```

**Auto-detection:** The library detects the runtime environment automatically (`typeof process !== 'undefined'` → Node.js, otherwise → browser). Developers can also explicitly provide a runtime adapter via the `BaseBot` constructor.

**Node.js specifics:**

- WebSocket via `ws` npm package (API-compatible with browser `WebSocket`)
- Configuration via `process.env` (matching Java's env var names per ADR-0013)
- Bot process lifecycle via `process.exit()`
- Booter can launch Node.js bot processes identically to Java/Python processes
- Test runner: Vitest or Jest

**Browser specifics:**

- Native `WebSocket` API (no library needed)
- Configuration via constructor parameters or a config object (no env vars)
- No process lifecycle management — bots run within the page
- Enables online bot editors, web-based tournaments, educational platforms
- CORS considerations: WebSocket connections must target the correct server origin

**npm distribution:**

- Package: `@robocode.dev/tank-royale-bot-api`
- Dual exports: ESM (browser + modern Node.js) and CJS (legacy Node.js)
- TypeScript declarations included (`.d.ts`)
- No native dependencies — `ws` is an optional peer dependency for Node.js

---

## Rationale

**Why both runtimes from the start:**

- ✅ **Browser support is a primary goal** (ADR-0023) — deferring it risks an architecture that's hard to adapt later
- ✅ **The abstraction is small** — only 3 methods differ between Node.js and browser (WebSocket, env vars, exit)
- ✅ **TypeScript natively supports both** — unlike WASM-based approaches that need loading/instantiation differences per runtime
- ✅ **Dual-export npm packages are standard practice** — well-supported by bundlers (webpack, vite, esbuild)

**Why auto-detection with manual override:**

- ✅ Zero configuration for the common case (bot just works in both environments)
- ✅ Manual override for testing or unusual environments (SSR, Workers, Deno)

---

## Alternatives Considered

### Alternative 1: Node.js Only (Browser Deferred)

Ship Node.js support first, add browser support later as a separate proposal.

**Rejected because:**

- Browser support is a primary motivation for the TypeScript Bot API (ADR-0023)
- The runtime abstraction is small enough to include from the start
- Deferring browser support risks assumptions that are hard to undo (e.g., `process.env` usage without abstraction)

### Alternative 2: Browser Only

Target browsers exclusively, requiring bot developers to use a browser environment.

**Rejected because:**

- The booter expects bots to be standalone processes — Node.js enables this
- No `process.env` configuration breaks compatibility with existing bot launching (ADR-0013)
- Development/testing workflows are easier with Node.js (CLI, CI)

### Alternative 3: Separate Packages Per Runtime

Publish `@robocode.dev/bot-api-node` and `@robocode.dev/bot-api-browser` as distinct packages.

**Rejected because:**

- Fragments the ecosystem — bot code should be portable between runtimes
- Increases maintenance burden (two packages, two release cycles)
- Modern bundlers handle dual-export packages well

---

## Consequences

### Positive

- ✅ Bots written once run in both Node.js and browsers
- ✅ Browser support enables online editors, web tournaments, educational tools
- ✅ Node.js support enables booter integration and standalone bot processes
- ✅ npm distribution with dual exports is standard and well-supported
- ✅ No native dependencies required (browser) or minimal (Node.js: optional `ws`)

### Negative / Challenges

- ⚠️ Must test across both Node.js and browser environments (slightly larger test matrix)
- ⚠️ Browser bots cannot use env var configuration — must use constructor parameters
- ⚠️ CORS restrictions may complicate browser WebSocket connections to local servers
- ⚠️ `ws` is a peer dependency for Node.js — must be documented

---

## References

- [ADR-0013: Bot Configuration via Environment Variables](./0013-bot-configuration-env-vars.md)
- [ADR-0023: Platform Scope and Boundaries](./0023-robocode-tank-royale-platform-scope.md)
- [ADR-0027: TypeScript Bot API for Web Platform Support](./0027-typescript-bot-api-architecture.md)
- [ADR-0028: TypeScript Bot API Threading Model](./0028-typescript-bot-api-threading-model.md)
