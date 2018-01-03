package json_schema.comm;

import static def.jquery.Globals.$;

import java.util.HashSet;
import java.util.Set;

import def.js.Array;
import json_schema.events.BotStateWithId;
import json_schema.events.BulletState;
import json_schema.events.Event;
import json_schema.events.RoundState;

public class TickEventForObserver extends Message {

	public static final String TYPE = "tickEventForObserver";

	public TickEventForObserver() {
		super(TYPE);
	}

	public TickEventForObserver(String type) {
		super(type);
	}

	public RoundState getRoundState() {
		return (RoundState) $.extend(false, new RoundState(), $get("roundState"));
	}

	public Set<BotStateWithId> getBotStates() {

		@SuppressWarnings("unchecked")
		Array<BotStateWithId> array = (Array<BotStateWithId>) $get("botStates");

		Set<BotStateWithId> set = new HashSet<>();
		for (BotStateWithId obj : array) {
			BotStateWithId botState = (BotStateWithId) $.extend(false, new BotStateWithId(), obj);
			set.add(botState);
		}
		return set;
	}

	public Set<BulletState> getBulletStates() {

		@SuppressWarnings("unchecked")
		Array<BulletState> array = (Array<BulletState>) $get("bulletStates");

		Set<BulletState> set = new HashSet<>();
		for (BulletState obj : array) {
			BulletState bulletState = (BulletState) $.extend(false, new BulletState(), obj);
			set.add(bulletState);
		}
		return set;
	}

	public Set<Event> getEvents() {
		@SuppressWarnings("unchecked")
		Array<Event> array = (Array<Event>) $get("events");

		Set<Event> set = new HashSet<>();
		for (Event obj : array) {
			Event event = (Event) $.extend(false, new Event(null), obj);
			set.add(event);
		}
		return set;
	}

	public static TickEventForObserver map(Object obj) {
		return (TickEventForObserver) $.extend(false, new TickEventForObserver(), obj);
	}
}