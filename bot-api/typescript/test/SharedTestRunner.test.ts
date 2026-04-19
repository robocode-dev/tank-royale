import { describe, it, expect, vi } from 'vitest';
import * as fs from 'fs';
import * as path from 'path';
import { BaseBotInternals } from '../src/internal/BaseBotInternals.js';
import { IntentValidator } from '../src/internal/intentValidator.js';
import { BotInfo } from '../src/BotInfo.js';
import { Color } from '../src/graphics/Color.js';
import { Constants } from '../src/Constants.js';
import { TickEvent } from '../src/events/TickEvent.js';
import { BotState } from '../src/BotState.js';
import { DefaultEventPriority } from '../src/DefaultEventPriority.js';
import { GameType } from '../src/GameType.js';
import { BulletState } from '../src/BulletState.js';
import * as Events from '../src/events/index.js';
import { Condition } from '../src/events/Condition.js';
import { BaseBot } from '../src/BaseBot.js';
import { EventQueue } from '../src/events/EventQueue.js';
import { EventPriorities } from '../src/events/EventPriorities.js';
import { EventInterruption } from '../src/events/EventInterruption.js';
import { BotEventHandlers } from '../src/events/BotEventHandlers.js';

const sharedTestsDir = path.resolve(__dirname, '../../tests/shared');

interface Step {
  action: string;
  eventType?: string;
  turnNumber?: number;
  repeat?: number;
  atTurn?: number;
}

interface ExpectAfter {
  dispatchOrder?: string[];
  queueSize?: number;
}

interface TestCase {
  id: string;
  description: string;
  type: string;
  method: string;
  setup?: Record<string, any>;
  args?: any[];
  expected?: Record<string, any>;
  steps?: Step[];
  expectAfter?: ExpectAfter;
}

interface TestSuite {
  suite: string;
  description: string;
  tests: TestCase[];
}

function getSharedTestSuites(): TestSuite[] {
  const suites: TestSuite[] = [];
  if (!fs.existsSync(sharedTestsDir)) return suites;

  const files = fs.readdirSync(sharedTestsDir);
  for (const file of files) {
    if (file.endsWith('.json') && !file.endsWith('schema.json')) {
      const content = fs.readFileSync(path.join(sharedTestsDir, file), 'utf-8');
      suites.push(JSON.parse(content));
    }
  }
  return suites;
}

function parseArg(arg: any): any {
  if (arg === "NaN") return NaN;
  if (arg === "Infinity") return Infinity;
  if (arg === "-Infinity") return -Infinity;
  if (typeof arg === 'object' && arg !== null && 'r' in arg) {
    return Color.fromRgba(arg.r, arg.g, arg.b, arg.a ?? 255);
  }
  return arg;
}

