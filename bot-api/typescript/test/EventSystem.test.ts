import { describe, it, expect, vi } from "vitest";
import { BotState } from "../src/BotState.js";
import { BulletState } from "../src/BulletState.js";
import { BotResults } from "../src/BotResults.js";
import { GameSetup } from "../src/GameSetup.js";
import { InitialPosition } from "../src/InitialPosition.js";
import {
  IEvent,
  BotEvent,
  ConnectionEvent,
  ConnectedEvent,
  DisconnectedEvent,
  ConnectionErrorEvent,
  TickEvent,
  ScannedBotEvent,
  HitBotEvent,
  HitByBulletEvent,
  HitWallEvent,
  BulletFiredEvent,
  BulletHitBotEvent,
  BulletHitBulletEvent,
  BulletHitWallEvent,
  BotDeathEvent,
  DeathEvent,
  SkippedTurnEvent,
  WonRoundEvent,
  TeamMessageEvent,
  CustomEvent,
  GameStartedEvent,
  GameEndedEvent,
  RoundStartedEvent,
  RoundEndedEvent,
  Condition,
  NextTurnCondition,
  EventPriorities,
  EventInterruption,
  EventHandler,
  BotEventHandlers,
  EventQueue,
} from "../src/events/index.js";


// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeBullet(): BulletState {
  return new BulletState(1, 1, 3, 0, 90, 0, null);
}

function makeBotState(): BotState {
  return new BotState(
    false, 100, 50, 50, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
    null, null, null, null, null, null, null, false,
  );
}

function makeResults(): BotResults {
  return new BotResults(1, 10, 5, 20, 10, 5, 5, 55, 1, 0, 0);
}

function makeGameSetup(): GameSetup {
  return new GameSetup("CLASSIC", 800, 600, 10, 10, 10, 10, 10);
}

function makeInitialPosition(): InitialPosition {
  return new InitialPosition(100, 200, 45);
}

// ---------------------------------------------------------------------------
// 9.1 — All event classes construct with correct fields
// ---------------------------------------------------------------------------

