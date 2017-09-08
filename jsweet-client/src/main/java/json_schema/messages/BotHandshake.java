package json_schema.messages;

import def.js.Array;

public class BotHandshake extends Message2 {

	public static final String TYPE = "bot-handshake";

	public BotHandshake() {
		super(TYPE);
	}

	public BotHandshake(String type) {
		super(type);
	}

	public String getName() {
		return (String) $get("name");
	}

	public String getVersion() {
		return (String) $get("version");
	}

	public String getAuthor() {
		return (String) $get("author");
	}

	public String getContryCode() {
		return (String) $get("country-code");
	}

	@SuppressWarnings("unchecked")
	public Array<String> getGameTypes() {
		return (Array<String>) $get("game-types");
	}

	public String getProgrammingLanguage() {
		return (String) $get("programming-language");
	}
}