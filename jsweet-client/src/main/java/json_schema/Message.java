package json_schema;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jsweet.lang.JSON;

public class Message {

	private Map<String, String> map = new HashMap<>();

	public Message(String messageType) {
		setField("message-type", messageType);
	}

	public void setField(String fieldName, String value) {
		map.put(fieldName, value);
	}

	public String toJsonString() {
		return JSON.stringify(toJsObject(map));
	}

	protected static jsweet.lang.Object toJsObject(Map<String, String> map) {
		jsweet.lang.Object jsObject = new jsweet.lang.Object();

		// Put the keys and values from the map into the object
		for (Entry<String, String> keyVal : map.entrySet()) {
			jsObject.$set(keyVal.getKey(), keyVal.getValue());
		}
		return jsObject;
	}
}