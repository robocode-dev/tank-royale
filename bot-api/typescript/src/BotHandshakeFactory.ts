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
    return handshake;
  }
}
