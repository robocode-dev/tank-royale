import GameSetup from "./GameSetup";

export enum MessageType {
  ServerHandshake = "serverHandshake",
  BotListUpdate = "botListUpdate",
}

export class Message {
  public type?: string;
  public clientKey?: string;
}

export class ServerHandshake extends Message {
  public clientKey: string = "";
  public games: GameSetup[] = [];
}
