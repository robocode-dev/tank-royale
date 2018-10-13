export class Message {
  public type?: string;
  public clientKey?: string;
}

export class ServerHandshake extends Message {}
