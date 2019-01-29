package net.robocode2.mappers;

import net.robocode2.schema.types.Point;

public final class PointMapper {

	private PointMapper() {}

	public static Point map(net.robocode2.model.Point position) {
		Point schemaPos = new Point();
		schemaPos.setX(position.x);
		schemaPos.setY(position.y);
		return schemaPos;
	}
}