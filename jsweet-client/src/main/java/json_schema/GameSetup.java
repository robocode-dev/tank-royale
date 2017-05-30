package json_schema;

public class GameSetup extends jsweet.lang.Object {

	public String getGameType() {
		return (String) $get("game-type");
	}

	public void setGameType(String gameType) {
		$set("game-type", gameType);
	}

	public Integer getArenaWidth() {
		return (Integer) $get("arena-width");
	}

	public void setArenaWidth(Integer arenaWidth) {
		$set("arena-width", arenaWidth);
	}

	public Boolean isArenaWidthFixed() {
		return (Boolean) $get("is-arena-width-fixed");
	}

	public Integer getArenaHeight() {
		return (Integer) $get("arena-height");
	}

	public void setArenaHeight(Integer arenaHeight) {
		$set("arena-height", arenaHeight);
	}

	public Boolean isArenaHeightFixed() {
		return (Boolean) $get("is-arena-height-fixed");
	}

	public Integer getMinNumberOfParticipants() {
		return (Integer) $get("min-number-of-participants");
	}

	public void setMinNumberOfParticipants(Integer minNumberOfParticipants) {
		$set("min-number-of-participants", minNumberOfParticipants);
	}

	public Boolean isMinNumberOfParticipantsFixed() {
		return (Boolean) $get("is-min-number-of-participants-fixed");
	}

	public Integer getMaxNumberOfParticipants() {
		return (Integer) $get("max-number-of-participants");
	}

	public void setMaxNumberOfParticipants(Integer maxNumberOfParticipants) {
		$set("max-number-of-participants", maxNumberOfParticipants);
	}

	public Boolean isMaxNumberOfParticipantsFixed() {
		return (Boolean) $get("is-max-number-of-participants-fixed");
	}

	public Integer getNumberOfRounds() {
		return (Integer) $get("number-of-rounds");
	}

	public void setNumberOfRounds(Integer numberOfRounds) {
		$set("number-of-rounds", numberOfRounds);
	}

	public Boolean isNumberOfRoundsFixed() {
		return (Boolean) $get("is-number-of-rounds-fixed");
	}

	public Double getGunCoolingRate() {
		return (Double) $get("gun-cooling-rate");
	}

	public void setGunCoolingRate(Double gunCoolingRate) {
		$set("gun-cooling-rate", gunCoolingRate);
	}

	public Boolean isGunCoolingRateFixed() {
		return (Boolean) $get("is-gun-cooling-rate-fixed");
	}

	public Integer getInactivityTurns() {
		return (Integer) $get("inactivity-turns");
	}

	public void setInactivityTurns(Integer inactivityTurns) {
		$set("inactivity-turns", inactivityTurns);
	}

	public Boolean isInactivityTurnsFixed() {
		return (Boolean) $get("is-inactivity-turns-fixed");
	}

	public Integer getTurnTimeout() {
		return (Integer) $get("turn-timeout");
	}

	public void setTurnTimeout(Integer turnTimeout) {
		$set("turn-timeout", turnTimeout);
	}

	public Boolean isTurnTimeoutFixed() {
		return (Boolean) $get("is-turn-timeout-fixed");
	}

	public Integer getReadyTimeout() {
		return (Integer) $get("ready-timeout");
	}

	public void setReadyTimeout(Integer readyTimeout) {
		$set("ready-timeout", readyTimeout);
	}

	public Boolean isReadyTimeout() {
		return (Boolean) $get("is-ready-timeout-fixed");
	}

	public Integer getDelayedObserverTurns() {
		return (Integer) $get("delayed-observer-turns");
	}

	public void setDelayedObserverTurns(Integer delayedObserverTurns) {
		$set("delayed-observer-turns", delayedObserverTurns);
	}

	public Boolean isDelayedObserverTurnsFixed() {
		return (Boolean) $get("is-delayed-observer-turns-fixed");
	}
}