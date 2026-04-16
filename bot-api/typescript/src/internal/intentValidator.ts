import { MathUtil } from "../util/MathUtil.js";
import { Color } from "../graphics/Color.js";
import { ColorUtil } from "../util/ColorUtil.js";
import { Constants } from "../Constants.js";

export class IntentValidator {
  static validateFirepower(firepower: number): number {
    if (isNaN(firepower)) throw new Error("'firepower' cannot be NaN");
    return firepower;
  }

  static validateTurnRate(turnRate: number, maxTurnRate: number): number {
    if (isNaN(turnRate)) throw new Error("'turnRate' cannot be NaN");
    return MathUtil.clamp(turnRate, -maxTurnRate, maxTurnRate);
  }

  static validateGunTurnRate(gunTurnRate: number, maxGunTurnRate: number): number {
    if (isNaN(gunTurnRate)) throw new Error("'gunTurnRate' cannot be NaN");
    return MathUtil.clamp(gunTurnRate, -maxGunTurnRate, maxGunTurnRate);
  }

  static validateRadarTurnRate(radarTurnRate: number, maxRadarTurnRate: number): number {
    if (isNaN(radarTurnRate)) throw new Error("'radarTurnRate' cannot be NaN");
    return MathUtil.clamp(radarTurnRate, -maxRadarTurnRate, maxRadarTurnRate);
  }

  static validateTargetSpeed(targetSpeed: number, maxSpeed: number): number {
    if (isNaN(targetSpeed)) throw new Error("'targetSpeed' cannot be NaN");
    return MathUtil.clamp(targetSpeed, -maxSpeed, maxSpeed);
  }

  static validateMaxSpeed(maxSpeed: number): number {
    return MathUtil.clamp(maxSpeed, 0, Constants.MAX_SPEED);
  }

  static validateMaxTurnRate(maxTurnRate: number): number {
    return MathUtil.clamp(maxTurnRate, 0, Constants.MAX_TURN_RATE);
  }

  static validateMaxGunTurnRate(maxGunTurnRate: number): number {
    return MathUtil.clamp(maxGunTurnRate, 0, Constants.MAX_GUN_TURN_RATE);
  }

  static validateMaxRadarTurnRate(maxRadarTurnRate: number): number {
    return MathUtil.clamp(maxRadarTurnRate, 0, Constants.MAX_RADAR_TURN_RATE);
  }

  static getNewTargetSpeed(speed: number, distance: number, maxSpeed: number): number {
    if (distance < 0) return -this.getNewTargetSpeed(-speed, -distance, maxSpeed);
    const targetSpeed = !isFinite(distance) ? maxSpeed : Math.min(maxSpeed, this.getMaxSpeedForDistance(distance));

    const absDecel = Math.abs(Constants.DECELERATION);
    if (speed >= 0) {
      return MathUtil.clamp(targetSpeed, speed - absDecel, speed + Constants.ACCELERATION);
    } else {
      return MathUtil.clamp(targetSpeed, speed - Constants.ACCELERATION, speed + this.getMaxDeceleration(-speed));
    }
  }

  private static getMaxSpeedForDistance(distance: number): number {
    const absDecel = Math.abs(Constants.DECELERATION);
    const decelerationTime = Math.max(1, Math.ceil((Math.sqrt((4 * 2 / absDecel) * distance + 1) - 1) / 2));
    if (!isFinite(decelerationTime)) return Constants.MAX_SPEED;
    const decelerationDistance = (decelerationTime / 2) * (decelerationTime - 1) * absDecel;
    return (decelerationTime - 1) * absDecel + (distance - decelerationDistance) / decelerationTime;
  }

  private static getMaxDeceleration(speed: number): number {
    const absDecel = Math.abs(Constants.DECELERATION);
    const decelerationTime = speed / absDecel;
    const accelerationTime = 1 - decelerationTime;
    return Math.min(1, decelerationTime) * absDecel + Math.max(0, accelerationTime) * Constants.ACCELERATION;
  }

  static getDistanceTraveledUntilStop(speed: number, maxSpeed: number): number {
    speed = Math.abs(speed);
    let distance = 0;
    while (speed > 0) {
      distance += (speed = this.getNewTargetSpeed(speed, 0, maxSpeed));
    }
    return distance;
  }

  static colorToHex(color: Color | null): string | null {
    return color ? "#" + ColorUtil.toHex(color) : null;
  }

  static validateTeammateId(teammateId: number | undefined, teammateIds: ReadonlySet<number>): void {
    if (teammateId !== undefined && !teammateIds.has(teammateId)) {
      throw new Error("No teammate was found with the specified 'teammateId': " + teammateId);
    }
  }

  static validateTeamMessage(message: unknown, currentTeamMessageCount: number): void {
    if (currentTeamMessageCount >= 10) { // MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN
      throw new Error("The maximum number team messages has already been reached: 10");
    }
    if (message === null || message === undefined) {
      throw new Error("The 'message' of a team message cannot be null");
    }
  }

  static validateTeamMessageSize(json: string): void {
    if (json.length > 32768) { // TEAM_MESSAGE_MAX_SIZE
      throw new Error("The team message is larger than the limit of 32768 bytes (compact JSON format)");
    }
  }
}
