package json_schema.controller.commands;

public class PauseGame extends Command {

	public static final String TYPE = "pauseGame";

	public PauseGame() {
		super(TYPE);
	}

	public PauseGame(String type) {
		super(type);
	}
}