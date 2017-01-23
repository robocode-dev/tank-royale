package json_schema;

import jsweet.lang.Array;

public class NewBattleForObserver extends Message {

	public static final String MESSAGE_TYPE = "new-battle-for-observer";

	public NewBattleForObserver() {
		super(MESSAGE_TYPE);
	}

	public GameSetup getGameSetup() {
		return (GameSetup) $get("game-setup");
	}

	@SuppressWarnings("unchecked")
	public Array<Participant> getParticipants() {
		return (Array<Participant>) $get("participants");
	}
}
