package net.robocode2.model;

/**
 * Defines a 2D size
 * 
 * @author Flemming N. Larsen
 */
public final class Size {

	/** Width */
	public final double width;
	/** Height */
	public final double height;

	/**
	 * Creates a new size
	 * 
	 * @param width
	 *            is the width
	 * @param height
	 *            is the height
	 */
	public Size(double width, double height) {
		this.width = width;
		this.height = height;
	}
}