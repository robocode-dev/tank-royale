package json_schema.events;

public class BotStateWithId extends BotState {

	public Integer getId() {
		return (Integer) $get("id");
	}
}
