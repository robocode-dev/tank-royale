package net.robocode2.server.mappers;

import net.robocode2.json_schema.types.Arc;
import net.robocode2.json_schema.types.Position;

public final class TypesMapper {

	public static Arc map(net.robocode2.model.Arc arc) {
		Arc schemaArc = new Arc();
		schemaArc.setAngle(arc.getAngle());
		schemaArc.setAngle(arc.getRadius());
		return schemaArc;
	}

	public static Position map(net.robocode2.model.Position position) {
		Position schemaPos = new Position();
		schemaPos.setX(position.getX());
		schemaPos.setY(position.getY());
		return schemaPos;
	}
}