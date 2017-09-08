package json_schema.controller.commands;

public class Command extends def.js.Object {

	public Command(String type) {
		$set("type", type);
	}

	public String getType() {
		return (String) $get("type");
	}
}