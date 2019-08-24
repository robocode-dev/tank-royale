package dev.robocode.tankroyale.server.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dev.robocode.tankroyale.server.model.Bot;
import net.robocode2.schema.BotStateWithId;

public final class BotsToBotsWithIdMapper {

	private BotsToBotsWithIdMapper() {}

	public static List<BotStateWithId> map(Set<Bot> bots) {
		List<BotStateWithId> botStates = new ArrayList<>();
		for (Bot bot : bots) {
			botStates.add(BotToBotStateWithIdMapper.map(bot));
		}
		return botStates;
	}
}