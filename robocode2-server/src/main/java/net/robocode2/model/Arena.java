package net.robocode2.model;

import lombok.Value;

/**
 * Defines the arena, where bots battle each other.
 * 
 * @author Flemming N. Larsen
 */
@Value
public final class Arena {

	/** The size of the arena */
	Size size;
}