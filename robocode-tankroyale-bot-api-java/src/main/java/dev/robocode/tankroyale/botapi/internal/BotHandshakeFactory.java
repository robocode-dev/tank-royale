package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.schema.BotHandshake;

import java.util.ArrayList;

/** Utility class used fro creating bot handshakes. */
public final class BotHandshakeFactory {

  public static BotHandshake create(BotInfo botInfo) {
    BotHandshake handshake = new BotHandshake();
    handshake.set$type(BotHandshake.$type.BOT_HANDSHAKE);
    handshake.setName(botInfo.getName());
    handshake.setVersion(botInfo.getVersion());
    handshake.setAuthor(botInfo.getAuthor());
    handshake.setDescription(botInfo.getDescription());
    handshake.setUrl(botInfo.getUrl());
    handshake.setCountryCode(botInfo.getCountryCode());
    handshake.setGameTypes(new ArrayList<>(botInfo.getGameTypes()));
    handshake.setPlatform(botInfo.getPlatform());
    handshake.setProgrammingLang(botInfo.getProgrammingLang());
    return handshake;
  }
}