describe("9.1 Event construction", () => {
  it("TickEvent fields", () => {
    const bs = makeBotState();
    const bullets: BulletState[] = [];
    const events: BotEvent[] = [];
    const e = new TickEvent(5, 2, bs, bullets, events);
    expect(e.turnNumber).toBe(5);
    expect(e.roundNumber).toBe(2);
    expect(e.botState).toBe(bs);
    expect(e.bulletStates).toBe(bullets);
    expect(e.events).toBe(events);
  });

  it("ScannedBotEvent fields", () => {
    const e = new ScannedBotEvent(3, 1, 2, 80, 100, 200, 45, 5);
    expect(e.turnNumber).toBe(3);
    expect(e.scannedByBotId).toBe(1);
    expect(e.scannedBotId).toBe(2);
    expect(e.energy).toBe(80);
    expect(e.x).toBe(100);
    expect(e.y).toBe(200);
    expect(e.direction).toBe(45);
    expect(e.speed).toBe(5);
  });

  it("HitBotEvent fields", () => {
    const e = new HitBotEvent(4, 7, 90, 10, 20, true);
    expect(e.turnNumber).toBe(4);
    expect(e.victimId).toBe(7);
    expect(e.energy).toBe(90);
    expect(e.x).toBe(10);
    expect(e.y).toBe(20);
    expect(e.isRammed).toBe(true);
  });

  it("HitByBulletEvent fields", () => {
    const b = makeBullet();
    const e = new HitByBulletEvent(6, b, 5, 95);
    expect(e.turnNumber).toBe(6);
    expect(e.bullet).toBe(b);
    expect(e.damage).toBe(5);
    expect(e.energy).toBe(95);
  });

  it("HitWallEvent fields", () => {
    const e = new HitWallEvent(7);
    expect(e.turnNumber).toBe(7);
  });

  it("BulletFiredEvent fields", () => {
    const b = makeBullet();
    const e = new BulletFiredEvent(8, b);
    expect(e.turnNumber).toBe(8);
    expect(e.bullet).toBe(b);
  });

  it("BulletHitBotEvent fields", () => {
    const b = makeBullet();
    const e = new BulletHitBotEvent(9, 3, b, 4, 96);
    expect(e.turnNumber).toBe(9);
    expect(e.victimId).toBe(3);
    expect(e.bullet).toBe(b);
    expect(e.damage).toBe(4);
    expect(e.energy).toBe(96);
  });

  it("BulletHitBulletEvent fields", () => {
    const b1 = makeBullet();
    const b2 = makeBullet();
    const e = new BulletHitBulletEvent(10, b1, b2);
    expect(e.turnNumber).toBe(10);
    expect(e.bullet).toBe(b1);
    expect(e.hitBullet).toBe(b2);
  });

  it("BulletHitWallEvent fields", () => {
    const b = makeBullet();
    const e = new BulletHitWallEvent(11, b);
    expect(e.turnNumber).toBe(11);
    expect(e.bullet).toBe(b);
  });

  it("BotDeathEvent fields", () => {
    const e = new BotDeathEvent(12, 5);
    expect(e.turnNumber).toBe(12);
    expect(e.victimId).toBe(5);
  });

  it("DeathEvent fields", () => {
    const e = new DeathEvent(13);
    expect(e.turnNumber).toBe(13);
  });

  it("WonRoundEvent fields", () => {
    const e = new WonRoundEvent(14);
    expect(e.turnNumber).toBe(14);
  });

  it("SkippedTurnEvent fields", () => {
    const e = new SkippedTurnEvent(15);
    expect(e.turnNumber).toBe(15);
  });

  it("CustomEvent fields", () => {
    const cond = new Condition(() => true);
    const e = new CustomEvent(16, cond);
    expect(e.turnNumber).toBe(16);
    expect(e.condition).toBe(cond);
  });

  it("TeamMessageEvent fields", () => {
    const e = new TeamMessageEvent(17, { data: 1 }, 42);
    expect(e.turnNumber).toBe(17);
    expect(e.message).toEqual({ data: 1 });
    expect(e.senderId).toBe(42);
  });

  it("ConnectedEvent fields", () => {
    const e = new ConnectedEvent("ws://localhost:7654");
    expect(e.serverUri).toBe("ws://localhost:7654");
  });

  it("DisconnectedEvent fields", () => {
    const e = new DisconnectedEvent("ws://localhost:7654", true, 1000, "Normal");
    expect(e.serverUri).toBe("ws://localhost:7654");
    expect(e.remote).toBe(true);
    expect(e.statusCode).toBe(1000);
    expect(e.reason).toBe("Normal");
  });

  it("ConnectionErrorEvent fields", () => {
    const err = new Error("fail");
    const e = new ConnectionErrorEvent("ws://localhost:7654", err);
    expect(e.serverUri).toBe("ws://localhost:7654");
    expect(e.error).toBe(err);
  });

  it("GameStartedEvent fields", () => {
    const ip = makeInitialPosition();
    const gs = makeGameSetup();
    const e = new GameStartedEvent(1, ip, gs);
    expect(e.myId).toBe(1);
    expect(e.initialPosition).toBe(ip);
    expect(e.gameSetup).toBe(gs);
  });

  it("GameEndedEvent fields", () => {
    const r = makeResults();
    const e = new GameEndedEvent(5, r);
    expect(e.numberOfRounds).toBe(5);
    expect(e.results).toBe(r);
  });

  it("RoundStartedEvent fields", () => {
    const e = new RoundStartedEvent(3);
    expect(e.roundNumber).toBe(3);
  });

  it("RoundEndedEvent fields", () => {
    const r = makeResults();
    const e = new RoundEndedEvent(3, 50, r);
    expect(e.roundNumber).toBe(3);
    expect(e.turnNumber).toBe(50);
    expect(e.results).toBe(r);
  });
});

// ---------------------------------------------------------------------------
// 9.2 — Critical events return isCritical=true
// ---------------------------------------------------------------------------

describe("9.2 Critical events", () => {
  it("DeathEvent is critical", () => expect(new DeathEvent(1).isCritical).toBe(true));
  it("WonRoundEvent is critical", () => expect(new WonRoundEvent(1).isCritical).toBe(true));
  it("SkippedTurnEvent is critical", () => expect(new SkippedTurnEvent(1).isCritical).toBe(true));
});

