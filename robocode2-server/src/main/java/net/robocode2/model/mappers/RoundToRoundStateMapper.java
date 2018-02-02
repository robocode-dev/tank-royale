package net.robocode2.model.mappers;

import net.robocode2.json_schema.states.RoundState;
import net.robocode2.model.ITurn;
import net.robocode2.model.Round;

public final class RoundToRoundStateMapper {

	public static RoundState map(Round round, ITurn turn) {
		RoundState roundState = new RoundState();
		roundState.setRoundNumber(round.getRoundNumber());
		roundState.setTurnNumber(turn.getTurnNumber());
		return roundState;
	}
}
