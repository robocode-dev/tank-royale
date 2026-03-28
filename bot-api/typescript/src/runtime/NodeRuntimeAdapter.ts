import { RuntimeAdapter } from "./RuntimeAdapter.js";
import { WebSocketLike } from "./WebSocketLike.js";
import type WsWebSocket from "ws";

/**
 * RuntimeAdapter implementation for Node.js.
 * Uses the `ws` library for WebSocket, `process.env` for environment variables,
 * and `process.exit()` for termination.
 */
export class NodeRuntimeAdapter implements RuntimeAdapter {
  createWebSocket(url: string): WebSocketLike {
    // Dynamically require `ws` so the package stays an optional peer dependency.
    // eslint-disable-next-line @typescript-eslint/no-require-imports
    const WS = require("ws") as new (url: string) => WsWebSocket;
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
      wrapper.onmessage?.({ data: data.toString() });
    });
    return wrapper;
  }

  getEnvVar(name: string): string | undefined {
    return process.env[name];
  }

  exit(code: number): void {
    process.exit(code);
  }
}
