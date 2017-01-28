package json_schema.states;

import static def.jquery.Globals.$;

import json_schema.types.Arc;
import json_schema.types.Position;

public class BotState extends jsweet.lang.Object {

	public Double getEnergyLevel() {
		return (Double) $get("energy-level");
	}

	public Position getPosition() {
		return (Position) $.extend(false, new Position(), $get("position"));
	}

	public Double getDirection() {
		return (Double) $get("direction");
	}

	public Double getGunDirection() {
		return (Double) $get("gun-direction");
	}

	public Double getRadarDirection() {
		return (Double) $get("radar-direction");
	}

	public Double getSpeed() {
		return (Double) $get("speed");
	}

	public Arc getScanArc() {
		return (Arc) $.extend(false, new Arc(), $get("scan-arc"));
	}
}