describe('Shared Cross-Platform Tests', () => {
  const suites = getSharedTestSuites();

  for (const suite of suites) {
    describe(suite.suite, () => {
      for (const testCase of suite.tests) {
        it(testCase.id + ': ' + testCase.description, () => {
          if (testCase.type === 'scenario') {
            executeScenario(testCase);
            return;
          }
          if (testCase.type === 'botDefault') {
            executeBotDefault(testCase);
            return;
          }

          const botInfo = new BotInfo("TestBot", "1.0", ["Author"], null, null, null, ["classic"], null, null);
          const mockBot: any = new (class extends BaseBot {
            constructor() { super(botInfo); }
            run() {}
          })();
          const internals = (mockBot as any)._internals;

          if (testCase.setup) {
            const setup = testCase.setup;
            const botState: any = {
              energy: setup.energy ?? 100,
              gunHeat: setup.gunHeat ?? 0,
              speed: 0,
              turnRate: 0,
              gunTurnRate: 0,
              radarTurnRate: 0,
            };
            // Mocking tickEvent to provide state for BaseBotInternals.setFire etc.
            (internals as any).tickEvent = new TickEvent(1, 1, botState as BotState, [], []);
            
            if (setup.maxSpeed !== undefined) internals.setMaxSpeed(setup.maxSpeed);
            if (setup.maxTurnRate !== undefined) internals.setMaxTurnRate(setup.maxTurnRate);
            if (setup.maxGunTurnRate !== undefined) internals.setMaxGunTurnRate(setup.maxGunTurnRate);
            if (setup.maxRadarTurnRate !== undefined) internals.setMaxRadarTurnRate(setup.maxRadarTurnRate);
          }

          let lastActionValue: any = null;
          const args = (testCase.args || []).map(parseArg);

          const runAction = () => {
            console.log(`Running method: ${testCase.method} with args: ${JSON.stringify(args)}`);
            switch (testCase.method) {
              case 'setFire': lastActionValue = internals.setFire(args[0]); break;
              case 'setTurnRate': internals.setTurnRate(args[0]); break;
              case 'setGunTurnRate': internals.setGunTurnRate(args[0]); break;
              case 'setRadarTurnRate': internals.setRadarTurnRate(args[0]); break;
              case 'setTargetSpeed': internals.setTargetSpeed(args[0]); break;
              case 'setMaxSpeed': internals.setMaxSpeed(args[0]); break;
              case 'setMaxTurnRate': internals.setMaxTurnRate(args[0]); break;
              case 'getNewTargetSpeed': lastActionValue = IntentValidator.getNewTargetSpeed(args[0], args[1], args[2]); break;
              case 'getDistanceTraveledUntilStop': lastActionValue = IntentValidator.getDistanceTraveledUntilStop(args[0], args[1]); break;
              case 'BotInfo':
                lastActionValue = new BotInfo(
                  args[0], args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]
                );
                break;
              case 'fromRgb': lastActionValue = Color.fromRgb(args[0], args[1], args[2]); break;
              case 'fromRgba': lastActionValue = Color.fromRgba(args[0], args[1], args[2], args[3]); break;
              case 'colorToHex': lastActionValue = IntentValidator.colorToHex(args[0]); break;
              case 'getColorConstant': lastActionValue = (Color as any)[args[0].toUpperCase()]; break;
              case 'getConstant':
                lastActionValue = (Constants as any)[args[0]] ?? (DefaultEventPriority as any)[args[0]] ?? (GameType as any)[args[0]];
                break;
              case 'isCritical': lastActionValue = createEvent(args[0]).isCritical; break;
              case 'getDefaultPriority': lastActionValue = getDefaultPriority(args[0]); break;
              case 'calcBulletSpeed': lastActionValue = mockBot.calcBulletSpeed(args[0]); break;
              case 'calcMaxTurnRate': lastActionValue = mockBot.calcMaxTurnRate(args[0]); break;
              case 'calcGunHeat': lastActionValue = mockBot.calcGunHeat(args[0]); break;
              case 'calcBearing':
                if (args.length === 2) {
                  mockBot.getDirection = () => args[0];
                  lastActionValue = mockBot.calcBearing(args[1]);
                } else {
                  lastActionValue = mockBot.calcBearing(args[0]);
                }
                break;
              case 'normalizeAbsoluteAngle':
                lastActionValue = mockBot.normalizeAbsoluteAngle(args[0]);
                console.log(`normalizeAbsoluteAngle(${args[0]}) = ${lastActionValue}`);
                break;
              case 'normalizeRelativeAngle':
                lastActionValue = mockBot.normalizeRelativeAngle(args[0]);
                console.log(`normalizeRelativeAngle(${args[0]}) = ${lastActionValue}`);
                break;
              default: throw new Error(`Method ${testCase.method} not implemented in runner`);
            }
          };

          if (testCase.expected!.throws) {
            expect(runAction).toThrow();
          } else {
            runAction();

            if (testCase.expected!.returns !== undefined) {
              const expectedRet = parseArg(testCase.expected!.returns);
              if (typeof expectedRet === 'number' && isNaN(expectedRet)) {
                expect(lastActionValue).toBeNaN();
              } else if (typeof expectedRet === 'string' && expectedRet.startsWith('#')) {
                expect(lastActionValue).toBeIgnoreCase(expectedRet);
              } else if (typeof expectedRet === 'number') {
                expect(lastActionValue).toBeCloseTo(expectedRet, 6);
              } else {
                expect(lastActionValue).toEqual(expectedRet);
              }
            }

            const intent = (internals as any).intent;
            for (const [key, val] of Object.entries(testCase.expected!)) {
              if (key === 'returns' || key === 'throws') continue;

              let actual: any = null;
              if (key === 'firepower') actual = intent.firepower ?? 0;
              else if (key === 'turnRate') actual = intent.turnRate;
              else if (key === 'gunTurnRate') actual = intent.gunTurnRate;
              else if (key === 'radarTurnRate') actual = intent.radarTurnRate;
              else if (key === 'targetSpeed') actual = intent.targetSpeed;
              else if (key === 'maxSpeed') actual = (internals as any).maxSpeed;
              else if (key === 'maxTurnRate') actual = (internals as any).maxTurnRate;
              else if (lastActionValue instanceof Color) {
                if (key === 'r') actual = lastActionValue.getR();
                else if (key === 'g') actual = lastActionValue.getG();
                else if (key === 'b') actual = lastActionValue.getB();
                else if (key === 'a') actual = lastActionValue.getA();
              } else if (lastActionValue instanceof BotInfo) {
                if (key === 'name') actual = lastActionValue.name;
                else if (key === 'version') actual = lastActionValue.version;
                else if (key === 'authors') actual = lastActionValue.authors;
                else if (key === 'countryCodes') actual = lastActionValue.countryCodes;
              }

              if (actual !== null) {
                if (typeof val === 'number') {
                  expect(actual).toBeCloseTo(val, 6);
                } else {
                  expect(actual).toEqual(val);
                }
              }
            }
          }
        });
      }
    });
  }
});

