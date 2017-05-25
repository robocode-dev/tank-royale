package json_schema.controller.commands;

public class ListGameTypes extends Command {

	public static final String TYPE = "list-game-types";

	public ListGameTypes() {
		super(TYPE);
	}

	public ListGameTypes(String type) {
		super(type);
	}
}