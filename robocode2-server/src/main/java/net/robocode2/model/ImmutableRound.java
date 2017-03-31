package net.robocode2.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ImmutableRound implements IRound {

	private final int roundNumber;
	private final List<ITurn> turns;
	private final boolean roundEnded;

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

	@Override
	public int getRoundNumber() {
		return roundNumber;
	}

	@Override
	public List<ITurn> getTurns() {
		return Collections.unmodifiableList(turns);
	}

	@Override
	public boolean isRoundEnded() {
		return roundEnded;
	}
}