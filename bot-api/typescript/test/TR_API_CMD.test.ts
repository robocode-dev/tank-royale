import { describe, it, expect } from "vitest";
import { BaseBotInternals } from "../src/internal/BaseBotInternals.js";
import { BotInfo } from "../src/BotInfo.js";

describe("TR-API-CMD-003: Radar commands (rescan, adjust)", () => {
  const info = new BotInfo("TestBot", "1.0", ["Author"], null, null, null, ["classic"], null, null);

  it("Positive: setAdjustGunForBodyTurn persists in intent", () => {
    const stub = {} as any;
    const internals = new BaseBotInternals(stub, info, null, undefined);
    
    internals.setAdjustGunForBodyTurn(true);
    expect(internals.isAdjustGunForBodyTurn()).toBe(true);
    expect((internals as any).intent.adjustGunForBodyTurn).toBe(true);
    
    internals.setAdjustGunForBodyTurn(false);
    expect(internals.isAdjustGunForBodyTurn()).toBe(false);
    expect((internals as any).intent.adjustGunForBodyTurn).toBe(false);
  });

  it("Positive: setAdjustRadarForGunTurn persists in intent", () => {
    const stub = {} as any;
    const internals = new BaseBotInternals(stub, info, null, undefined);
    
    internals.setAdjustRadarForGunTurn(true);
    expect(internals.isAdjustRadarForGunTurn()).toBe(true);
    expect((internals as any).intent.adjustRadarForGunTurn).toBe(true);
  });

  it("Positive: setAdjustRadarForBodyTurn persists in intent", () => {
    const stub = {} as any;
    const internals = new BaseBotInternals(stub, info, null, undefined);
    
    internals.setAdjustRadarForBodyTurn(true);
    expect(internals.isAdjustRadarForBodyTurn()).toBe(true);
  });

  it("Positive: setRescan sets the rescan flag", () => {
    const stub = {} as any;
    const internals = new BaseBotInternals(stub, info, null, undefined);
    
    internals.setRescan();
    expect((internals as any).intent.rescan).toBe(true);
  });
});
