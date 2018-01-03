package json_schema.comm;

import static def.jquery.Globals.$;

public class Message2 extends def.js.Object {

	protected Message2(String type) {
		$set("type", type);
	}

	public String getType() {
		return (String) $get("type");
	}

	public static Message2 map(Object obj) {
		return (Message2) $.extend(false, new Message2(null), obj);
	}
}