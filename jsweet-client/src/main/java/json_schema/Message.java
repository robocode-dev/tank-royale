package json_schema;

public class Message extends jsweet.lang.Object {

	public Message(String messageType) {
		$set("message-type", messageType);
	}

	public String getMessageType() {
		return (String) $get("message-type");
	}
}