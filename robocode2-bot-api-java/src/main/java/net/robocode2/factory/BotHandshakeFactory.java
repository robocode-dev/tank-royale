package net.robocode2.factory;

import lombok.experimental.UtilityClass;
import lombok.val;
import net.robocode2.BotInfo;
import net.robocode2.schema.BotHandshake;

/** Utility class used fro creating bot handshakes */
@UtilityClass
public class BotHandshakeFactory {

  public BotHandshake create(String clientKey, BotInfo botInfo) {
    val handshake = new BotHandshake();
    handshake.setType(BotHandshake.Type.BOT_HANDSHAKE);
    handshake.setClientKey(clientKey);
    handshake.setName(botInfo.getName());
    handshake.setVersion(botInfo.getVersion());
    handshake.setAuthor(botInfo.getAuthor());
    handshake.setCountryCode(botInfo.getCountryCode());
    handshake.setGameTypes(botInfo.getGameTypes());
    handshake.setProgrammingLang(botInfo.getProgrammingLang());
    return handshake;
  }
}
