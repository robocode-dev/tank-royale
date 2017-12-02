package json_schema.events;

import static def.jquery.Globals.$;

import json_schema.states.BulletState;

public class BulletMissedEvent extends Event {

	public static final String TYPE = "bulletMissedEvent";

	public BulletMissedEvent() {
		super(TYPE);
	}

	public BulletState getBullet() {
		return (BulletState) $.extend(false, new BulletState(), $get("bullet"));
	}
}