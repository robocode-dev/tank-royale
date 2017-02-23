package json_schema.events;

import static def.jquery.Globals.$;

import json_schema.states.BulletState;

public class BulletHitBotEvent extends Event {

	public static final String TYPE = "bullet-hit-bot-event";

	public BulletHitBotEvent() {
		super(TYPE);
	}

	public BulletState getBullet() {
		return (BulletState) $.extend(false, new BulletState(), $get("bullet"));
	}

	public Integer getVictimId() {
		return (Integer) $get("victim-id");
	}

	public Double getDamage() {
		return (Double) $get("damage");
	}

	public Double getEnergy() {
		return (Double) $get("energy");
	}
}