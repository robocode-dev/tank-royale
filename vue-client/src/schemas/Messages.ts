export enum MessageType {
  ServerHandshake = "serverHandshake",
  BotListUpdate = "botListUpdate",
}

export class Message {
  public type?: string;
  public clientKey?: string;
}
