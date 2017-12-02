package json_schema.states;

import static def.jquery.Globals.$;

import json_schema.types.Point;

public class BotState extends def.js.Object {

	public Double getEnergyLevel() {
		return (Double) $get("energy");
	}

	public Point getPosition() {
		return (Point) $.extend(false, new Point(), $get("position"));
	}

	public Double getDirection() {
		return (Double) $get("direction");
	}

	public Double getGunDirection() {
		return (Double) $get("gunDirection");
	}

	public Double getRadarDirection() {
		return (Double) $get("radarDirection");
	}

	public Double getRadarSpreadAngle() {
		return (Double) $get("radarSpreadAngle");
	}

	public Double getSpeed() {
		return (Double) $get("speed");
	}
}
