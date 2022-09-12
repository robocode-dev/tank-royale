package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.mapper.InitialPositionMapper;
import dev.robocode.tankroyale.schema.BotHandshake;

import java.util.ArrayList;

import static dev.robocode.tankroyale.botapi.internal.EnvVars.BOOT_ID;

/**
 * Utility class used for creating bot handshakes.
 */
final class BotHandshakeFactory {

    static BotHandshake create(String sessionId, BotInfo botInfo, String secret) {
        BotHandshake handshake = new BotHandshake();
        handshake.setSessionId(sessionId);
        handshake.setType(BotHandshake.Type.BOT_HANDSHAKE);
        handshake.setName(botInfo.getName());
        handshake.setVersion(botInfo.getVersion());
        handshake.setAuthors(new ArrayList<>(botInfo.getAuthors()));
        handshake.setDescription(botInfo.getDescription());
        handshake.setHomepage(botInfo.getHomepage());
        handshake.setCountryCodes(new ArrayList<>(botInfo.getCountryCodes()));
        handshake.setGameTypes(new ArrayList<>(botInfo.getGameTypes()));
        handshake.setPlatform(botInfo.getPlatform());
        handshake.setProgrammingLang(botInfo.getProgrammingLang());
        handshake.setInitialPosition(InitialPositionMapper.map(botInfo.getInitialPosition()));
        handshake.setSecret(secret);
        handshake.setBootId(System.getenv(BOOT_ID));
        return handshake;
    }
}
