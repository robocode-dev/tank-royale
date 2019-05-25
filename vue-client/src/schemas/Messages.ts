export enum MessageType {
  ServerHandshake = "ServerHandshake",
  BotHandshake = "BotHandshake",
  ControllerHandshake = "ControllerHandshake",
  BotListUpdate = "BotListUpdate",
  BotInfo = "BotInfo",
}

export class Message {
  public type: string;
  public clientKey: string;

  constructor(type: string, clientKey: string) {
    this.type = type;
    this.clientKey = clientKey;
  }
}
