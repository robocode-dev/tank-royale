package json_schema.controller.commands;

import jsweet.lang.Array;

public class ListBots extends Command {

	public static final String TYPE = "list-bots";

	public ListBots() {
		super(TYPE);
	}

	public ListBots(String type) {
		super(type);
	}

	public void setGameTypes(Array<String> gameTypes) {
		$set("game-types", gameTypes);
	}
}