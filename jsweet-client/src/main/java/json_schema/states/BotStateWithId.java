package json_schema.states;

public class BotStateWithId extends BotState {

	public Integer getId() {
		return (Integer) $get("id");
	}
}
