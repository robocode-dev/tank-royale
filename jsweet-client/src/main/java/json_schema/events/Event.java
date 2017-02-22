package json_schema.events;

public class Event extends jsweet.lang.Object {

	public Event(String messageType) {
		$set("message-type", messageType);
	}

	public String getMessageType() {
		return (String) $get("message-type");
	}
}