function executeBotDefault(testCase: TestCase): void {
  class BotDefaultStub extends BaseBot {
    constructor() {
      super(new BotInfo("StubBot", "1.0", ["Author"], null, null, null, ["classic"], null, null));
    }
    run() {}
  }
  const bot = new BotDefaultStub();

  const methodMap: Record<string, () => any> = {
    'getMyId':                  () => bot.getMyId(),
    'getVariant':               () => bot.getVariant(),
    'getVersion':               () => bot.getVersion(),
    'getEnergy':                () => bot.getEnergy(),
    'getX':                     () => bot.getX(),
    'getY':                     () => bot.getY(),
    'getDirection':             () => bot.getDirection(),
    'getGunDirection':          () => bot.getGunDirection(),
    'getRadarDirection':        () => bot.getRadarDirection(),
    'getSpeed':                 () => bot.getSpeed(),
    'getGunHeat':               () => bot.getGunHeat(),
    'getBulletStates':          () => bot.getBulletStates(),
    'getEvents':                () => bot.getEvents(),
    'getArenaWidth':            () => bot.getArenaWidth(),
    'getArenaHeight':           () => bot.getArenaHeight(),
    'getGameType':              () => bot.getGameType(),
    'isAdjustGunForBodyTurn':   () => bot.isAdjustGunForBodyTurn(),
    'isAdjustRadarForBodyTurn': () => bot.isAdjustRadarForBodyTurn(),
    'isAdjustRadarForGunTurn':  () => bot.isAdjustRadarForGunTurn(),
  };

  const call = methodMap[testCase.method];
  if (!call) throw new Error(`Unknown botDefault method: ${testCase.method}`);

  const expected = testCase.expected!;

  if (expected.throws) {
    expect(call).toThrow();
  } else if (expected.returns !== undefined) {
    const result = call();
    if (typeof expected.returns === 'number') {
      expect(result).toBeCloseTo(expected.returns, 6);
    } else {
      expect(result).toBe(expected.returns);
    }
  } else if (expected.returnsEmpty) {
    const result = call();
    if (result instanceof Set) {
      expect(result.size).toBe(0);
    } else if (Array.isArray(result)) {
      expect(result.length).toBe(0);
    }
  }
}

function getDefaultPriority(eventName: string): number {
  const normalized = eventName.replace("Event", "").replace(/([a-z])([A-Z])/g, '$1_$2').toUpperCase();
  return (DefaultEventPriority as any)[normalized];
}

