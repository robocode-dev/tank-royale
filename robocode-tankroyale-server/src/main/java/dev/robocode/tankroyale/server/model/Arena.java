package dev.robocode.tankroyale.server.model;

import lombok.NonNull;
import lombok.Value;

/**
 * Defines the arena, where bots battle each other.
 * 
 * @author Flemming N. Larsen
 */
@Value
public class Arena {

	/** Size of the arena */
	@NonNull Size size;
}