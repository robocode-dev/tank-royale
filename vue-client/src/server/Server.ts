import { TypedEvent } from "@/events/TypedEvent";
import { ServerHandshake, BotListUpdate } from "@/schemas/Comm";
import { MessageType } from "@/schemas/Messages";
import { CommandType } from "@/schemas/CommandType";
import { BotInfo } from "@/schemas/Comm";
import {
  EventType,
  TickEventForObserver,
  GameStartedEventForObserver,
  GameAbortedEventForObserver,
  GameEndedEventForObserver,
  GamePausedEventForObserver,
  GameResumedEventForObserver,
} from "@/schemas/Events";
import GameSetup from "@/schemas/GameSetup";

export enum ConnectionStatus {
  NotConnected = "not connected",
  Connected = "connected",
  Error = "error",
}

export class Server {
  public static connectedEvent = new TypedEvent<void>();
  public static disconnectedEvent = new TypedEvent<void>();
  public static connectionErrorEvent = new TypedEvent<void>();

  public static serverHandshakeEvent = new TypedEvent<ServerHandshake>();
  public static botListUpdateEvent = new TypedEvent<BotListUpdate>();
  public static tickEvent = new TypedEvent<TickEventForObserver>();
  public static gameStartedEvent = new TypedEvent<
    GameStartedEventForObserver
  >();
  public static gameAbortedEvent = new TypedEvent<
    GameAbortedEventForObserver
  >();
  public static gameEndedEvent = new TypedEvent<GameEndedEventForObserver>();
  public static gamePausedEvent = new TypedEvent<GamePausedEventForObserver>();
  public static gameResumedEvent = new TypedEvent<
    GameResumedEventForObserver
  >();

  public static connect(serverUrl: string) {
    let websocket = Server._websocket;
    if (websocket !== null && websocket !== undefined) {
      throw new Error("connect: Already connected");
    }

    Server._serverUrl = serverUrl;

    websocket = new WebSocket(serverUrl);
    Server._websocket = websocket;

    websocket.onopen = (event) => {
      console.log("ws connected to: " + event.target.url);

      Server._connectionStatus = ConnectionStatus.Connected;
      Server._connectionErrorMsg = "";

      Server.connectedEvent.emit(undefined);
    };
    websocket.onclose = (event) => {
      console.log("ws closed: " + event.target.url);

      Server._connectionStatus = ConnectionStatus.NotConnected;
      Server._connectionErrorMsg = "";

      Server.disconnectedEvent.emit(undefined);
    };
    websocket.onerror = (event) => {
      console.log("ws error: " + event.data);

      Server._connectionStatus = ConnectionStatus.Error;
      Server._connectionErrorMsg = event.data;

      Server.connectionErrorEvent.emit(undefined);
    };
    websocket.onmessage = (event) => {
      console.log("ws message: " + event.data);

      const message = JSON.parse(event.data);

      switch (message.type) {
        case MessageType.ServerHandshake:
          Server.onServerHandhake(message);
          Server.serverHandshakeEvent.emit(message);
          break;
        case MessageType.BotListUpdate:
          Server.botListUpdateEvent.emit(message);
          break;
        case EventType.TickEventForObserver:
          Server._lastTickEvent = message;
          Server.tickEvent.emit(message);
          break;
        case EventType.GameStartedEventForObserver:
          Server._gameRunning = true;
          Server._gamePaused = false;
          Server.gameStartedEvent.emit(message);
          break;
        case EventType.GameAbortedEventForObserver:
          Server._gameRunning = false;
          Server._gamePaused = false;
          Server.gameAbortedEvent.emit(message);
          break;
        case EventType.GameEndedEventForObserver:
          Server._gameRunning = false;
          Server._gamePaused = false;
          Server.gameEndedEvent.emit(message);
          break;
        case EventType.GamePausedEventForObserver:
          Server._gamePaused = true;
          Server.gamePausedEvent.emit(message);
          break;
        case EventType.GameResumedEventForObserver:
          Server._gamePaused = false;
          Server.gameResumedEvent.emit(message);
          break;
      }
    };
  }

  public static disconnect() {
    if (Server._websocket !== null) {
      Server._websocket.close();
      Server._websocket = null;
    }
  }

  public static connectionStatus(): string {
    if (Server._connectionStatus === ConnectionStatus.Error) {
      return ConnectionStatus.Error + ": " + Server._connectionErrorMsg;
    }
    return Server._connectionStatus;
  }

  public static getGameTypes(): string[] {
    return Server._games.map((game) => game.gameType);
  }

  public static isConnected(): boolean {
    return Server._connectionStatus === ConnectionStatus.Connected;
  }

  public static isGameRunning(): boolean {
    return Server._gameRunning;
  }

  public static isGamePaused(): boolean {
    return Server._gamePaused;
  }

  public static selectGameType(gameType: string): GameSetup | null {
    const gameSetup: GameSetup | undefined = Server._games.find(
      (game) => game.gameType === gameType,
    );
    if (typeof gameSetup === "undefined") {
      return (this._gameSetup = null);
    }
    return (this._gameSetup = gameSetup);
  }

  public static selectBots(bots: BotInfo[]) {
    this._selectedBots = bots;
  }

  public static sendStartGame() {
    Server._websocket.send(
      JSON.stringify({
        type: CommandType.StartGame,
        clientKey: Server._clientKey,
        gameSetup: Server._gameSetup,
        botAddresses: Server._selectedBots,
      }),
    );
  }

  public static sendStopGame() {
    Server._websocket.send(
      JSON.stringify({
        type: CommandType.StopGame,
        clientKey: Server._clientKey,
      }),
    );
  }

  public static sendPauseGame() {
    Server._websocket.send(
      JSON.stringify({
        type: CommandType.PauseGame,
        clientKey: Server._clientKey,
      }),
    );
  }

  public static sendResumeGame() {
    Server._websocket.send(
      JSON.stringify({
        type: CommandType.ResumeGame,
        clientKey: Server._clientKey,
      }),
    );
  }

  public static getLastTickEvent(): TickEventForObserver | null {
    return Server._lastTickEvent;
  }

  private static _serverUrl: string = "ws://localhost:50000";
  private static _websocket: any;
  private static _clientKey?: string;

  private static _connectionStatus: string = ConnectionStatus.NotConnected;
  private static _connectionErrorMsg: string = "";

  private static _gameRunning: boolean = false;
  private static _gamePaused: boolean = false;

  private static _games: GameSetup[];
  private static _gameSetup: GameSetup | null;

  private static _selectedBots: BotInfo[];

  private static _lastTickEvent: TickEventForObserver | null;

  private static onServerHandhake(serverHandshake: ServerHandshake) {
    Server._clientKey = serverHandshake.clientKey;
    Server._games = serverHandshake.games;
    Server.sendControllerHandshake();
  }

  private static sendControllerHandshake() {
    Server._websocket.send(
      JSON.stringify({
        type: MessageType.ControllerHandshake,
        clientKey: Server._clientKey,
        name: "Robocode 2 Web UI",
        version: "0.1.0",
        author: "Flemming N. Larsen <fnl@users.sourceforge.net>",
      }),
    );
  }
}
