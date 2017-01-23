package json_schema;

import jsweet.lang.Array;

public class Participant extends jsweet.lang.Object {

	public Integer getId() {
		return (Integer) $get("id");
	}

	public String getName() {
		return (String) $get("name");
	}

	public String getVersion() {
		return (String) $get("version");
	}

	@SuppressWarnings("unchecked")
	public Array<String> getGameTypes() {
		return (Array<String>) $get("game-types");
	}

	public String getAuthor() {
		return (String) $get("author");
	}

	public String getCountryCode() {
		return (String) $get("country-code");
	}

	public String getProgrammingLanguage() {
		return (String) $get("programming-language");
	}
}