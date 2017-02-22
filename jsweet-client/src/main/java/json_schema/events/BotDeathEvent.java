package json_schema.events;

public class BotDeathEvent extends Event {

	public static final String TYPE = "bot-death-event";

	public BotDeathEvent() {
		super(TYPE);
	}

	public Integer getVictimId() {
		return (Integer) $get("victim-id");
	}
}