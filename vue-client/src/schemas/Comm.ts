import GameSetup from "./GameSetup";
import { Message } from "./Messages";

export class ServerHandshake extends Message {
  public clientKey: string = "";
  public games: GameSetup[] = [];
}

export class BotHandshake extends Message {
  public name?: string;
  public version?: string;
  public author?: string;
  public countryCode?: string;
  public gameTypes: string[] = [];
  private programmingLanguage?: string;
}

export class BotInfo extends BotHandshake {
  public host?: string;
  public port?: number;

  public displayText?: string;
}

export class BotListUpdate extends Event {
  public bots: BotInfo[] = [];
}
