package json_schema.messages;

import static def.jquery.Globals.$;

import java.util.HashSet;
import java.util.Set;

import def.js.Array;

public class BotList extends Message2 {

	public static final String TYPE = "bot-list";

	public BotList() {
		super(TYPE);
	}

	public BotList(String type) {
		super(type);
	}

	public Set<BotInfo> getBots() {

		@SuppressWarnings("unchecked")
		Array<BotInfo> array = (Array<BotInfo>) $get("bots");

		Set<BotInfo> set = new HashSet<>();
		for (BotInfo obj : array) {
			BotInfo botInfo = (BotInfo) $.extend(false, new BotInfo(), obj);
			set.add(botInfo);
		}
		return set;
	}

	public static BotList map(Object obj) {
		return (BotList) $.extend(false, new BotList(), obj);
	}
}