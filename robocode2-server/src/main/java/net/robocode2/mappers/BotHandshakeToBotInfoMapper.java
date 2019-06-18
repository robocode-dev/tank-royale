package net.robocode2.mappers;

import java.util.Collections;

import net.robocode2.schema.BotHandshake;
import net.robocode2.schema.BotInfo;

public final class BotHandshakeToBotInfoMapper {

	private BotHandshakeToBotInfoMapper() {}

	public static BotInfo map(BotHandshake botHandshake, String hostName, Integer port) {
		BotInfo botInfo = new BotInfo();

		botInfo.setName(botHandshake.getName());
		botInfo.setVersion(botHandshake.getVersion());
		botInfo.setAuthor(botHandshake.getAuthor());
		botInfo.setDescription(botHandshake.getDescription());
		botInfo.setCountryCode(botHandshake.getCountryCode());
		botInfo.setProgrammingLang(botHandshake.getProgrammingLang());
		botInfo.setGameTypes(Collections.unmodifiableList(botHandshake.getGameTypes()));
		botInfo.setHost(hostName);
		botInfo.setPort(port);

		return botInfo;
	}
}
