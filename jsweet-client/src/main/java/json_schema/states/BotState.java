package json_schema.states;

import static def.jquery.Globals.$;

import json_schema.types.Point;
import json_schema.types.ScanField;

public class BotState extends jsweet.lang.Object {

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
		return (Double) $get("gun-direction");
	}

	public Double getRadarDirection() {
		return (Double) $get("radar-direction");
	}

	public Double getSpeed() {
		return (Double) $get("speed");
	}

	public ScanField getScanField() {
		return (ScanField) $.extend(false, new ScanField(), $get("scan-field"));
	}
}
