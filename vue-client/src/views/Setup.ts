import Vue from "vue";
import { Component } from "vue-property-decorator";
import ReconnectingWebSocket from "reconnectingwebsocket";
import GameSetup from "../schemas/GameSetup";
import state from "../store/store.ts";
import { MessageType, ServerHandshake } from "@/schemas/Messages";

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
  private serverUrl: string = "";
  private clientKey?: string;

  private gameSetup: GameSetup | null = null;
  private selectedBots: string[] = [];

  private server: string = "localhost";
  private port: number = 50000;

  private socket: any;
  private connectionStatus: string = "not connected";
  private isConnected: boolean = false;

  private serverHandshake: ServerHandshake | null = null;

  private gameTypeOptions: GameTypeOption[] = [];

  private rules: any = {
    arenaMinSize: 400,
    arenaMaxSize: 5000,
    minGunCoolingRate: 0.1,
    maxGunCoolingRate: 3.0,
  };

  private availableBots: any[] = [];

  private mounted() {
    const server = this.$route.query.server;
    if (server) {
      this.server = server;
    }
    const port = this.$route.query.port;
    if (port) {
      this.port = Number(port);
    }
    this.serverUrl = "ws://" + this.server + ":" + this.port;
  }

  private onConnect() {
    let socket = this.socket;
    if (socket) {
      socket.open();
      return;
    }

    // Store the server URL
    state.saveServerUrl(this.serverUrl);

    socket = new ReconnectingWebSocket(this.serverUrl);
    this.socket = socket;

    const vm = this;

    socket.onopen = (event) => {
      console.log("ws connected to: " + event.target.url);

      vm.isConnected = true;
      vm.connectionStatus = "connected";
    };
    socket.onclose = (event) => {
      console.log("ws closed: " + event.target.url);

      vm.isConnected = false;
      vm.connectionStatus = "not connected";
    };
    socket.onerror = (event) => {
      console.log("ws error: " + event.data);

      vm.connectionStatus = "error: " + event.data;
    };
    socket.onmessage = (event) => {
      console.log("ws message: " + event.data);

      const message = JSON.parse(event.data);

      switch (message.type) {
        case MessageType.ServerHandshake:
          vm.onServerHandshake(message);
          break;
        case MessageType.BotListUpdate:
          vm.onBotListUpdate(message);
          break;
      }
    };
  }

  private onDisconnect() {
    this.socket.close();

    this.gameSetup = null;
    this.selectedBots = [];
    this.gameTypeOptions = [];
  }

  private sendControllerHandshake() {
    console.log("<-controllerHandshake");

    this.socket.send(
      JSON.stringify({
        clientKey: this.clientKey,
        type: "controllerHandshake",
        name: "Robocode 2 Game Controller",
        version: "0.1.0",
        author: "Flemming N. Larsen <fnl@users.sourceforge.net>",
      }),
    );
  }

  private onServerHandshake(serverHandshake) {
    console.log("->serverHandshake");

    this.serverHandshake = serverHandshake;

    const gameTypeOptions: GameTypeOption[] = [];

    if (serverHandshake) {
      this.clientKey = serverHandshake.clientKey;

      this.sendControllerHandshake();

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

  private onBotListUpdate(botListUpdate) {
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

  private onAvailableBotClicked(bot) {
    this.selectedBots.push(bot);
    this.selectedBots.sort(this.compareBots);
    this.removeItem(this.availableBots, bot);
  }

  private onSelectedBotClicked(bot) {
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

  private compareBots(a, b) {
    if (a.displayText < b.displayText) {
      return -1;
    }
    if (a.displayText > b.displayText) {
      return 1;
    }
    return 0;
  }

  private isGameStartValid() {
    const selectedBotsCount = this.selectedBots.length;
    const gameSetup = this.gameSetup;
    return (
      this.isConnected &&
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
