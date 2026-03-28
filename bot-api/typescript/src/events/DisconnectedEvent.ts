import { ConnectionEvent } from "./ConnectionEvent.js";

/** Event occurring when disconnected from the server. */
export class DisconnectedEvent extends ConnectionEvent {
  readonly remote: boolean;
  readonly statusCode?: number;
  readonly reason?: string;

  constructor(serverUri: string, remote: boolean, statusCode?: number, reason?: string) {
    super(serverUri);
    this.remote = remote;
    if (statusCode !== undefined) this.statusCode = statusCode;
    if (reason !== undefined) this.reason = reason;
  }
}
