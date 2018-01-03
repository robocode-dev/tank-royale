package json_schema.comm;

import def.js.Array;

public class BotHandshake extends Message2 {

	public static final String TYPE = "botHandshake";

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
		return (String) $get("countryCode");
	}

	@SuppressWarnings("unchecked")
	public Array<String> getGameTypes() {
		return (Array<String>) $get("gameTypes");
	}

	public String getProgrammingLanguage() {
		return (String) $get("programmingLanguage");
	}
}