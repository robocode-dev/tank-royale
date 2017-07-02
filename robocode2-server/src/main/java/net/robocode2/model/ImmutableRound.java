package net.robocode2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable round instance.
 *
 * @author Flemming N. Larsen
 */
public final class ImmutableRound implements IRound {

	/** Round number */
	private final int roundNumber;
	/** List of turns */
	private final List<ITurn> turns;
	/** Flag specifying if round has ended */
	private final boolean roundEnded;

	/**
	 * Creates a immutable game state.
	 *
	 * @param roundNumber
	 *            is the round number
	 * @param turns
	 *            is the list of turns
	 * @param roundEnded
	 *            specifies if the round has ended
	 */
	public ImmutableRound(int roundNumber, List<ITurn> turns, boolean roundEnded) {
		this.roundNumber = roundNumber;
		List<ImmutableTurn> immuTurns = new ArrayList<>();
		if (turns != null) {
			for (ITurn turn : turns) {
				immuTurns.add(new ImmutableTurn(turn));
			}
		}
		this.turns = Collections.unmodifiableList(new ArrayList<>(turns));
		this.roundEnded = roundEnded;
	}

	/**
	 * Creates a immutable round based on another round.
	 * 
	 * @param bot
	 *            is the round that is deep copied into this round.
	 */
	public ImmutableRound(IRound round) {
		this(round.getRoundNumber(), round.getTurns(), round.isRoundEnded());
	}

	@Override
	public int getRoundNumber() {
		return roundNumber;
	}

	@Override
	public List<ITurn> getTurns() {
		return turns;
	}

	@Override
	public boolean isRoundEnded() {
		return roundEnded;
	}
}