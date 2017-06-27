package net.robocode2.model.mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.robocode2.json_schema.states.BotStateWithId;
import net.robocode2.model.IBot;

public final class BotsToBotsWithIdMapper {

	public static List<BotStateWithId> map(Set<IBot> bots) {
		List<BotStateWithId> botStates = new ArrayList<BotStateWithId>();
		for (IBot bot : bots) {
			botStates.add(BotToBotStateWithIdMapper.map(bot));
		}
		return botStates;
	}
}