import { describe, it, expect } from 'vitest';
import * as fs from 'fs';
import * as path from 'path';
import { BaseBotInternals } from '../src/internal/BaseBotInternals.js';
import { IntentValidator } from '../src/internal/intentValidator.js';
import { BotInfo } from '../src/BotInfo.js';
import { Color } from '../src/graphics/Color.js';
import { Constants } from '../src/Constants.js';
import { TickEvent } from '../src/events/TickEvent.js';
import { BotState } from '../src/BotState.js';

const sharedTestsDir = path.resolve(__dirname, '../../tests/shared');

interface TestCase {
  id: string;
  description: string;
  type: string;
  method: string;
  setup?: Record<string, any>;
  args?: any[];
  expected: Record<string, any>;
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
          const mockBot: any = {
            energy: 100,
            gunHeat: 0,
          };
          const internals = new BaseBotInternals(mockBot, null, "ws://localhost", null);

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
              case 'getConstant': lastActionValue = (Constants as any)[args[0]]; break;
              default: throw new Error(`Method ${testCase.method} not implemented in runner`);
            }
          };

          if (testCase.expected.throws) {
            expect(runAction).toThrow();
          } else {
            runAction();

            if (testCase.expected.returns !== undefined) {
              const expectedRet = parseArg(testCase.expected.returns);
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
            for (const [key, val] of Object.entries(testCase.expected)) {
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
