import { ConnectionEvent } from "./ConnectionEvent.js";

/** Event occurring when connected to the server. */
export class ConnectedEvent extends ConnectionEvent {
  constructor(serverUri: string) {
    super(serverUri);
  }
}
