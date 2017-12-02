package json_schema.controller.commands;

import json_schema.BotAddress;
import json_schema.GameSetup2;
import def.js.Array;

public class StartGame extends Command {

	public static final String TYPE = "startGame";

	public StartGame() {
		super(TYPE);
	}

	public StartGame(String type) {
		super(type);
	}

	public void setGameSetup(GameSetup2 gameSetup) {
		$set("gameSetup", gameSetup);
	}

	public void setBotAddresses(Array<BotAddress> botAddresses) {
		$set("botAddresses", botAddresses);
	}
}