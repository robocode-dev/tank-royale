import EventEmitter from "events";
import ReconnectingWebSocket from "reconnectingwebsocket";
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
  public static _instance = new Server();

  public static instance(): Server {
    return this._instance;
  }

  public connectedEvent = new TypedEvent<void>();
  public disconnectedEvent = new TypedEvent<void>();
  public connectionErrorEvent = new TypedEvent<void>();

  public serverHandshakeEvent = new TypedEvent<ServerHandshake>();
  public botListUpdateEvent = new TypedEvent<BotListUpdate>();
  public tickEvent = new TypedEvent<TickEventForObserver>();
  public gameStartedEvent = new TypedEvent<GameStartedEventForObserver>();
  public gameAbortedEvent = new TypedEvent<GameAbortedEventForObserver>();
  public gameEndedEvent = new TypedEvent<GameEndedEventForObserver>();
  public gamePausedEvent = new TypedEvent<GamePausedEventForObserver>();
  public gameResumedEvent = new TypedEvent<GameResumedEventForObserver>();

  private _socket: any;

  private _serverUrl: string = "ws://localhost:50000";
  private _clientKey?: string;

  private _connectionStatus: string = ConnectionStatus.NotConnected;
  private _connectionErrorMsg: string = "";

  public connect(serverUrl: string) {
    let socket = this._socket;
    if (socket !== null && socket !== undefined) {
      throw new Error("connect: Already connected");
    }

    this._serverUrl = serverUrl;

    socket = new ReconnectingWebSocket(serverUrl);
    this._socket = socket;

    const self = this;

    socket.onopen = (event) => {
      console.log("ws connected to: " + event.target.url);

      self._connectionStatus = ConnectionStatus.Connected;
      self._connectionErrorMsg = "";

      self.connectedEvent.emit(undefined);
    };
    socket.onclose = (event) => {
      console.log("ws closed: " + event.target.url);

      self._connectionStatus = ConnectionStatus.NotConnected;
      self._connectionErrorMsg = "";

      self.disconnectedEvent.emit(undefined);
    };
    socket.onerror = (event) => {
      console.log("ws error: " + event.data);

      self._connectionStatus = ConnectionStatus.Error;
      self._connectionErrorMsg = event.data;

      self.connectionErrorEvent.emit(undefined);
    };
    socket.onmessage = (event) => {
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
          self.tickEvent.emit(message);
          break;
        case EventType.GameStartedEventForObserver:
          self.gameStartedEvent.emit(message);
          break;
        case EventType.GameAbortedEventForObserver:
          self.gameAbortedEvent.emit(message);
          break;
        case EventType.GameEndedEventForObserver:
          self.gameEndedEvent.emit(message);
          break;
        case EventType.GamePausedEventForObserver:
          self.gamePausedEvent.emit(message);
          break;
        case EventType.GameResumedEventForObserver:
          self.gameResumedEvent.emit(message);
          break;
      }
    };
  }

  public disconnect() {
    if (this._socket !== null) {
      this._socket.close();
      this._socket = null;
    }
  }

  public connectionStatus(): string {
    if (this._connectionStatus === ConnectionStatus.Error) {
      return ConnectionStatus.Error + ": " + this._connectionErrorMsg;
    }
    return this._connectionStatus;
  }

  public isConnected(): boolean {
    return this._connectionStatus === ConnectionStatus.Connected;
  }

  public sendStartGame(gameSetup: GameSetup, botAddresses: BotInfo[]) {
    this._socket.send(
      JSON.stringify({
        clientKey: this._clientKey,
        type: CommandType.StartGame,
        gameSetup,
        botAddresses,
      }),
    );
  }

  public sendStopGame() {
    this._socket.send(
      JSON.stringify({
        clientKey: this._clientKey,
        type: CommandType.StopGame,
      }),
    );
  }

  public sendPauseGame() {
    this._socket.send(
      JSON.stringify({
        clientKey: this._clientKey,
        type: CommandType.PauseGame,
      }),
    );
  }

  public sendResumeGame() {
    this._socket.send(
      JSON.stringify({
        clientKey: this._clientKey,
        type: CommandType.ResumeGame,
      }),
    );
  }

  private onServerHandhake(serverHandshake: ServerHandshake) {
    this._clientKey = serverHandshake.clientKey;
    this.sendControllerHandshake();
  }

  private sendControllerHandshake() {
    this._socket.send(
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
