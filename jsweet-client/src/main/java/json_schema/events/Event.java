package json_schema.events;

public class Event extends jsweet.lang.Object {

	public Event(String type) {
		$set("type", type);
	}

	public String getType() {
		return (String) $get("type");
	}
}