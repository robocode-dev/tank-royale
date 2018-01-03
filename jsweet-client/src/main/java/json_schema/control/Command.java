package json_schema.control;

public class Command extends def.js.Object {

	public Command(String type) {
		$set("type", type);
	}

	public String getType() {
		return (String) $get("type");
	}
}