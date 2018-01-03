package json_schema.control;

public class ResumeGame extends Command {

	public static final String TYPE = "resumeGame";

	public ResumeGame() {
		super(TYPE);
	}

	public ResumeGame(String type) {
		super(type);
	}
}