package json_schema.controller.commands;

import json_schema.BotAddress;
import json_schema.GameSetup2;
import jsweet.lang.Array;

public class StartGame extends Command {

	public static final String TYPE = "start-game";

	public StartGame() {
		super(TYPE);
	}

	public StartGame(String type) {
		super(type);
	}

	public void setGameSetup(GameSetup2 gameSetup) {
		$set("game-setup", gameSetup);
	}

	public void setBotAddresses(Array<BotAddress> botAddresses) {
		$set("bot-addresses", botAddresses);
	}
}