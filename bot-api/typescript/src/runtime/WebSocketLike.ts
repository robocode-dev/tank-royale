/**
 * A minimal WebSocket-like interface that abstracts over native browser WebSocket
 * and the `ws` library used in Node.js.
 */
export interface WebSocketLike {
  /** Fired when the connection is opened. */
  onopen: ((event: unknown) => void) | null;
  /** Fired when the connection is closed. */
  onclose: ((event: unknown) => void) | null;
  /** Fired when an error occurs. */
  onerror: ((event: unknown) => void) | null;
  /** Fired when a message is received. */
  onmessage: ((event: { data: string }) => void) | null;
  /** Send a message to the server. */
  send(data: string): void;
  /** Close the connection. */
  close(): void;
  /** Current ready state (0=CONNECTING, 1=OPEN, 2=CLOSING, 3=CLOSED). */
  readonly readyState: number;
}
