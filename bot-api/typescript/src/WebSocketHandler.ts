import { BotInfo } from "./BotInfo.js";
import { BotHandshakeFactory } from "./BotHandshakeFactory.js";
import { EnvVars } from "./EnvVars.js";
import { BotException } from "./BotException.js";
import { RuntimeAdapter } from "./runtime/RuntimeAdapter.js";
import { WebSocketLike } from "./runtime/WebSocketLike.js";
import { MessageType } from "./protocol/MessageType.js";
import type {
  ServerHandshake,
  TickEventForBot,
  GameStartedEventForBot,
  GameEndedEventForBot,
  RoundStartedEvent,
  RoundEndedEventForBot,
  BotIntent,
  BotReady,
} from "./protocol/schema.js";

/** Callback hooks provided by the bot internals layer. */
export interface WebSocketHandlerCallbacks {
  onConnected?: () => void;
  onDisconnected?: (remote: boolean, code?: number, reason?: string) => void;
  onConnectionError?: (error: unknown) => void;
  onServerHandshake?: (handshake: ServerHandshake) => void;
  onGameStarted?: (event: GameStartedEventForBot) => void;
  onGameEnded?: (event: GameEndedEventForBot) => void;
  onGameAborted?: () => void;
  onRoundStarted?: (event: RoundStartedEvent) => void;
  onRoundEnded?: (event: RoundEndedEventForBot) => void;
  onTick?: (event: TickEventForBot) => void;
  onSkippedTurn?: (turnNumber: number) => void;
}

/**
 * Handles the WebSocket connection to the server, routing incoming messages
 * to the appropriate callback hooks. Matches Java's WebSocketHandler behavior.
 */
export class WebSocketHandler {
  private readonly adapter: RuntimeAdapter;
  private readonly serverUrl: string;
  private readonly serverSecret: string | undefined;
  private readonly botInfo: BotInfo;
  private readonly envVars: EnvVars;
  private readonly callbacks: WebSocketHandlerCallbacks;

  private socket: WebSocketLike | null = null;
  private serverHandshake: ServerHandshake | null = null;

  constructor(
    adapter: RuntimeAdapter,
    serverUrl: string,
    serverSecret: string | undefined,
    botInfo: BotInfo,
    envVars: EnvVars,
    callbacks: WebSocketHandlerCallbacks,
  ) {
    this.adapter = adapter;
    this.serverUrl = serverUrl;
    this.serverSecret = serverSecret;
    this.botInfo = botInfo;
    this.envVars = envVars;
    this.callbacks = callbacks;
  }

  /** Connects to the server WebSocket. */
  connect(): void {
    const ws = this.adapter.createWebSocket(this.serverUrl);
    this.socket = ws;

    ws.onopen = () => {
      this.callbacks.onConnected?.();
    };

    ws.onclose = (event) => {
      const e = event as { code?: number; reason?: string };
      this.callbacks.onDisconnected?.(true, e.code, e.reason);
    };

    ws.onerror = (event) => {
      this.callbacks.onConnectionError?.(event);
    };

    ws.onmessage = (event) => {
      this.handleMessage(String(event.data));
    };
  }

  /** Disconnects from the server WebSocket. */
  disconnect(): void {
    this.socket?.close();
    this.socket = null;
  }

  /** Sends a {@link BotIntent} message to the server. */
  sendBotIntent(intent: BotIntent): void {
    this.send(intent);
  }

  /** Returns the stored server handshake, or null if not yet received. */
  getServerHandshake(): ServerHandshake | null {
    return this.serverHandshake;
  }

  // ---------------------------------------------------------------------------
  // Message routing
  // ---------------------------------------------------------------------------

