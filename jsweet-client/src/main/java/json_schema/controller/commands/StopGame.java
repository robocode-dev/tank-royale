package json_schema.controller.commands;

public class StopGame extends Command {

	public static final String TYPE = "stopGame";

	public StopGame() {
		super(TYPE);
	}

	public StopGame(String type) {
		super(type);
	}
}