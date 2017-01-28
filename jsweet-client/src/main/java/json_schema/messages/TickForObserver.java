package json_schema.messages;

import static def.jquery.Globals.$;

import java.util.HashSet;
import java.util.Set;

import json_schema.states.BotStateWithId;
import json_schema.states.BulletState;
import json_schema.states.RoundState;
import jsweet.lang.Array;

public class TickForObserver extends Message {

	public static final String MESSAGE_TYPE = "tick-for-observer";

	public TickForObserver(String messageType) {
		super(messageType);
	}

	public RoundState getRoundState() {
		return (RoundState) $.extend(false, new RoundState(), $get("round-state"));
	}

	public Set<BotStateWithId> getBotStates() {
		@SuppressWarnings("unchecked")
		Array<BotStateWithId> array = (Array<BotStateWithId>) $.extend(true, new Array<BotStateWithId>(),
				$get("bot-states"));

		Set<BotStateWithId> set = new HashSet<>();
		for (BotStateWithId obj : array) {
			BotStateWithId botState = (BotStateWithId) $.extend(false, new BotStateWithId(), obj);
			set.add(botState);
		}
		return set;
	}

	public Set<BulletState> getBulletStates() {
		@SuppressWarnings("unchecked")
		Array<BulletState> array = (Array<BulletState>) $.extend(true, new Array<BulletState>(), $get("bot-states"));

		Set<BulletState> set = new HashSet<>();
		for (BulletState obj : array) {
			BulletState botState = (BulletState) $.extend(false, new BulletState(), obj);
			set.add(botState);
		}
		return set;
	}

	// TODO: Implement events
}