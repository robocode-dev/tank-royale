/**
 * Constants.
 */
export const Constants = {
  /**
   * The radius of the bounding circle of the bot, which is a constant of 18 units.
   *
   * The bounding circle of a bot is a circle going from the center of the bot with a radius so
   * that the circle covers most of the bot. The bounding circle is used for determining when a bot
   * is hit by a bullet.
   *
   * A bot gets hit by a bullet when the bullet gets inside the bounding circle, i.e. the
   * distance between the bullet and the center of the bounding circle is less than the radius of
   * the bounding circle.
   */
  BOUNDING_CIRCLE_RADIUS: 18,

  /**
   * The radius of the radar's scan beam, which is a constant of 1200 units.
   *
   * The radar is used for scanning the battlefield for opponent bots. The shape of the scan beam
   * of the radar is a circle arc ("pizza slice") starting from the center of the bot. Opponent bots
   * that get inside the scan arc will be detected by the radar.
   *
   * The radius of the arc is a constant of 1200 units. This means that the radar will not be able
   * to detect bots that are more than 1200 units away from the bot.
   *
   * The radar needs to be turned (left or right) to scan opponent bots. So make sure the radar
   * is always turned. The more the radar is turned, the larger the area of the scan arc becomes,
   * and the bigger the chance is that the radar detects an opponent. If the radar is not turning,
   * the scan arc becomes a thin line, unable to scan and detect anything.
   */
  SCAN_RADIUS: 1200,

  /**
   * The maximum possible driving turn rate, which is max. 10 degrees per turn.
   *
   * This is the max. possible turn rate of the bot. Note that the speed of the bot has a direct
   * impact on the turn rate. The faster the speed the less turn rate.
   *
   * The formula for the max. possible turn rate at a given speed is: MaxTurnRate - 0.75 x
   * abs(speed). Hence, the turn rate is at max. 10 degrees/turn when the speed is zero, and down to
   * only 4 degrees per turn when the bot is at max speed (which is 8 units per turn).
   */
  MAX_TURN_RATE: 10,

  /**
   * The maximum gun turn rate, which is a constant of 20 degrees per turn.
   */
  MAX_GUN_TURN_RATE: 20,

  /**
   * The maximum radar turn rate, which is a constant of 45 degrees per turn.
   */
  MAX_RADAR_TURN_RATE: 45,

  /**
   * The maximum absolute speed, which is 8 units per turn.
   */
  MAX_SPEED: 8,

  /**
   * The minimum firepower, which is 0.1. The gun will not fire with a power that is less than the
   * minimum firepower, which is 0.1.
   */
  MIN_FIREPOWER: 0.1,

  /**
   * The maximum firepower, which is 3. The gun will fire up to this power, even if the firepower is
   * set to a higher value.
   */
  MAX_FIREPOWER: 3,

  /**
   * The minimum bullet speed is 11 units per turn.
   *
   * The minimum bullet speed is the slowest possible speed that a bullet can travel and is
   * defined by the maximum firepower: 20 - 3 x max. firepower, i.e. 20 - 3 x 3 = 11. The more
   * power, the slower the bullet speed will be.
   */
  MIN_BULLET_SPEED: 20 - 3 * 3,

  /**
   * The maximum bullet speed is 19.7 units per turn.
   *
   * The maximum bullet speed is the fastest possible speed that a bullet can travel and is
   * defined by the minimum firepower. Max. bullet speed = 20 - 3 x min. firepower, i.e. 20 - 3 x
   * 0.1 = 19.7. The lesser power, the faster the bullet speed will be.
   */
  MAX_BULLET_SPEED: 20 - 3 * 0.1,

  /**
   * Acceleration is the increase in speed per turn, which adds 1 unit to the speed per turn when
   * the bot is increasing its speed moving forward.
   */
  ACCELERATION: 1,

  /**
   * Deceleration is the decrease in speed per turn, which subtracts 2 units to the speed per turn
   * when the bot is decreasing its speed moving backward. This means that a bot is faster at
   * braking than accelerating forward.
   */
  DECELERATION: -2,

  /**
   * The amount of damage a bot receives per turn when the game's inactivity time limit is
   * exceeded. A bot that has not fired or been hit by a bullet for `maxInactivityTurns`
   * consecutive turns will lose this much energy every turn until it acts again.
   */
  INACTIVITY_ZAP: 0.1,

  /**
   * The amount of damage dealt to each bot involved in a collision when two bots ram into each
   * other, which is 0.6 energy points per collision.
   */
  RAM_DAMAGE: 0.6,

  /**
   * The gun heat at the start of a round, which is 3.0. The gun cannot fire until its heat drops
   * to zero, cooling at the rate defined by the game setup's gun cooling rate.
   */
  STARTING_GUN_HEAT: 3.0,

  /**
   * The maximum size of a team message in bytes (32 KB), which is the serialized (compact JSON)
   * size of the message object. Messages exceeding this size are rejected.
   */
  TEAM_MESSAGE_MAX_SIZE: 32768,

  /**
   * The maximum number of team messages that can be sent per turn, which is 10 messages.
   */
  MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN: 10,
} as const;
