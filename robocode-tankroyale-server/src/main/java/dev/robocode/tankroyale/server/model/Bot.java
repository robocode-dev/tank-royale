package dev.robocode.tankroyale.server.model;

import lombok.Builder;
import lombok.Builder.Default;
import dev.robocode.tankroyale.server.util.MathUtil;
import lombok.Value;

/**
 * Mutable bot instance.
 * 
 * @author Flemming N. Larsen
 */
@Value
@Builder(toBuilder=true)
public class Bot {

	/** Bot id */
	int id;

	/** Energy level */
	@Default double energy = 100;

	/** X coordinate on the arena */
	double x;

	/** Y coordinate on the arena */
	double y;

	/** Driving direction in degrees */
	double direction;

	/** Gun direction in degrees */
	double gunDirection;

	/** Radar direction in degrees */
	double radarDirection;

	/** Radar spread angle in degrees */
	double radarSpreadAngle;

	/** Speed */
	double speed;

	/** Gun heat */
	double gunHeat;

	/** Score record */
	Score score;


	@SuppressWarnings("WeakerAccess")
	public static class BotBuilder {

		public int getId() {
			return id;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}

		public double getEnergy() {
			return energy;
		}

		public double getSpeed() {
			return speed;
		}

		public boolean isAlive() {
			return energy >= 0;
		}
		
		public boolean isDead() {
			return energy < 0;
		}

		public boolean isDisabled() {
			return isAlive() && MathUtil.nearlyEqual(getEnergy(), 0);
		}

		public boolean isEnabled() {
			return !isDisabled();
		}

		public double getDirection() {
			return direction;
		}

		public double getGunDirection() {
			return gunDirection;
		}

		public double getRadarDirection() {
			return radarDirection;
		}

		public double getRadarSpreadAngle() {
			return radarSpreadAngle;
		}
		
		public double getGunHeat() {
			return gunHeat;
		}
		
		/**
		 * Adds damage to the bot.
		 * 
		 * @param damage
		 *            is the damage done to this bot
		 * @return true if the bot got killed due to the damage, false otherwise.
		 */
		public boolean addDamage(double damage) {
			boolean aliveBefore = isAlive();
			energy -= damage;
			return isDead() && aliveBefore;
		}
	
		/**
		 * Change the energy level.
		 * 
		 * @param deltaEnergy
		 *            is the delta energy to add to the current energy level, which can be both positive and negative.
		 */
		public void changeEnergy(double deltaEnergy) {
			energy += deltaEnergy;
		}
	
		/**
		 * Move bot to new position of the bot based on the current position, the driving direction and speed.
		 */
		public void moveToNewPosition() {
			Point p = calcNewPosition(direction, speed);
			x = p.x;
			y = p.y;
		}
	
		/**
		 * Moves bot backwards a specific amount of distance due to bouncing.
		 * 
		 * @param distance
		 *            is the distance to bounce back
		 */
		public void bounceBack(double distance) {
			Point p = calcNewPosition(direction, (speed > 0 ? -distance : distance));
			x = p.x;
			y = p.y;
		}
	
		/**
		 * Calculates new position of the bot based on the current position, the driving direction and speed.
		 * 
		 * @param direction
		 *            is the new driving direction
		 * @param distance
		 *            is the distance to move
		 * @return the calculated new position of the bot
		 */
		private Point calcNewPosition(double direction, double distance) {
			double angle = Math.toRadians(direction);
			double x = this.x + Math.cos(angle) * distance;
			double y = this.y + Math.sin(angle) * distance;
			return new Point(x, y);
		}
	}
}