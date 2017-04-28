package json_schema.controller.commands;

public class Command extends jsweet.lang.Object {

	public Command(String type) {
		$set("type", type);
	}

	public String getType() {
		return (String) $get("type");
	}
}