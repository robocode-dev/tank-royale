package json_schema.control;

public class PauseGame extends Command {

	public static final String TYPE = "pauseGame";

	public PauseGame() {
		super(TYPE);
	}

	public PauseGame(String type) {
		super(type);
	}
}