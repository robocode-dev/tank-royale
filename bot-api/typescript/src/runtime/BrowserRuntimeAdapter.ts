import { RuntimeAdapter } from "./RuntimeAdapter.js";
import { WebSocketLike } from "./WebSocketLike.js";

/**
 * RuntimeAdapter implementation for browser environments.
 * Uses the native browser WebSocket, returns undefined for all env vars
 * (not available in browsers), and is a no-op for exit().
 */
export class BrowserRuntimeAdapter implements RuntimeAdapter {
  createWebSocket(url: string): WebSocketLike {
    const ws = new WebSocket(url);
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
    ws.addEventListener("open", (event) => wrapper.onopen?.(event));
    ws.addEventListener("close", (event) => wrapper.onclose?.(event));
    ws.addEventListener("error", (event) => wrapper.onerror?.(event));
    ws.addEventListener("message", (event: MessageEvent<string>) => {
      wrapper.onmessage?.({ data: event.data });
    });
    return wrapper;
  }

  getEnvVar(_name: string): string | undefined {
    return undefined;
  }

  exit(_code: number): void {
    // No-op in browser environments.
  }
}
