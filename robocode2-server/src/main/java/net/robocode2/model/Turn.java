package net.robocode2.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Turn {

	private final int turnNumber;
	private final Set<Bot> bots;
	private final Set<Bullet> bullets;

	public Turn(int turnNumber, Set<Bot> bots, Set<Bullet> bullets) {
		this.turnNumber = turnNumber;
		this.bots = new HashSet<Bot>(bots);
		this.bullets = new HashSet<Bullet>(bullets);
	}

	public int getTurnNumber() {
		return turnNumber;
	}

	public Set<Bot> getBots() {
		return Collections.unmodifiableSet(bots);
	}

	public Set<Bullet> getBullet() {
		return Collections.unmodifiableSet(bullets);
	}
}