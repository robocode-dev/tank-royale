import { describe, it, expect } from "vitest";
import { GameType } from "../src/GameType.js";

describe("GameType", () => {
  it("CLASSIC is 'classic'", () => {
    expect(GameType.CLASSIC).toBe("classic");
  });

  it("MELEE is 'melee'", () => {
    expect(GameType.MELEE).toBe("melee");
  });

  it("ONE_VS_ONE is '1v1'", () => {
    expect(GameType.ONE_VS_ONE).toBe("1v1");
  });
});
