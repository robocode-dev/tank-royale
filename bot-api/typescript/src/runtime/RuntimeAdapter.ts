import { WebSocketLike } from "./WebSocketLike.js";

/**
 * Abstracts runtime-specific capabilities so the core bot API can run in both
 * Node.js and browser environments.
 */
export interface RuntimeAdapter {
  /** Create a WebSocket connection to the given URL. */
  createWebSocket(url: string): WebSocketLike;
  /** Read an environment variable by name. Returns undefined if not available. */
  getEnvVar(name: string): string | undefined;
  /** Terminate the process / runtime with the given exit code. */
  exit(code: number): void;
}
