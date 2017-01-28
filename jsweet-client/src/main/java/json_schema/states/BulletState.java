package json_schema.states;

import static def.jquery.Globals.$;

import json_schema.types.Position;

public class BulletState extends jsweet.lang.Object {

	public Integer getBulletId() {
		return (Integer) $get("bullet-id");
	}

	public Integer getBotId() {
		return (Integer) $get("bot-id");
	}

	public Double getPower() {
		return (Double) $get("power");
	}

	public Position getPosition() {
		return (Position) $.extend(false, new Position(), $get("position"));
	}

	public Double getDirection() {
		return (Double) $get("direction");
	}
}