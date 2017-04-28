package json_schema.messages;

import jsweet.lang.Array;

public class BotList extends Message {

	public BotList() {
		super("bot-list");
	}

	public Array<BotInfo> getBots() {
		return (Array<BotInfo>) $get("bot");
	}
}