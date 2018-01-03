package json_schema.events;

public class RoundState extends def.js.Object {

	public Integer getRoundNumber() {
		return (Integer) $get("roundNumber");
	}

	public Integer getTurnNumber() {
		return (Integer) $get("turnNumber");
	}
}