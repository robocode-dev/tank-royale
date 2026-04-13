import { ConnectionEvent } from "./ConnectionEvent.js";

/** Event occurring when a connection error has occurred. */
export class ConnectionErrorEvent extends ConnectionEvent {
  readonly error: Error;

  constructor(serverUri: string, error: Error) {
    super(serverUri);
    this.error = error;
  }
}
