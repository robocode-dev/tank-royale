import Vue from "vue";
import { Component } from "vue-property-decorator";
import { ConnectionStatus, Server } from "@/server/Server";
import GameSetup from "@/schemas/GameSetup";
import { ServerHandshake, BotListUpdate, BotInfo } from "@/schemas/Comm";
import state from "@/store.ts";

class GameTypeOption {
  public value: string | null;
  public text: string;

  constructor(value: string | null, text: string) {
    this.value = value;
    this.text = text;
  }
}

@Component
export default class Setup extends Vue {
  private server: Server = Server.getInstance();
  private serverUrl: string = "";

  private connectionStatus: string = ConnectionStatus.NotConnected;

  private gameSetup: GameSetup | null = null;

  private availableBots: BotInfo[] = [];
  private selectedBots: BotInfo[] = [];

  private serverHandshake: ServerHandshake | null = null;

  private gameTypeOptions: GameTypeOption[] = [];

  private rules: any = {
    arenaMinSize: 400,
    arenaMaxSize: 5000,
    minGunCoolingRate: 0.1,
    maxGunCoolingRate: 3.0,
  };

  private mounted() {
    this.setServerUrl();
  }

  private setServerUrl() {
    let serverAddr = this.$route.query.server;
    if (!serverAddr) {
      serverAddr = "localhost";
    }
    let port = this.$route.query.port;
    if (!port) {
      port = "50000";
    }
    this.serverUrl = "ws://" + serverAddr + ":" + port;
  }

  private isConnected(): boolean {
    return this.server.isConnected();
  }

  private connect() {
    const server = this.server;
    server.connect(this.serverUrl);

    const self = this;

    server.connectedEvent.on((event) => {
      self.connectionStatus = server.connectionStatus();
    });
    server.disconnectedEvent.on((event) => {
      self.connectionStatus = server.connectionStatus();

      self.onDisconnected();
    });
    server.connectionErrorEvent.on((event) => {
      self.connectionStatus = server.connectionStatus();
    });
    server.serverHandshakeEvent.on((event) => {
      self.onServerHandshake(event);
    });
    server.botListUpdateEvent.on((event) => {
      self.onBotListUpdate(event);
    });
  }

  private disconnect() {
    this.server.disconnect();
  }

  private onDisconnected() {
    this.gameSetup = null;
    this.selectedBots = [];
    this.gameTypeOptions = [];
  }

  private onServerHandshake(serverHandshake) {
    console.log("->serverHandshake");

    this.serverHandshake = serverHandshake;

    const gameTypeOptions: GameTypeOption[] = [];

    if (serverHandshake) {
      const games = serverHandshake.games;
      if (games) {
        gameTypeOptions.push(new GameTypeOption(null, "-- select --"));
        games.forEach((element) => {
          const gameType = element.gameType;
          gameTypeOptions.push(new GameTypeOption(gameType, gameType));
        });
      }
    }
    this.gameTypeOptions = gameTypeOptions;
  }

  private onBotListUpdate(botListUpdate: BotListUpdate) {
    console.log("->botListUpdate");

    const bots = botListUpdate.bots;
    for (const bot of bots) {
      bot.displayText = `${bot.name} ${bot.version} (${bot.host}:${bot.port})`;
    }
    this.availableBots = bots;
    this.availableBots.sort(this.compareBots);
  }

  private isGameTypeSelected(): boolean {
    return this.gameSetup !== null;
  }

  private onGameTypeChanged(event) {
    let foundGameSetup;
    if (this.serverHandshake) {
      foundGameSetup = this.serverHandshake.games.find(
        (gameSetup) => gameSetup.gameType === event.target.value,
      );
    }
    if (!foundGameSetup) {
      foundGameSetup = null;
    }
    this.gameSetup = foundGameSetup;
  }

  private onAvailableBotClicked(bot: BotInfo) {
    this.selectedBots.push(bot);
    this.selectedBots.sort(this.compareBots);
    this.removeItem(this.availableBots, bot);
  }

  private onSelectedBotClicked(bot: BotInfo) {
    this.availableBots.push(bot);
    this.availableBots.sort(this.compareBots);
    this.removeItem(this.selectedBots, bot);
  }

  private onAllAvailableBotsClicked() {
    this.selectedBots = this.selectedBots
      .concat(this.availableBots)
      .sort(this.compareBots);
    this.availableBots = [];
  }

  private onAllSelectedBotsClicked() {
    this.availableBots = this.availableBots.concat(this.selectedBots);
    this.availableBots.sort(this.compareBots);
    this.selectedBots = [];
  }

  private removeItem(array, item) {
    for (let i = 0; i < array.length; i++) {
      if (array[i] === item) {
        array.splice(i, 1);
        return;
      }
    }
  }

  private compareBots(b1: BotInfo, b2: BotInfo) {
    if (!b1.displayText || !b2.displayText) {
      throw new Error("compareBots: Illegal argument");
    }
    if (b1.displayText < b2.displayText) {
      return -1;
    }
    if (b1.displayText > b2.displayText) {
      return 1;
    }
    return 0;
  }

  private isGameStartValid() {
    const selectedBotsCount = this.selectedBots.length;
    const gameSetup = this.gameSetup;
    return (
      this.server.isConnected() &&
      this.isGameTypeSelected() &&
      gameSetup &&
      selectedBotsCount >= gameSetup.minNumberOfParticipants &&
      (gameSetup.maxNumberOfParticipants == null ||
        selectedBotsCount <= gameSetup.maxNumberOfParticipants)
    );
  }

  private onStartGameClicked() {
    console.log("Goto arena");

    state.saveGameSetup(this.gameSetup);
    state.saveSelectedBots(this.selectedBots);

    this.$router.push("/arena");
  }
}