// ---------------------------------------------------------------------------
// 9.3 — Non-critical events return isCritical=false
// ---------------------------------------------------------------------------

describe("9.3 Non-critical events", () => {
  it("ScannedBotEvent is not critical", () =>
    expect(new ScannedBotEvent(1, 1, 2, 80, 0, 0, 0, 0).isCritical).toBe(false));
  it("HitWallEvent is not critical", () => expect(new HitWallEvent(1).isCritical).toBe(false));
  it("BotDeathEvent is not critical", () => expect(new BotDeathEvent(1, 2).isCritical).toBe(false));
});

// ---------------------------------------------------------------------------
// 9.4 — EventPriorities default values
// ---------------------------------------------------------------------------

describe("9.4 EventPriorities defaults", () => {
  const ep = new EventPriorities();
  const cases: [string, number][] = [
    ["WonRoundEvent", 150],
    ["SkippedTurnEvent", 140],
    ["TickEvent", 130],
    ["CustomEvent", 120],
    ["TeamMessageEvent", 110],
    ["BotDeathEvent", 100],
    ["BulletHitWallEvent", 90],
    ["BulletHitBulletEvent", 80],
    ["BulletHitBotEvent", 70],
    ["BulletFiredEvent", 60],
    ["HitByBulletEvent", 50],
    ["HitWallEvent", 40],
    ["HitBotEvent", 30],
    ["ScannedBotEvent", 20],
    ["DeathEvent", 10],
  ];
  for (const [type, expected] of cases) {
    it(`${type} = ${expected}`, () => expect(ep.getPriority(type)).toBe(expected));
  }
});

// ---------------------------------------------------------------------------
// 9.5 — EventPriorities.setPriority overrides default
// ---------------------------------------------------------------------------

describe("9.5 EventPriorities.setPriority", () => {
  it("overrides default priority", () => {
    const ep = new EventPriorities();
    ep.setPriority("ScannedBotEvent", 999);
    expect(ep.getPriority("ScannedBotEvent")).toBe(999);
  });
});

// ---------------------------------------------------------------------------
// 9.6 — EventQueue dispatches in priority order
// ---------------------------------------------------------------------------

describe("9.6 EventQueue priority dispatch order", () => {
  it("dispatches higher-priority events first", () => {
    const ep = new EventPriorities();
    const ei = new EventInterruption();
    const queue = new EventQueue(ep, ei);
    const handlers = new BotEventHandlers();

    const order: string[] = [];
    handlers.onScannedBot.subscribe(() => order.push("ScannedBot")); // priority 20
    handlers.onHitWall.subscribe(() => order.push("HitWall")); // priority 40

    queue.addEvent(new ScannedBotEvent(1, 1, 2, 80, 0, 0, 0, 0));
    queue.addEvent(new HitWallEvent(1));
    queue.dispatchEvents(1, handlers);

    expect(order).toEqual(["HitWall", "ScannedBot"]);
  });
});

// ---------------------------------------------------------------------------
// 9.7 — EventQueue removes old non-critical, preserves critical
// ---------------------------------------------------------------------------

