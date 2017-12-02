package json_schema.states;

import static def.jquery.Globals.$;

import json_schema.types.Point;

public class BulletState extends def.js.Object {

	public Integer getBulletId() {
		return (Integer) $get("bulletId");
	}

	public Integer getBotId() {
		return (Integer) $get("ownerid");
	}

	public Double getPower() {
		return (Double) $get("power");
	}

	public Point getPosition() {
		return (Point) $.extend(false, new Point(), $get("position"));
	}

	public Double getDirection() {
		return (Double) $get("direction");
	}
}