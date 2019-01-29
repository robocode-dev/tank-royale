package net.robocode2.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.robocode2.schema.events.BotStateWithId;
import net.robocode2.model.Bot;

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