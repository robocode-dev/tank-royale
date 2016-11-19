package net.robocode2.server.mappers;

import net.robocode2.model.Arc;
import net.robocode2.model.Position;

public final class TypesMapper {

	public static net.robocode2.json_schema.types.Arc map(Arc arc) {
		net.robocode2.json_schema.types.Arc schemaArc = new net.robocode2.json_schema.types.Arc();
		schemaArc.setAngle(arc.getAngle());
		schemaArc.setAngle(arc.getRadius());
		return schemaArc;
	}

	public static net.robocode2.json_schema.types.Position map(Position position) {
		net.robocode2.json_schema.types.Position schemaPos = new net.robocode2.json_schema.types.Position();
		schemaPos.setX(position.getX());
		schemaPos.setY(position.getY());
		return schemaPos;
	}
}