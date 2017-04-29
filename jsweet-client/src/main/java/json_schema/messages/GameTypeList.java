package json_schema.messages;

import static def.jquery.Globals.$;

import jsweet.lang.Array;

public class GameTypeList extends Message2 {

	public static final String TYPE = "game-type-list";

	public GameTypeList() {
		super(TYPE);
	}

	public GameTypeList(String type) {
		super(type);
	}

	@SuppressWarnings("unchecked")
	public Array<String> getGameTypes() {
		return (Array<String>) $get("game-types");
	}

	public static GameTypeList map(Object obj) {
		return (GameTypeList) $.extend(false, new GameTypeList(), obj);
	}
}