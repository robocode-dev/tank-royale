package json_schema.comm;

public class BotInfo extends BotHandshake {

	public static final String TYPE = "botInfo";

	public BotInfo() {
		super(TYPE);
	}

	public BotInfo(String type) {
		super(type);
	}

	public String getHostName() {
		return (String) $get("host");
	}

	public Integer getPort() {
		return (Integer) $get("port");
	}
}