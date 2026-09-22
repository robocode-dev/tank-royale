import { describe, expect, test } from "vitest";
import { Constants } from "../src/Constants.js";
import { IntentValidator } from "../src/internal/intentValidator.js";
import { BotException } from "../src/BotException.js";

describe("Unit: team message limits", () => {
  test("accepts 64 messages and rejects the 65th", () => {
    expect(() => IntentValidator.validateTeamMessage("hello", 10)).not.toThrow();
    expect(() => IntentValidator.validateTeamMessage("hello", Constants.MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN - 1)).not.toThrow();
    expect(() => IntentValidator.validateTeamMessage("hello", Constants.MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN)).toThrow(BotException);
  });

  test("counts Unicode payload bytes", () => {
    expect(() => IntentValidator.validateTeamMessageSize("é".repeat(Constants.TEAM_MESSAGE_MAX_SIZE / 2))).not.toThrow();
    expect(() => IntentValidator.validateTeamMessageSize("é".repeat(Constants.TEAM_MESSAGE_MAX_SIZE / 2 + 1))).toThrow();
  });

  test("includes the aggregate boundary", () => {
    expect(() => IntentValidator.validateTeamMessagesSize("x".repeat(Constants.TEAM_MESSAGES_MAX_BYTES_PER_TURN))).not.toThrow();
    expect(() => IntentValidator.validateTeamMessagesSize("x".repeat(Constants.TEAM_MESSAGES_MAX_BYTES_PER_TURN + 1))).toThrow();
  });
});
