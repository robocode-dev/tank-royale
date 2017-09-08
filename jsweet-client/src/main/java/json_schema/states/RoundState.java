package json_schema.states;

public class RoundState extends def.js.Object {

	public Integer getRoundNumber() {
		return (Integer) $get("round-number");
	}

	public Integer getTurnNumber() {
		return (Integer) $get("turn-number");
	}
}