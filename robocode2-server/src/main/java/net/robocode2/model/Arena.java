package net.robocode2.model;

/**
 * Defines the arena, where bots battle each other.
 * 
 * @author Flemming N. Larsen
 */
public final class Arena {

	/** The size of the arena */
	private final Size size;

	/**
	 * Creates a new arena.
	 * 
	 * @param size
	 *            is the size of the arena.
	 */
	public Arena(Size size) {
		this.size = size;
	}

	/**
	 * Return the size of the arena.
	 * 
	 * @return the size
	 */
	public Size getSize() {
		return size;
	}
}