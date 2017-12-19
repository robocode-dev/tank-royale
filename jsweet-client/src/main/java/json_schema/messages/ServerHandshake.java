package json_schema.messages;

import static def.jquery.Globals.$;

import java.util.ArrayList;
import java.util.List;

import def.js.Array;
import json_schema.GameSetup2;

public class ServerHandshake extends Message2 {

	public static final String TYPE = "serverHandshake";

	public ServerHandshake() {
		super(TYPE);
	}

	public ServerHandshake(String type) {
		super(type);
	}

	public String getProtocolVersion() {
		return (String) $get("protocolVersion");
	}


	public List<GameSetup2> getGames() {

		@SuppressWarnings("unchecked")
		Array<GameSetup2> array = (Array<GameSetup2>) $get("games");

		List<GameSetup2> list = new ArrayList<>();
		for (GameSetup2 obj : array) {
			GameSetup2 gameSetup = (GameSetup2) $.extend(false, new GameSetup2(), obj);
			list.add(gameSetup);
		}
		return list;
	}

	public static ServerHandshake map(Object obj) {
		return (ServerHandshake) $.extend(false, new ServerHandshake(), obj);
	}
}