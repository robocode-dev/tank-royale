package json_schema.messages;

import static def.jquery.Globals.$;

import java.util.HashSet;
import java.util.Set;

import json_schema.events.Event;
import json_schema.states.BotStateWithId;
import json_schema.states.BulletState;
import json_schema.states.RoundState;
import jsweet.lang.Array;

public class TickForObserver extends Message {

	public static final String TYPE = "tick-for-observer";

	public TickForObserver() {
		super(TYPE);
	}

	public TickForObserver(String type) {
		super(type);
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
		Array<BulletState> array = (Array<BulletState>) $.extend(true, new Array<BulletState>(), $get("bullet-states"));

		Set<BulletState> set = new HashSet<>();
		for (BulletState obj : array) {
			BulletState bulletState = (BulletState) $.extend(false, new BulletState(), obj);
			set.add(bulletState);
		}
		return set;
	}

	public Set<Event> getEvents() {
		@SuppressWarnings("unchecked")
		Array<Event> array = (Array<Event>) $.extend(true, new Array<Event>(), $get("events"));

		Set<Event> set = new HashSet<>();
		for (Event obj : array) {
			Event event = (Event) $.extend(false, new Event(null), obj);
			set.add(event);
		}
		return set;
	}

	public static TickForObserver map(Object obj) {
		return (TickForObserver) $.extend(false, new TickForObserver(), obj);
	}
}