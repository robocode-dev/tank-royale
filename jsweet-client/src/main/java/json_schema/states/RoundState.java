package json_schema.states;

public class RoundState extends jsweet.lang.Object {

	public Integer getRoundNumber() {
		return (Integer) $get("round-number");
	}

	public Integer getTurnNumber() {
		return (Integer) $get("turn-number");
	}
}