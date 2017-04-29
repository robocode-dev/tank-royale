package json_schema.controller.commands;

import java.util.Set;

public class ListGameTypes extends Command {

	public static final String TYPE = "list-game-types";

	public ListGameTypes() {
		super(TYPE);
	}

	public ListGameTypes(String type) {
		super(type);
	}

	public void setGameTypes(Set<String> gameTypes) {
		$set("game-types", gameTypes);
	}
}