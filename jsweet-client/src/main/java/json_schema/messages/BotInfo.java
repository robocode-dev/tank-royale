package json_schema.messages;

public class BotInfo extends BotHandshake {

	public static final String TYPE = "bot-info";

	public BotInfo() {
		super(TYPE);
	}

	public BotInfo(String type) {
		super(type);
	}

	public String getHostName() {
		return (String) $get("host-name");
	}

	public Integer getPort() {
		return (Integer) $get("port");
	}
}