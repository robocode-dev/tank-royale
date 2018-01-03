package json_schema.comm;

import static def.jquery.Globals.$;

public class Message extends def.js.Object {

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