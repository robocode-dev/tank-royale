package net.robocode2.server.mappers;

import net.robocode2.game.MathUtil;
import net.robocode2.json_schema.types.Arc;

public final class ArcMapper {

	public static Arc map(net.robocode2.model.Arc arc) {
		if (arc == null) {
			return null;
		}
		Arc schemaArc = new Arc();
		schemaArc.setAngle(MathUtil.normalAbsoluteAngleDegrees(arc.getAngle()));
		schemaArc.setRadius(arc.getRadius());
		return schemaArc;
	}
}