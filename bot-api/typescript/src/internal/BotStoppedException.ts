/**
 * Thrown to stop the bot worker thread gracefully.
 * Replaces Java's ThreadInterruptedException in the TypeScript implementation.
 */
export class BotStoppedException extends Error {
  constructor() {
    super("Bot stopped");
    this.name = "BotStoppedException";
  }
}
