package json_schema;

public class GameSetup2 extends jsweet.lang.Object {

	public String getGameType() {
		return (String) $get("game-type");
	}

	public Integer getArenaWidth() {
		return (Integer) $get("arena-width");
	}

	public Boolean isArenaWidthFixed() {
		return (Boolean) $get("is-arena-width-fixed");
	}

	public Integer getArenaHeight() {
		return (Integer) $get("arena-height");
	}

	public Boolean isArenaHeightFixed() {
		return (Boolean) $get("is-arena-height-fixed");
	}

	public Integer getMinNumberOfParticipants() {
		return (Integer) $get("min-number-of-participants");
	}

	public Boolean isMinNumberOfParticipantsFixed() {
		return (Boolean) $get("is-min-number-of-participants-fixed");
	}

	public Integer getMaxNumberOfParticipants() {
		return (Integer) $get("max-number-of-participants");
	}

	public Boolean isMaxNumberOfParticipantsFixed() {
		return (Boolean) $get("is-max-number-of-participants-fixed");
	}

	public Integer getNumberOfRounds() {
		return (Integer) $get("number-of-rounds");
	}

	public Boolean isNumberOfRoundsFixed() {
		return (Boolean) $get("is-number-of-rounds-fixed");
	}

	public Double getGunCoolingRate() {
		return (Double) $get("gun-cooling-rate");
	}

	public Boolean isGunCoolingRateFixed() {
		return (Boolean) $get("is-gun-cooling-rate-fixed");
	}

	public Integer getInactivityTurns() {
		return (Integer) $get("inactivity-turns");
	}

	public Boolean isInactivityTurnsFixed() {
		return (Boolean) $get("is-inactivity-turns-fixed");
	}

	public Integer getTurnTimeout() {
		return (Integer) $get("turn-timeout");
	}

	public Boolean isTurnTimeoutFixed() {
		return (Boolean) $get("is-turn-timeout-fixed");
	}

	public Integer getReadyTimeout() {
		return (Integer) $get("ready-timeout");
	}

	public Boolean isReadyTimeout() {
		return (Boolean) $get("is-ready-timeout-fixed");
	}

	public Integer getDelayedObserverTurns() {
		return (Integer) $get("delayed-observer-turns");
	}

	public Boolean isDelayedObserverTurnsFixed() {
		return (Boolean) $get("is-delayed-observer-turns-fixed");
	}
}