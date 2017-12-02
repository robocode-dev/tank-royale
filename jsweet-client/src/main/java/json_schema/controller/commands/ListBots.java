package json_schema.controller.commands;

import def.js.Array;

public class ListBots extends Command {

	public static final String TYPE = "listBots";

	public ListBots() {
		super(TYPE);
	}

	public ListBots(String type) {
		super(type);
	}

	public void setGameTypes(Array<String> gameTypes) {
		$set("gameTypes", gameTypes);
	}
}