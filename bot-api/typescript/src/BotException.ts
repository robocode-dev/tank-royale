/**
 * Represents errors that occur with bot execution.
 */
export class BotException extends Error {
  constructor(message: string, cause?: Error) {
    super(message);
    this.name = "BotException";
    if (cause !== undefined) {
      this.cause = cause;
    }
  }
}
