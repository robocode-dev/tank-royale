import { IEvent } from "./IEvent.js";
import { InitialPosition } from "../InitialPosition.js";
import { GameSetup } from "../GameSetup.js";

/** Event occurring when a game has started. */
export class GameStartedEvent implements IEvent {
  readonly myId: number;
  readonly initialPosition: InitialPosition;
  readonly gameSetup: GameSetup;

  constructor(myId: number, initialPosition: InitialPosition, gameSetup: GameSetup) {
    this.myId = myId;
    this.initialPosition = initialPosition;
    this.gameSetup = gameSetup;
  }
}
