package net.robocode2.model;

import lombok.ToString;

/**
 * Defines a 2D point
 * 
 * @author Flemming N. Larsen
 */
@ToString
public final class Point {

	/** X coordinate */
	public final double x;
	/** Y coordinate */
	public final double y;

	/**
	 * Creates a new point (x,y)
	 * 
	 * @param x
	 *            is the x coordinate
	 * @param y
	 *            is the y coordinate
	 */
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
}