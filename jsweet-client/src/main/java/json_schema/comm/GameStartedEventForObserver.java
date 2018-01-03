package json_schema.comm;

import static def.jquery.Globals.$;

import java.util.HashSet;
import java.util.Set;

import def.js.Array;
import json_schema.GameSetup;
import json_schema.Participant;

public class GameStartedEventForObserver extends Message {

	public static final String TYPE = "gameStartedEventForObserver";

	public GameStartedEventForObserver() {
		super(TYPE);
	}

	public GameStartedEventForObserver(String type) {
		super(type);
	}

	public GameSetup getGameSetup() {
		return (GameSetup) $.extend(false, new GameSetup(), $get("gameSetup"));
	}

	public Set<Participant> getParticipants() {
		@SuppressWarnings("unchecked")
		Array<Participant> array = (Array<Participant>) $get("participants");

		Set<Participant> set = new HashSet<>();
		for (Participant obj : array) {
			Participant participant = (Participant) $.extend(false, new Participant(), obj);
			set.add(participant);
		}
		return set;
	}

	public static GameStartedEventForObserver map(Object obj) {
		return (GameStartedEventForObserver) $.extend(false, new GameStartedEventForObserver(), obj);
	}
}