function createEventAt(name: string, turn: number): Events.BotEvent {
  switch (name) {
    case "WonRoundEvent":    return new Events.WonRoundEvent(turn);
    case "DeathEvent":       return new Events.DeathEvent(turn);
    case "ScannedBotEvent":  return new Events.ScannedBotEvent(turn, 0, 0, 0, 0, 0, 0, 0);
    case "SkippedTurnEvent": return new Events.SkippedTurnEvent(turn);
    case "BotDeathEvent":    return new Events.BotDeathEvent(turn, 0);
    default: throw new Error(`Unknown event for scenario: ${name}`);
  }
}

function executeScenario(testCase: TestCase): void {
  const priorities = new EventPriorities();
  const interruption = new EventInterruption();
  const queue = new EventQueue(priorities, interruption);
  const handlers = new BotEventHandlers();
  const fireSpy = vi.spyOn(handlers, 'fireEvent');

  for (const step of testCase.steps ?? []) {
    if (step.action === 'addEvent') {
      const repeat = step.repeat ?? 1;
      for (let i = 0; i < repeat; i++) {
        queue.addEvent(createEventAt(step.eventType!, step.turnNumber!));
      }
    } else if (step.action === 'dispatchEvents') {
      queue.dispatchEvents(step.atTurn!, handlers);
    }
  }

  const expectAfter = testCase.expectAfter;
  if (expectAfter?.dispatchOrder) {
    const fired = fireSpy.mock.calls;
    expect(fired).toHaveLength(expectAfter.dispatchOrder.length);
    for (let i = 0; i < expectAfter.dispatchOrder.length; i++) {
      expect(fired[i][0].constructor.name).toBe(expectAfter.dispatchOrder[i]);
    }
  }
  if (expectAfter?.queueSize !== undefined) {
    expect(queue.getEvents()).toHaveLength(expectAfter.queueSize);
  }

  fireSpy.mockRestore();
}

function createEvent(eventName: string): any {
  switch (eventName) {
    case "BotDeathEvent": return new Events.BotDeathEvent(0, 0);
    case "WonRoundEvent": return new Events.WonRoundEvent(0);
    case "SkippedTurnEvent": return new Events.SkippedTurnEvent(0);
    case "BotHitBotEvent": return new Events.HitBotEvent(0, 0, 0, 0, 0, false);
    case "BotHitWallEvent": return new Events.HitWallEvent(0);
    case "BulletFiredEvent": return new Events.BulletFiredEvent(0, new BulletState(0, 0, 0, 0, 0, 0, "#000000"));
    case "BulletHitBotEvent": return new Events.BulletHitBotEvent(0, 0, new BulletState(0, 0, 0, 0, 0, 0, "#000000"), 0, 0);
    case "BulletHitBulletEvent": return new Events.BulletHitBulletEvent(0, new BulletState(0, 0, 0, 0, 0, 0, "#000000"), new BulletState(0, 0, 0, 0, 0, 0, "#000000"));
    case "BulletHitWallEvent": return new Events.BulletHitWallEvent(0, new BulletState(0, 0, 0, 0, 0, 0, "#000000"));
    case "HitByBulletEvent": return new Events.HitByBulletEvent(0, new BulletState(0, 0, 0, 0, 0, 0, "#000000"), 0, 0);
    case "ScannedBotEvent": return new Events.ScannedBotEvent(0, 0, 0, 0, 0, 0, 0, 0);
    case "CustomEvent": return new Events.CustomEvent(0, new (class extends Condition { test() { return true; } })("test"));
    case "TeamMessageEvent": return new Events.TeamMessageEvent(0, "test", 0);
    case "TickEvent": return new TickEvent(0, 0, null as any, [], []);
    case "DeathEvent": return new Events.DeathEvent(0);
    case "HitWallEvent": return new Events.HitWallEvent(0);
    case "HitBotEvent": return new Events.HitBotEvent(0, 0, 0, 0, 0, false);
    default: throw new Error(`Unknown event: ${eventName}`);
  }
}

// Custom matcher for case-insensitive hex comparison
expect.extend({
  toBeIgnoreCase(received: string, expected: string) {
    const pass = received.toLowerCase() === expected.toLowerCase();
    if (pass) {
      return {
        message: () => `expected ${received} not to be equal to ${expected} (case-insensitive)`,
        pass: true,
      };
    } else {
      return {
        message: () => `expected ${received} to be equal to ${expected} (case-insensitive)`,
        pass: false,
      };
    }
  },
});
