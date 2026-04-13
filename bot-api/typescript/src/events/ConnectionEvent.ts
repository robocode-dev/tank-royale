import { IEvent } from "./IEvent.js";

/** Base class for all connection events. */
export abstract class ConnectionEvent implements IEvent {
  readonly serverUri: string;

  protected constructor(serverUri: string) {
    this.serverUri = serverUri;
  }
}
