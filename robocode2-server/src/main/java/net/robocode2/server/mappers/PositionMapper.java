package net.robocode2.server.mappers;

import net.robocode2.json_schema.types.Position;

public final class PositionMapper {

	public static Position map(net.robocode2.model.Position position) {
		Position schemaPos = new Position();
		schemaPos.setX(position.getX());
		schemaPos.setY(position.getY());
		return schemaPos;
	}
}