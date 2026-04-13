import { RuntimeAdapter } from "./RuntimeAdapter.js";
import { WebSocketLike } from "./WebSocketLike.js";
import type WsWebSocket from "ws";

// Cached require function that works in both CJS and ESM contexts.
let _nodeRequire: ((id: string) => any) | null = null;

/**
 * Initialize the Node.js require function for ESM compatibility.
 * In CJS, `require` is already available. In ESM, `createRequire` from
 * `node:module` is used. Must be awaited before `createWebSocket()`.
 */
export async function initNodeRuntime(): Promise<void> {
  if (_nodeRequire) return;
  if (typeof require === "function") {
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    _nodeRequire = require;
    return;
  }
  if (typeof process === "undefined" || process.env === undefined) return;
  try {
    const mod = await import("node:module");
    // @ts-ignore TS1343 – import.meta.url is ESM-only; in CJS builds, the require branch above is taken
    _nodeRequire = mod.createRequire(import.meta.url);
  } catch {
    // Not in a Node.js environment
  }
}

/**
 * RuntimeAdapter implementation for Node.js.
 * Uses the `ws` library for WebSocket, `process.env` for environment variables,
 * and `process.exit()` for termination.
 */
export class NodeRuntimeAdapter implements RuntimeAdapter {
  createWebSocket(url: string): WebSocketLike {
    if (!_nodeRequire) {
      throw new Error("[BOT-API] Node runtime not initialized. Ensure start() was called.");
    }
    const WS = _nodeRequire("ws") as new (url: string) => WsWebSocket;
    const ws = new WS(url);
    // The `ws` library emits events via EventEmitter; wrap it to match WebSocketLike.
    const wrapper: WebSocketLike = {
      onopen: null,
      onclose: null,
      onerror: null,
      onmessage: null,
      get readyState() {
        return ws.readyState;
      },
      send(data: string) {
        ws.send(data);
      },
      close() {
        ws.close();
      },
    };
    ws.on("open", (event: unknown) => wrapper.onopen?.(event));
    ws.on("close", (event: unknown) => wrapper.onclose?.(event));
    ws.on("error", (event: unknown) => wrapper.onerror?.(event));
    ws.on("message", (data: Buffer | string) => {
      try {
        wrapper.onmessage?.({ data: data.toString() });
      } catch (err) {
        console.error("[BOT-API] Error handling WebSocket message:", err);
      }
    });
    return wrapper;
  }

  getEnvVar(name: string): string | undefined {
    return process.env[name];
  }

  exit(code: number): void {
    process.exit(code);
  }

  readFile(path: string): string | undefined {
    try {
      const req = _nodeRequire ?? (typeof require === "function" ? require : null);
      if (!req) return undefined;
      return req("fs").readFileSync(path, "utf8") as string;
    } catch {
      return undefined;
    }
  }
}
