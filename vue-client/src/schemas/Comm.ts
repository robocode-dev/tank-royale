import GameSetup from "./GameSetup";
import { EventType } from "./Events";
import { MessageType, Message } from "./Messages";

export class ServerHandshake extends Message {
  public games: GameSetup[] = [];

  constructor(clientKey: string) {
    super(MessageType.ServerHandshake, clientKey);
  }
}

export class BotHandshake extends Message {
  public name: string;
  public version: string;
  public author?: string;
  public countryCode?: string;
  public gameTypes: string[] = [];
  public programmingLanguage?: string;

  constructor(clientKey: string, name: string, version: string) {
    super(MessageType.BotHandshake, clientKey);
    this.name = name;
    this.version = version;
  }
}

export class BotInfo extends BotHandshake {
  public host: string;
  public port: number;

  public displayText?: string;

  constructor(
    clientKey: string,
    name: string,
    version: string,
    host: string,
    port: number,
  ) {
    super(clientKey, name, version);
    this.type = MessageType.BotInfo;
    this.host = host;
    this.port = port;
  }
}

export class BotListUpdate extends Event {
  public bots: BotInfo[] = [];

  constructor() {
    super(EventType.BotListUpdate);
  }
}
