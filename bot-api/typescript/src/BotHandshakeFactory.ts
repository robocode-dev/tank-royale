import { BotInfo } from "./BotInfo.js";
import { EnvVars } from "./EnvVars.js";
import { MessageType } from "./protocol/MessageType.js";
import type { BotHandshake, InitialPosition as SchemaInitialPosition } from "./protocol/schema.js";

/**
 * Utility class for creating {@link BotHandshake} protocol messages.
 * Matches Java's BotHandshakeFactory exactly.
 */
export class BotHandshakeFactory {
  private constructor() {}

  static create(
    sessionId: string,
    botInfo: BotInfo,
    isDroid: boolean,
    secret: string | undefined,
    envVars: EnvVars,
  ): BotHandshake {
    const ip = botInfo.initialPosition;
    const schemaInitialPosition: SchemaInitialPosition | null =
      ip != null ? { x: ip.x ?? null, y: ip.y ?? null, direction: ip.direction ?? null } : null;

    const handshake: BotHandshake = {
      type: MessageType.BotHandshake,
      sessionId,
      name: botInfo.name ?? "",
      version: botInfo.version ?? "",
      authors: [...(botInfo.authors ?? [])],
    };
    if (botInfo.description != null) handshake.description = botInfo.description;
    if (botInfo.homepage != null) handshake.homepage = botInfo.homepage;
    if (botInfo.countryCodes.length > 0) handshake.countryCodes = [...botInfo.countryCodes];
    if (botInfo.gameTypes.length > 0) handshake.gameTypes = [...botInfo.gameTypes];
    if (botInfo.platform != null) handshake.platform = botInfo.platform;
    if (botInfo.programmingLang != null) handshake.programmingLang = botInfo.programmingLang;
    if (schemaInitialPosition != null) handshake.initialPosition = schemaInitialPosition;
    const teamId = envVars.getTeamId();
    if (teamId != null) handshake.teamId = teamId;
    const teamName = envVars.getTeamName();
    if (teamName != null) handshake.teamName = teamName;
    const teamVersion = envVars.getTeamVersion();
    if (teamVersion != null) handshake.teamVersion = teamVersion;
    handshake.isDroid = isDroid;
    if (secret != null) handshake.secret = secret;

    // Set debuggerAttached field (ADR-0035)
    const debuggerAttached = BotHandshakeFactory.isDebuggerAttached(envVars);
    handshake.debuggerAttached = debuggerAttached;

    // Log hint if debugger is detected
    if (debuggerAttached) {
      console.log("Debugger detected. Consider enabling breakpoint mode for this bot in the controller.");
    }

    return handshake;
  }

  /**
   * Detects if a debugger is attached to the process.
   * Checks the ROBOCODE_DEBUG env var first; falls back to inspecting Node.js
   * process arguments for --inspect / --debug flags. Returns false in
   * browser environments where process is not available.
   */
  private static isDebuggerAttached(envVars: EnvVars): boolean {
    const envFlag = envVars.getRobocodeDebug();
    if (envFlag?.toLowerCase() === "true") return true;
    if (envFlag?.toLowerCase() === "false") return false;

    // Node.js native check — safe to call in browser (process is undefined there)
    try {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const args: string[] = (globalThis as any).process?.execArgv ?? [];
      return args.some((a: string) => /--inspect|--debug/.test(a));
    } catch {
      return false;
    }
  }
}
