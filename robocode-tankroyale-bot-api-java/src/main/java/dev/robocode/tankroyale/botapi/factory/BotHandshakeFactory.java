package dev.robocode.tankroyale.botapi.factory;

import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.schema.BotHandshake;
import lombok.experimental.UtilityClass;
import lombok.val;

/** Utility class used fro creating bot handshakes */
@UtilityClass
public class BotHandshakeFactory {

  public BotHandshake create(BotInfo botInfo) {
    val handshake = new BotHandshake();
    handshake.setType(BotHandshake.Type.BOT_HANDSHAKE);
    handshake.setName(botInfo.getName());
    handshake.setVersion(botInfo.getVersion());
    handshake.setAuthor(botInfo.getAuthor());
    handshake.setCountryCode(botInfo.getCountryCode());
    handshake.setGameTypes(botInfo.getGameTypes());
    handshake.setProgrammingLang(botInfo.getProgrammingLang());
    return handshake;
  }
}
