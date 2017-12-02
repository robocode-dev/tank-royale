package json_schema;

import def.js.Array;

public class Participant extends def.js.Object {

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
		return (Array<String>) $get("gameTypes");
	}

	public String getAuthor() {
		return (String) $get("author");
	}

	public String getCountryCode() {
		return (String) $get("countryCode");
	}

	public String getProgrammingLanguage() {
		return (String) $get("programmingLanguage");
	}
}