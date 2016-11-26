package net.robocode2.model.events;

import net.robocode2.model.Bullet;
import net.robocode2.model.Position;

public final class HitBotEvent implements Event {

	private final Bullet bullet;
	private final double energy;
	private final Position position;
	private final boolean rammed;

	public HitBotEvent(Bullet bullet, double energy, Position position, boolean rammed) {
		this.bullet = bullet;
		this.energy = energy;
		this.position = position;
		this.rammed = rammed;
	}

	public Bullet getBullet() {
		return bullet;
	}

	public double getEnergy() {
		return energy;
	}

	public Position getPosition() {
		return position;
	}

	public boolean getRammed() {
		return rammed;
	}
}