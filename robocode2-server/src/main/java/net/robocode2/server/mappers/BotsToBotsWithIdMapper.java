package net.robocode2.server.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.robocode2.json_schema.states.BotStateWithId;
import net.robocode2.model.Bot;

public final class BotsToBotsWithIdMapper {

	public static List<BotStateWithId> map(Set<Bot> bots) {
		List<BotStateWithId> botStates = new ArrayList<BotStateWithId>();
		for (Bot bot : bots) {
			botStates.add(BotToBotStateWithIdMapper.map(bot));
		}
		return botStates;
	}
}