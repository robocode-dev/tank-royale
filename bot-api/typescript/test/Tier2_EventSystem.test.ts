import { describe, it, expect, vi } from "vitest";
import { EventQueue } from "../src/events/EventQueue.js";
import { BotEventHandlers } from "../src/events/BotEventHandlers.js";
import { EventPriorities } from "../src/events/EventPriorities.js";
import { EventInterruption } from "../src/events/EventInterruption.js";
import * as Events from "../src/events/index.js";
import { TickEvent } from "../src/events/TickEvent.js";
import { BulletState } from "../src/BulletState.js";
import { Condition } from "../src/events/Condition.js";
import { BotState } from "../src/BotState.js";

describe("EventSystem Tier 2", () => {
  const handlers = new BotEventHandlers();
  const priorities = new EventPriorities();
  const interruption = new EventInterruption();

  it("TR-API-EVT-001: Event constructors store fields correctly", () => {
    // TickEvent
    const te = new TickEvent(1, 2, null as any, [], []);
    expect(te.turnNumber).toBe(1);
    expect(te.roundNumber).toBe(2);

    // ScannedBotEvent
    const sbe = new Events.ScannedBotEvent(3, 1, 2, 80, 100, 200, 45, 5);
    expect(sbe.turnNumber).toBe(3);
    expect(sbe.scannedByBotId).toBe(1);
    expect(sbe.scannedBotId).toBe(2);
    expect(sbe.energy).toBe(80);
    expect(sbe.x).toBe(100);
    expect(sbe.y).toBe(200);
    expect(sbe.direction).toBe(45);
    expect(sbe.speed).toBe(5);

    // HitBotEvent
    const hbe = new Events.HitBotEvent(4, 5, 90, 10, 20, true);
    console.log('hbe:', hbe);
    expect(hbe.turnNumber).toBe(4);
    expect(hbe.victimId).toBe(5);
    expect(hbe.energy).toBe(90);
    expect(hbe.x).toBe(10);
    expect(hbe.y).toBe(20);
    expect(hbe.isRammed).toBe(true);

    // BulletHitBotEvent
    const bullet = new BulletState(1, 1, 3, 100, 200, 45, "#FF0000");
    const bhbe = new Events.BulletHitBotEvent(9, 10, bullet, 5, 90);
    expect(bhbe.turnNumber).toBe(9);
    expect(bhbe.victimId).toBe(10);
    expect(bhbe.bullet).toBe(bullet);
    expect(bhbe.damage).toBe(5);
    expect(bhbe.energy).toBe(90);

    // BotDeathEvent
    const bde = new Events.BotDeathEvent(12, 13);
    expect(bde.turnNumber).toBe(12);
    expect(bde.victimId).toBe(13);

    // WonRoundEvent
    const wre = new Events.WonRoundEvent(16);
    expect(wre.turnNumber).toBe(16);

    // TeamMessageEvent
    const tme = new Events.TeamMessageEvent(17, "hello", 18);
    expect(tme.turnNumber).toBe(17);
    expect(tme.message).toBe("hello");
    expect(tme.senderId).toBe(18);

    // CustomEvent
    const condition = new (class extends Condition { test() { return true; } })("test");
    const ce = new Events.CustomEvent(19, condition);
    expect(ce.turnNumber).toBe(19);
    expect(ce.condition).toBe(condition);
  });

  it("TR-API-EVT-008: Condition.test() callable and overridable", () => {
    const c1 = new (class extends Condition { test() { return true; } })("true");
    expect(c1.test()).toBe(true);

    const c2 = new (class extends Condition { test() { return false; } })("false");
    expect(c2.test()).toBe(false);
  });

  it("TR-API-EVT-009: CustomEvent dispatches when Condition.test() is true", () => {
    const queue = new EventQueue(priorities, interruption);
    const fireSpy = vi.spyOn(handlers, "fireEvent");

    const condTrue = new (class extends Condition { test() { return true; } })("true");
    const condFalse = new (class extends Condition { test() { return false; } })("false");

    queue.addCondition(condTrue);
    queue.addCondition(condFalse);

    queue.dispatchEvents(5, handlers);

    expect(fireSpy).toHaveBeenCalledTimes(1);
    expect(fireSpy.mock.calls[0][0]).toBeInstanceOf(Events.CustomEvent);
    expect((fireSpy.mock.calls[0][0] as Events.CustomEvent).condition).toBe(condTrue);
    
    fireSpy.mockRestore();
  });
});
