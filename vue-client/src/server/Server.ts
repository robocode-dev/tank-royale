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
    let websocket = this._websocket;
    if (websocket !== null && websocket !== undefined) {
      throw new Error("connect: Already connected");
    }

    this._serverUrl = serverUrl;

    websocket = new WebSocket(serverUrl);
    this._websocket = websocket;

    const self = this;

    websocket.onopen = (event) => {
      console.log("ws connected to: " + event.target.url);

      self._connectionStatus = ConnectionStatus.Connected;
      self._connectionErrorMsg = "";

      self.connectedEvent.emit(undefined);
    };
    websocket.onclose = (event) => {
      console.log("ws closed: " + event.target.url);

      self._connectionStatus = ConnectionStatus.NotConnected;
      self._connectionErrorMsg = "";

      self.disconnectedEvent.emit(undefined);
    };
    websocket.onerror = (event) => {
      console.log("ws error: " + event.data);

      self._connectionStatus = ConnectionStatus.Error;
      self._connectionErrorMsg = event.data;

      self.connectionErrorEvent.emit(undefined);
    };
    websocket.onmessage = (event) => {
      console.log("ws message: " + event.data);

      const message = JSON.parse(event.data);

      switch (message.type) {
        case MessageType.ServerHandshake:
          self.onServerHandhake(message);
          self.serverHandshakeEvent.emit(message);
          break;
        case MessageType.BotListUpdate:
          self.botListUpdateEvent.emit(message);
          break;
        case EventType.TickEventForObserver:
          self._lastTickEvent = message;
          self.tickEvent.emit(message);
          break;
        case EventType.GameStartedEventForObserver:
          self._gameRunning = true;
          self._gamePaused = false;
          self.gameStartedEvent.emit(message);
          break;
        case EventType.GameAbortedEventForObserver:
          self._gameRunning = false;
          self._gamePaused = false;
          self.gameAbortedEvent.emit(message);
          break;
        case EventType.GameEndedEventForObserver:
          self._gameRunning = false;
          self._gamePaused = false;
          self.gameEndedEvent.emit(message);
          break;
        case EventType.GamePausedEventForObserver:
          self._gamePaused = true;
          self.gamePausedEvent.emit(message);
          break;
        case EventType.GameResumedEventForObserver:
          self._gamePaused = false;
          self.gameResumedEvent.emit(message);
          break;
      }
    };
  }

  public static disconnect() {
    if (this._websocket !== null) {
      this._websocket.close();
      this._websocket = null;
    }
  }

  public static connectionStatus(): string {
    if (this._connectionStatus === ConnectionStatus.Error) {
      return ConnectionStatus.Error + ": " + this._connectionErrorMsg;
    }
    return this._connectionStatus;
  }

  public static isConnected(): boolean {
    return this._connectionStatus === ConnectionStatus.Connected;
  }

  public static isGameRunning(): boolean {
    return this._gameRunning;
  }

  public static isGamePaused(): boolean {
    return this._gamePaused;
  }

  public static sendStartGame(gameSetup: GameSetup, botAddresses: BotInfo[]) {
    this._websocket.send(
      JSON.stringify({
        clientKey: this._clientKey,
        type: CommandType.StartGame,
        gameSetup,
        botAddresses,
      }),
    );
  }

  public static sendStopGame() {
    this._websocket.send(
      JSON.stringify({
        clientKey: this._clientKey,
        type: CommandType.StopGame,
      }),
    );
  }

  public static sendPauseGame() {
    this._websocket.send(
      JSON.stringify({
        clientKey: this._clientKey,
        type: CommandType.PauseGame,
      }),
    );
  }

  public static sendResumeGame() {
    this._websocket.send(
      JSON.stringify({
        clientKey: this._clientKey,
        type: CommandType.ResumeGame,
      }),
    );
  }

  public static getLastTickEvent(): TickEventForObserver | null {
    return this._lastTickEvent;
  }

  private static _websocket: any;

  private static _serverUrl: string = "ws://localhost:50000";
  private static _clientKey?: string;

  private static _connectionStatus: string = ConnectionStatus.NotConnected;
  private static _connectionErrorMsg: string = "";

  private static _gameRunning: boolean = false;
  private static _gamePaused: boolean = false;

  private static _lastTickEvent: TickEventForObserver | null;

  private static onServerHandhake(serverHandshake: ServerHandshake) {
    this._clientKey = serverHandshake.clientKey;
    this.sendControllerHandshake();
  }

  private static sendControllerHandshake() {
    this._websocket.send(
      JSON.stringify({
        clientKey: this._clientKey,
        type: MessageType.ControllerHandshake,
        name: "Robocode 2 Web UI",
        version: "0.1.0",
        author: "Flemming N. Larsen <fnl@users.sourceforge.net>",
      }),
    );
  }
}
