/** Immutable ordered payloads delivered as one team-message event on the next turn. */
export class TeamMessageBatch {
  readonly messages: readonly unknown[];

  constructor(messages: readonly unknown[]) {
    if (!Array.isArray(messages)) throw new Error("A team message batch must contain at least one message");
    if (messages.length === 0) throw new Error("A team message batch must contain at least one message");
    if (messages.some((message) => message === null || message === undefined)) {
      throw new Error("A team message batch cannot contain null messages");
    }
    this.messages = Object.freeze([...messages]);
  }
}