  private handleMessage(json: string): void {
    try {
      let msg: { type?: string };
      try {
        msg = JSON.parse(json) as { type?: string };
      } catch {
        throw new BotException("Failed to parse WebSocket message: " + json);
      }

      const type = msg.type;
      if (type == null) return;

      switch (type as MessageType) {
        case MessageType.ServerHandshake:
          this.handleServerHandshake(msg as unknown as ServerHandshake);
          break;
        case MessageType.TickEventForBot:
          this.handleTick(msg as unknown as TickEventForBot);
          break;
        case MessageType.RoundStartedEvent:
          this.handleRoundStarted(msg as unknown as RoundStartedEvent);
          break;
        case MessageType.RoundEndedEventForBot:
          this.handleRoundEnded(msg as unknown as RoundEndedEventForBot);
          break;
        case MessageType.GameStartedEventForBot:
          this.handleGameStarted(msg as unknown as GameStartedEventForBot);
          break;
        case MessageType.GameEndedEventForBot:
          this.handleGameEnded(msg as unknown as GameEndedEventForBot);
          break;
        case MessageType.SkippedTurnEvent:
          this.handleSkippedTurn(msg as unknown as { turnNumber: number });
          break;
        case MessageType.GameAbortedEvent:
          this.handleGameAborted();
          break;
        default:
          throw new BotException("Unsupported WebSocket message type: " + type);
      }
    } catch (err) {
      this.callbacks.onConnectionError?.(err);
    }
  }

  private handleServerHandshake(msg: ServerHandshake): void {
    this.serverHandshake = msg;
    this.callbacks.onServerHandshake?.(msg);

    // Validate required bot info before sending the bot handshake
    this.validateBotInfo();

    const isDroid = false; // Droid detection handled by caller via BotInfo/callbacks
    const handshake = BotHandshakeFactory.create(
      msg.sessionId,
      this.botInfo,
      isDroid,
      this.serverSecret,
      this.envVars,
    );
    this.send(handshake);
  }

  private handleGameStarted(msg: GameStartedEventForBot): void {
    this.callbacks.onGameStarted?.(msg);

    const ready: BotReady = { type: MessageType.BotReady };
    this.send(ready);
  }

  private handleTick(msg: TickEventForBot): void {
    this.callbacks.onTick?.(msg);
  }

  private handleRoundStarted(msg: RoundStartedEvent): void {
    this.callbacks.onRoundStarted?.(msg);
  }

  private handleRoundEnded(msg: RoundEndedEventForBot): void {
    this.callbacks.onRoundEnded?.(msg);
  }

  private handleGameEnded(msg: GameEndedEventForBot): void {
    this.callbacks.onGameEnded?.(msg);
  }

  private handleGameAborted(): void {
    this.callbacks.onGameAborted?.();
  }

  private handleSkippedTurn(msg: { turnNumber: number }): void {
    this.callbacks.onSkippedTurn?.(msg.turnNumber);
  }

  // ---------------------------------------------------------------------------
  // Helpers
  // ---------------------------------------------------------------------------

  /** Validates that required bot info fields are set before sending the bot handshake. */
  private validateBotInfo(): void {
    if (this.isBlank(this.botInfo.name)) {
      this.throwMissingPropertyException("name");
    }
    if (this.isBlank(this.botInfo.version)) {
      this.throwMissingPropertyException("version");
    }
    const authors = this.botInfo.authors;
    if (authors == null || authors.length === 0 || authors.every((a) => this.isBlank(a))) {
      this.throwMissingPropertyException("authors");
    }
  }

  private throwMissingPropertyException(propertyName: string): never {
    throw new BotException(
      `Required bot property '${propertyName}' is missing. ` +
        `This property is required in order for the bot to be recognized when booting it up and ` +
        `when it needs to join the game. You must set this property in your bot code ` +
        `or provide a .json configuration file.`,
    );
  }

  private isBlank(s: string | null | undefined): boolean {
    return s == null || s.trim() === "";
  }

  private send(obj: object): void {
    if (this.socket == null) {
      throw new BotException("WebSocket is not connected");
    }
    this.socket.send(JSON.stringify(obj));
  }
}
