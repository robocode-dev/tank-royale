import { IBaseBot } from "./IBaseBot.js";
import { Condition } from "./events/Condition.js";

/**
 * Interface for a bot that extends IBaseBot with blocking movement methods.
 *
 * This interface adds the ability to execute blocking movement commands that
 * wait until the movement is completed before returning.
 */
export interface IBot extends IBaseBot {
  /**
   * The main method for the bot. Override this to implement the bot's behavior.
   * This method is called when the bot starts running in a new round.
   */
  run(): void;

  /** Returns whether the bot is currently running. */
  isRunning(): boolean;

  /**
   * Sets the bot to move forward by the given distance (non-blocking).
   * @param distance the distance to move forward in units.
   */
  setForward(distance: number): void;

  /**
   * Sets the bot to move backward by the given distance (non-blocking).
   * @param distance the distance to move backward in units.
   */
  setBack(distance: number): void;

  /**
   * Moves the bot forward by the given distance (blocking).
   * Blocks until the movement is completed.
   * @param distance the distance to move forward in units.
   */
  forward(distance: number): void;

  /**
   * Moves the bot backward by the given distance (blocking).
   * Blocks until the movement is completed.
   * @param distance the distance to move backward in units.
   */
  back(distance: number): void;

  /** The distance remaining until the bot has finished moving. */
  getDistanceRemaining(): number;

  /**
   * Sets the bot to turn left by the given degrees (non-blocking).
   * @param degrees the degrees to turn left.
   */
  setTurnLeft(degrees: number): void;

  /**
   * Sets the bot to turn right by the given degrees (non-blocking).
   * @param degrees the degrees to turn right.
   */
  setTurnRight(degrees: number): void;

  /**
   * Turns the bot left by the given degrees (blocking).
   * Blocks until the turn is completed.
   * @param degrees the degrees to turn left.
   */
  turnLeft(degrees: number): void;

  /**
   * Turns the bot right by the given degrees (blocking).
   * Blocks until the turn is completed.
   * @param degrees the degrees to turn right.
   */
  turnRight(degrees: number): void;

  /** The turn remaining until the bot has finished turning. */
  getTurnRemaining(): number;

  /**
   * Sets the gun to turn left by the given degrees (non-blocking).
   * @param degrees the degrees to turn the gun left.
   */
  setTurnGunLeft(degrees: number): void;

  /**
   * Sets the gun to turn right by the given degrees (non-blocking).
   * @param degrees the degrees to turn the gun right.
   */
  setTurnGunRight(degrees: number): void;

  /**
   * Turns the gun left by the given degrees (blocking).
   * Blocks until the turn is completed.
   * @param degrees the degrees to turn the gun left.
   */
  turnGunLeft(degrees: number): void;

  /**
   * Turns the gun right by the given degrees (blocking).
   * Blocks until the turn is completed.
   * @param degrees the degrees to turn the gun right.
   */
  turnGunRight(degrees: number): void;

  /** The gun turn remaining until the gun has finished turning. */
  getGunTurnRemaining(): number;

  /**
   * Sets the radar to turn left by the given degrees (non-blocking).
   * @param degrees the degrees to turn the radar left.
   */
  setTurnRadarLeft(degrees: number): void;

  /**
   * Sets the radar to turn right by the given degrees (non-blocking).
   * @param degrees the degrees to turn the radar right.
   */
  setTurnRadarRight(degrees: number): void;

  /**
   * Turns the radar left by the given degrees (blocking).
   * Blocks until the turn is completed.
   * @param degrees the degrees to turn the radar left.
   */
  turnRadarLeft(degrees: number): void;

  /**
   * Turns the radar right by the given degrees (blocking).
   * Blocks until the turn is completed.
   * @param degrees the degrees to turn the radar right.
   */
  turnRadarRight(degrees: number): void;

  /** The radar turn remaining until the radar has finished turning. */
  getRadarTurnRemaining(): number;

  /**
   * Fires the gun with the given firepower (blocking).
   * Blocks until the next turn after firing.
   * @param firepower the firepower to fire with.
   */
  fire(firepower: number): void;

  /**
   * Stops all movement (blocking). Blocks until the next turn.
   */
  stop(): void;

  /**
   * Stops all movement (blocking), optionally overwriting a previous stop.
   * Blocks until the next turn.
   * @param overwrite whether to overwrite a previous stop.
   */
  stop(overwrite: boolean): void;

  /**
   * Resumes movement after a stop (blocking). Blocks until the next turn.
   */
  resume(): void;

  /**
   * Rescans with the radar (blocking). Blocks until the next turn.
   */
  rescan(): void;

  /**
   * Waits until the given condition is true, calling go() each turn.
   * @param condition the condition to wait for.
   */
  waitFor(condition: Condition): void;
}
