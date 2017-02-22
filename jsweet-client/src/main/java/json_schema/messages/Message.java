package json_schema.messages;

import static def.jquery.Globals.$;

public class Message extends jsweet.lang.Object {

	protected Message(String type) {
		$set("type", type);
	}

	public String getType() {
		return (String) $get("type");
	}

	public static Message map(Object obj) {
		return (Message) $.extend(false, new Message(null), obj);
	}
}