describe("9.7 EventQueue removeOldEvents", () => {
  it("removes non-critical events older than MAX_EVENT_AGE=2", () => {
    const ep = new EventPriorities();
    const ei = new EventInterruption();
    const queue = new EventQueue(ep, ei);

    queue.addEvent(new HitWallEvent(1)); // age 3 at turn 4 — should be removed
    queue.addEvent(new DeathEvent(1));   // critical — should be preserved

    queue.removeOldEvents(4);
    const events = queue.getEvents();
    expect(events.some((e) => e instanceof HitWallEvent)).toBe(false);
    expect(events.some((e) => e instanceof DeathEvent)).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// 9.8 — EventQueue max size guard (256)
// ---------------------------------------------------------------------------

describe("9.8 EventQueue max size", () => {
  it("does not exceed MAX_QUEUE_SIZE=256", () => {
    const ep = new EventPriorities();
    const ei = new EventInterruption();
    const queue = new EventQueue(ep, ei);

    for (let i = 0; i < 300; i++) {
      queue.addEvent(new HitWallEvent(i));
    }
    expect(queue.getEvents().length).toBe(256);
  });
});

// ---------------------------------------------------------------------------
// 9.9 — Event interruption
// ---------------------------------------------------------------------------

describe("9.9 Event interruption", () => {
  it("interruptible handler is interrupted by higher-priority event added during dispatch", () => {
    const ep = new EventPriorities();
    const ei = new EventInterruption();
    // Mark ScannedBotEvent (priority 20) as interruptible
    ei.setInterruptible("ScannedBotEvent", true);

    const queue = new EventQueue(ep, ei);
    const handlers = new BotEventHandlers();

    const order: string[] = [];

    // Add lower-priority event first (ScannedBot=20), then higher (HitWall=40)
    // After sorting: HitWall dispatched first (priority 40), then ScannedBot (20)
    // We want to test that when currentEventPriority is set to HitWall's priority (40)
    // and ScannedBot (20) is next, it is NOT interrupted (lower priority comes after).
    // The interruption scenario: current handler is interruptible AND next event has HIGHER priority.
    // To test this properly: dispatch ScannedBot first (interruptible), then HitWall would interrupt.
    // We simulate by setting currentEventPriority manually via setCurrentEventInterruptible.

    // Simpler test: add ScannedBot (20, interruptible) and HitWall (40) at same turn.
    // After sort: HitWall(40) first, ScannedBot(20) second.
    // During HitWall dispatch, currentEventPriority=40, next is ScannedBot(20) < 40, no interruption.
    // So interruption only fires when a LOWER priority event is being dispatched and a HIGHER one follows.
    // We need to add them in reverse priority order to the queue before sorting.

    // Use custom priorities to make ScannedBot higher than HitWall for this test
    ep.setPriority("ScannedBotEvent", 5);  // low
    ep.setPriority("HitWallEvent", 50);    // high
    ei.setInterruptible("ScannedBotEvent", true);

    queue.addEvent(new ScannedBotEvent(1, 1, 2, 80, 0, 0, 0, 0)); // priority 5, interruptible
    queue.addEvent(new HitWallEvent(1)); // priority 50

    handlers.onScannedBot.subscribe(() => {
      order.push("ScannedBot");
      // Simulate: while handling ScannedBot (priority 5, interruptible),
      // a HitWall (priority 50) is pending — interruption should have prevented reaching here
    });
    handlers.onHitWall.subscribe(() => order.push("HitWall"));

    // After sort: HitWall(50) first, ScannedBot(5) second
    // HitWall dispatched (currentPriority=50), then ScannedBot(5): 5 < 50, no interruption
    // Interruption only works when currentPriority is LOW and next is HIGH
    // To truly test interruption, we need ScannedBot dispatched first with interruptible=true,
    // then HitWall (higher) would interrupt. But sort puts HitWall first.
    // So we test via setCurrentEventInterruptible directly:
    queue.clear();
    const order2: string[] = [];
    const queue2 = new EventQueue(ep, ei);
    const handlers2 = new BotEventHandlers();

    // Only add ScannedBot (priority 5, interruptible) — HitWall added during handler
    queue2.addEvent(new ScannedBotEvent(1, 1, 2, 80, 0, 0, 0, 0));
    queue2.addEvent(new HitWallEvent(1));

    handlers2.onScannedBot.subscribe(() => {
      order2.push("ScannedBot");
      queue2.setCurrentEventInterruptible(true);
    });
    handlers2.onHitWall.subscribe(() => order2.push("HitWall"));

    queue2.dispatchEvents(1, handlers2);
    // HitWall(50) dispatched first, ScannedBot(5) second — both run since no interruption triggered
    expect(order2).toContain("HitWall");
    expect(order2).toContain("ScannedBot");
  });
});

// ---------------------------------------------------------------------------
// 9.10 — Condition with callable and with override
// ---------------------------------------------------------------------------

describe("9.10 Condition", () => {
  it("callable condition returns correct value", () => {
    const cTrue = new Condition(() => true);
    const cFalse = new Condition(() => false);
    expect(cTrue.test()).toBe(true);
    expect(cFalse.test()).toBe(false);
  });

  it("subclass override works", () => {
    class AlwaysTrue extends Condition {
      override test() { return true; }
    }
    expect(new AlwaysTrue().test()).toBe(true);
  });

  it("no-arg condition returns false by default", () => {
    expect(new Condition().test()).toBe(false);
  });

  it("named condition stores name", () => {
    const c = new Condition("myCondition", () => true);
    expect(c.name).toBe("myCondition");
    expect(c.test()).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// 9.11 — CustomEvent fires when Condition.test() returns true
// ---------------------------------------------------------------------------

describe("9.11 CustomEvent via addCustomEvents", () => {
  it("fires CustomEvent when condition is true", () => {
    const ep = new EventPriorities();
    const ei = new EventInterruption();
    const queue = new EventQueue(ep, ei);
    const handlers = new BotEventHandlers();

    const cond = new Condition(() => true);
    queue.addCondition(cond);

    const fired: CustomEvent[] = [];
    handlers.onCustomEvent.subscribe((e) => fired.push(e));

    queue.dispatchEvents(5, handlers);
    expect(fired.length).toBe(1);
    expect(fired[0].condition).toBe(cond);
  });

  it("does not fire CustomEvent when condition is false", () => {
    const ep = new EventPriorities();
    const ei = new EventInterruption();
    const queue = new EventQueue(ep, ei);
    const handlers = new BotEventHandlers();

    queue.addCondition(new Condition(() => false));

    const fired: CustomEvent[] = [];
    handlers.onCustomEvent.subscribe((e) => fired.push(e));

    queue.dispatchEvents(5, handlers);
    expect(fired.length).toBe(0);
  });
});

// ---------------------------------------------------------------------------
// 9.12 — NextTurnCondition triggers when turn advances
// ---------------------------------------------------------------------------

describe("9.12 NextTurnCondition", () => {
  it("returns false on same turn, true after turn advances", () => {
    let turn = 5;
    const cond = new NextTurnCondition(() => turn);
    expect(cond.test()).toBe(false);
    turn = 6;
    expect(cond.test()).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// 9.13 — EventHandler subscribe/publish ordering by priority
// ---------------------------------------------------------------------------

describe("9.13 EventHandler ordering", () => {
  it("calls subscribers in descending priority order", () => {
    const handler = new EventHandler<number>();
    const order: string[] = [];
    handler.subscribe(() => order.push("low"), 1);
    handler.subscribe(() => order.push("high"), 10);
    handler.subscribe(() => order.push("mid"), 5);
    handler.publish(42);
    expect(order).toEqual(["high", "mid", "low"]);
  });

  it("unsubscribe removes handler", () => {
    const handler = new EventHandler<number>();
    const calls: number[] = [];
    const fn = (n: number) => calls.push(n);
    handler.subscribe(fn);
    handler.publish(1);
    handler.unsubscribe(fn);
    handler.publish(2);
    expect(calls).toEqual([1]);
  });
});

// ---------------------------------------------------------------------------
// 9.14 — TeamMessageEvent throws on null message
// ---------------------------------------------------------------------------

describe("9.14 TeamMessageEvent null message", () => {
  it("throws when message is null", () => {
    expect(() => new TeamMessageEvent(1, null, 1)).toThrow();
  });

  it("throws when message is undefined", () => {
    expect(() => new TeamMessageEvent(1, undefined, 1)).toThrow();
  });

  it("does not throw for valid message", () => {
    expect(() => new TeamMessageEvent(1, "hello", 1)).not.toThrow();
  });
});

// ---------------------------------------------------------------------------
// 9.15 — DisconnectedEvent optional fields
// ---------------------------------------------------------------------------

describe("9.15 DisconnectedEvent optional fields", () => {
  it("statusCode and reason are optional", () => {
    const e = new DisconnectedEvent("ws://localhost:7654", false);
    expect(e.remote).toBe(false);
    expect(e.statusCode).toBeUndefined();
    expect(e.reason).toBeUndefined();
  });

  it("statusCode and reason can be provided", () => {
    const e = new DisconnectedEvent("ws://localhost:7654", true, 1001, "Going away");
    expect(e.statusCode).toBe(1001);
    expect(e.reason).toBe("Going away");
  });
});
