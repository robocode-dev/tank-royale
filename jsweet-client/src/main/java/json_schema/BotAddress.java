package json_schema;

public class BotAddress extends def.js.Object {

	public void setHostName(String hostname) {
		$set("host-name", hostname);
	}

	public void setPort(String port) {
		$set("port", port);
	}
}