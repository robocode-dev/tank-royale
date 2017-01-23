package json_schema;

public class GameSetup extends jsweet.lang.Object {

	public String getGameType() {
		return (String) $get("game-type");
	}

	public Integer getArenaWidth() {
		return (Integer) $get("arena-width");
	}

	public Integer getArenaHeight() {
		return (Integer) $get("arena-height");
	}

	public Integer getMinNumberOfParticipants() {
		return (Integer) $get("min-number-of-participants");
	}

	public Integer getMaxNumberOfParticipants() {
		return (Integer) $get("max-number-of-participants");
	}

	public Integer getNumberOfRounds() {
		return (Integer) $get("number-of-rounds");
	}

	public Double getGunCoolingRate() {
		return (Double) $get("gun-cooling-rate");
	}

	public Integer getInactivityTurns() {
		return (Integer) $get("inactivity-turns");
	}

	public Integer getTurnTimeout() {
		return (Integer) $get("turn-timeout");
	}

	public Integer getReadyTimeout() {
		return (Integer) $get("ready-timeout");
	}

	public Integer getNumberOfTurnsDelayedForObservers() {
		return (Integer) $get("number-of-turns-delayed-for-observers");
	}
}