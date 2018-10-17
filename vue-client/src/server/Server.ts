import EventEmitter from "events";
import ReconnectingWebSocket from "reconnectingwebsocket";
import { TypedEvent } from "@/events/TypedEvent";
import { ServerHandshake, BotListUpdate } from "@/schemas/Comm";
import { MessageType } from "@/schemas/Messages";
import {
  EventType,
  TickEventForObserver,
  GameStartedEventForObserver,
  GameAbortedEventForObserver,
  GameEndedEventForObserver,
  GamePausedEventForObserver,
  GameResumedEventForObserver
} from "@/schemas/Events";

enum ConnectionStatus {
  NotConnected = "not connected",
  Connected = "connected",
  Error = "error"
}

export const connectedEvent = new EventEmitter();
export const disconnectedEvent = new EventEmitter();
export const connectionErrorEvent = new EventEmitter();

export const serverHandshakeEvent = new TypedEvent<ServerHandshake>();
export const botListUpdateEvent = new TypedEvent<BotListUpdate>();
export const tickEvent = new TypedEvent<TickEventForObserver>();
export const gameStartedEvent = new TypedEvent<GameStartedEventForObserver>();
export const gameAbortedEvent = new TypedEvent<GameAbortedEventForObserver>();
export const gameEndedEvent = new TypedEvent<GameEndedEventForObserver>();
export const gamePausedEvent = new TypedEvent<GamePausedEventForObserver>();
export const gameResumedEvent = new TypedEvent<GameResumedEventForObserver>();

export default class Server {
  private _socket: any;

  private _serverUrl?: string;
  private _clientKey?: string;

  private _connectionStatus: string = ConnectionStatus.NotConnected;
  private _connectionErrorMsg: string = "";

  get connectionStatus(): string {
    if (this._connectionStatus === ConnectionStatus.Error) {
      return ConnectionStatus.Error + ": " + this._connectionErrorMsg;
    }
    return this._connectionStatus;
  }

  get isConnected(): boolean {
    return this._connectionStatus === ConnectionStatus.Connected;
  }

  public connect(serverUrl: string): void {
    let socket = this._socket;
    if (socket !== null) {
      throw new Error("connect: Already connected");
    }

    this._serverUrl = serverUrl;

    socket = new ReconnectingWebSocket(serverUrl);
    this._socket = socket;

    const self = this;

    socket.onopen = event => {
      console.log("ws connected to: " + event.target.url);

      self._connectionStatus = ConnectionStatus.Connected;
      self._connectionErrorMsg = "";

      connectedEvent.emit();
    };
    socket.onclose = event => {
      console.log("ws closed: " + event.target.url);

      self._connectionStatus = ConnectionStatus.NotConnected;
      self._connectionErrorMsg = "";

      disconnectedEvent.emit();
    };
    socket.onerror = event => {
      console.log("ws error: " + event.data);

      self._connectionStatus = ConnectionStatus.Error;
      self._connectionErrorMsg = event.data;

      connectionErrorEvent.emit();
    };
    socket.onmessage = event => {
      console.log("ws message: " + event.data);

      const message = JSON.parse(event.data);

      switch (message.type) {
        case MessageType.ServerHandshake:
          serverHandshakeEvent.emit(message);
          break;
        case MessageType.BotListUpdate:
          botListUpdateEvent.emit(message);
          break;
        case EventType.TickEventForObserver:
          tickEvent.emit(message);
          break;
        case EventType.GameStartedEventForObserver:
          gameStartedEvent.emit(message);
          break;
        case EventType.GameAbortedEventForObserver:
          gameAbortedEvent.emit(message);
          break;
        case EventType.GameEndedEventForObserver:
          gameEndedEvent.emit(message);
          break;
        case EventType.GamePausedEventForObserver:
          gamePausedEvent.emit(message);
          break;
        case EventType.GameResumedEventForObserver:
          gameResumedEvent.emit(message);
          break;
      }
    };
  }

  public disconnect(): void {
    if (this._socket !== null) {
      this._socket.close();
      this._socket = null;
    }
  }
}
