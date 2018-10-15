export enum MessageType {
  ServerHandshake = "serverHandshake",
  BotHandshake = "botHandshake",
  BotListUpdate = "botListUpdate",
  BotInfo = "botInfo",
}

export class Message {
  public type: string;
  public clientKey: string;

  constructor(type: string, clientKey: string) {
    this.type = type;
    this.clientKey = clientKey;
  }
}
