import { describe, expect, it } from "vitest";
import { TeamMessageBatch } from "../src/TeamMessageBatch.js";
import { Constants } from "../src/Constants.js";
import { IntentValidator } from "../src/internal/intentValidator.js";
import { BotException } from "../src/BotException.js";

describe("TR-API-TCK-022: TeamMessageBatch", () => {
  it("keeps order and exposes the shared logical item limit", () => {
    expect(new TeamMessageBatch(["first", "second"]).messages).toEqual(["first", "second"]);
    expect(Constants.MAX_LOGICAL_TEAM_MESSAGES_PER_TURN).toBe(128);
    expect(() => IntentValidator.validateLogicalTeamMessageCount(128)).not.toThrow();
    expect(() => IntentValidator.validateLogicalTeamMessageCount(129)).toThrow(BotException);
  });

  it("requires at least one non-null payload", () => {
    expect(() => new TeamMessageBatch([])).toThrow("at least one message");
    expect(() => new TeamMessageBatch(["ok", null])).toThrow("cannot contain null");
  });
});
