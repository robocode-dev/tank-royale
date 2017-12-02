package json_schema;

public class GameSetup extends def.js.Object {

	public String getGameType() {
		return (String) $get("gameType");
	}

	public void setGameType(String gameType) {
		$set("gameType", gameType);
	}

	public Integer getArenaWidth() {
		return (Integer) $get("arenaWidth");
	}

	public void setArenaWidth(Integer arenaWidth) {
		$set("arenaWidth", arenaWidth);
	}

	public Boolean isArenaWidthFixed() {
		return (Boolean) $get("isArenaWidthLocked");
	}

	public Integer getArenaHeight() {
		return (Integer) $get("arenaHeight");
	}

	public void setArenaHeight(Integer arenaHeight) {
		$set("arenaHeight", arenaHeight);
	}

	public Boolean isArenaHeightFixed() {
		return (Boolean) $get("isArenaHeightLocked");
	}

	public Integer getMinNumberOfParticipants() {
		return (Integer) $get("minNumberOfParticipants");
	}

	public void setMinNumberOfParticipants(Integer minNumberOfParticipants) {
		$set("minNumberOfParticipants", minNumberOfParticipants);
	}

	public Boolean isMinNumberOfParticipantsFixed() {
		return (Boolean) $get("isMinNumberOfParticipantsLocked");
	}

	public Integer getMaxNumberOfParticipants() {
		return (Integer) $get("maxNumberOfParticipants");
	}

	public void setMaxNumberOfParticipants(Integer maxNumberOfParticipants) {
		$set("maxNumberOfParticipants", maxNumberOfParticipants);
	}

	public Boolean isMaxNumberOfParticipantsFixed() {
		return (Boolean) $get("isMaxNumberOfParticipantsLocked");
	}

	public Integer getNumberOfRounds() {
		return (Integer) $get("numberOfRounds");
	}

	public void setNumberOfRounds(Integer numberOfRounds) {
		$set("numberOfRounds", numberOfRounds);
	}

	public Boolean isNumberOfRoundsFixed() {
		return (Boolean) $get("isNumberOfRoundsLocked");
	}

	public Double getGunCoolingRate() {
		return (Double) $get("gunCoolingRate");
	}

	public void setGunCoolingRate(Double gunCoolingRate) {
		$set("gunCoolingRate", gunCoolingRate);
	}

	public Boolean isGunCoolingRateFixed() {
		return (Boolean) $get("isGunCoolingRateLocked");
	}

	public Integer getInactivityTurns() {
		return (Integer) $get("inactivityTurns");
	}

	public void setInactivityTurns(Integer inactivityTurns) {
		$set("inactivityTurns", inactivityTurns);
	}

	public Boolean isInactivityTurnsFixed() {
		return (Boolean) $get("isInactivityTurnsLocked");
	}

	public Integer getTurnTimeout() {
		return (Integer) $get("turnTimeout");
	}

	public void setTurnTimeout(Integer turnTimeout) {
		$set("turnTimeout", turnTimeout);
	}

	public Boolean isTurnTimeoutFixed() {
		return (Boolean) $get("isTurnTimeoutLocked");
	}

	public Integer getReadyTimeout() {
		return (Integer) $get("readyTimeout");
	}

	public void setReadyTimeout(Integer readyTimeout) {
		$set("readyTimeout", readyTimeout);
	}

	public Boolean isReadyTimeout() {
		return (Boolean) $get("isReadyTimeoutLocked");
	}

	public Integer getDelayedObserverTurns() {
		return (Integer) $get("delayedObserverTurns");
	}

	public void setDelayedObserverTurns(Integer delayedObserverTurns) {
		$set("delayedObserverTurns", delayedObserverTurns);
	}

	public Boolean isDelayedObserverTurnsFixed() {
		return (Boolean) $get("isDelayedObserverTurnsLocked");
	}
}