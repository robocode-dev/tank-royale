package net.robocode2.mappers;

import net.robocode2.json_schema.states.RoundState;
import net.robocode2.model.Turn;
import net.robocode2.model.Round;

public final class RoundToRoundStateMapper {

	private RoundToRoundStateMapper() {}

	public static RoundState map(Round round, Turn turn) {
		RoundState roundState = new RoundState();
		roundState.setRoundNumber(round.getRoundNumber());
		roundState.setTurnNumber(turn.getTurnNumber());
		return roundState;
	}
}
