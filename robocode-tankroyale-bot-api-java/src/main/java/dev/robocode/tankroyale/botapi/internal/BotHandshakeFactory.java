package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.schema.BotHandshake;

import java.util.ArrayList;

/** Utility class used for creating bot handshakes. */
final class BotHandshakeFactory {

  static BotHandshake create(BotInfo botInfo) {
    BotHandshake handshake = new BotHandshake();
    handshake.set$type(BotHandshake.$type.BOT_HANDSHAKE);
    handshake.setName(botInfo.getName());
    handshake.setVersion(botInfo.getVersion());
    handshake.setAuthors(new ArrayList<>(botInfo.getAuthors()));
    handshake.setDescription(botInfo.getDescription());
    handshake.setUrl(botInfo.getUrl());
    handshake.setCountryCodes(new ArrayList<>(botInfo.getCountryCodes()));
    handshake.setGameTypes(new ArrayList<>(botInfo.getGameTypes()));
    handshake.setPlatform(botInfo.getPlatform());
    handshake.setProgrammingLang(botInfo.getProgrammingLang());
    return handshake;
  }
}
