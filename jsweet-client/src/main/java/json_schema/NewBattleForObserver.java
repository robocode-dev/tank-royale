package json_schema;

import static def.jquery.Globals.$;

import java.util.HashSet;
import java.util.Set;

import jsweet.lang.Array;

public class NewBattleForObserver extends Message {

	public static final String MESSAGE_TYPE = "new-battle-for-observer";

	public NewBattleForObserver() {
		super(MESSAGE_TYPE);
	}

	public GameSetup getGameSetup() {
		return (GameSetup) $.extend(false, new GameSetup(), $get("game-setup"));
	}

	public Set<Participant> getParticipants() {
		@SuppressWarnings("unchecked")
		Array<Participant> array = (Array<Participant>) $.extend(true, new Array<Participant>(), $get("participants"));

		Set<Participant> set = new HashSet<>();
		for (Participant obj : array) {
			Participant participant = (Participant) $.extend(false, new Participant(), obj);
			set.add(participant);
		}
		return set;
	}

	public static NewBattleForObserver map(Object obj) {
		return (NewBattleForObserver) $.extend(false, new NewBattleForObserver(), obj);
	}
}