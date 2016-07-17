package net.robocode2.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class Arena {

	private final int width;
	private final int height;

	private final Set<Bot> bots;
	private final Set<Bullet> bullets;

	public Arena(int width, int height, Set<Bot> bots, Set<Bullet> bullets) {
		this.width = width;
		this.height = height;
		this.bots = new HashSet<Bot>(bots);
		this.bullets = new HashSet<Bullet>(bullets);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Set<Bot> getBots() {
		return Collections.unmodifiableSet(bots);
	}

	public Set<Bullet> getBullet() {
		return Collections.unmodifiableSet(bullets);
	}
}