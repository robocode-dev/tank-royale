import { describe, it, expect } from "vitest";
import { readdirSync, readFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const TEST_DIR = dirname(fileURLToPath(import.meta.url));
const ACCEPTANCE_ID = /(?:[A-Z][A-Z0-9]*-)+\d+[a-z]?/g;
const TEST_CALL = /\b(?:it|test)(?:\.[A-Za-z]+)?\s*\(\s*["'`]/g;
const DESCRIBE = /describe\s*\(\s*["']([^"']+)["']\s*,/g;

type Suite = { start: number; end: number; purposes: string[] };

function matchingBrace(source: string, openingBrace: number): number {
  let depth = 0;
  let quote = "";
  let lineComment = false;
  let blockComment = false;

  for (let index = openingBrace; index < source.length; index += 1) {
    const character = source[index];
    const next = source[index + 1];

    if (lineComment) {
      if (character === "\n") lineComment = false;
      continue;
    }
    if (blockComment) {
      if (character === "*" && next === "/") {
        blockComment = false;
        index += 1;
      }
      continue;
    }
    if (quote) {
      if (character === "\\") {
        index += 1;
      } else if (character === quote) {
        quote = "";
      }
      continue;
    }
    if (character === "/" && next === "/") {
      lineComment = true;
      index += 1;
      continue;
    }
    if (character === "/" && next === "*") {
      blockComment = true;
      index += 1;
      continue;
    }
    if (character === "\"" || character === "'" || character === "`") {
      quote = character;
      continue;
    }
    if (character === "{") depth += 1;
    if (character === "}" && --depth === 0) return index;
  }

  return -1;
}

function suitePurpose(name: string): string[] {
  const acceptance = name.match(ACCEPTANCE_ID) ?? [];
  if (acceptance.length > 0) return acceptance;
  const generic = name.match(/^(Unit|Sanity|Arch):/);
  return generic ? [generic[1]] : [];
}

function suitesIn(source: string): Suite[] {
  const suites: Suite[] = [];
  for (const match of source.matchAll(DESCRIBE)) {
    const openingBrace = source.indexOf("{", (match.index ?? 0) + match[0].length);
    const closingBrace = matchingBrace(source, openingBrace);
    suites.push({ start: match.index ?? 0, end: closingBrace, purposes: suitePurpose(match[1]) });
  }
  return suites;
}

describe("Arch: test-purpose declarations", () => {
  it("every TypeScript test has exactly one effective purpose", () => {
    const failures: string[] = [];

    for (const file of readdirSync(TEST_DIR).filter(name => name.endsWith(".test.ts"))) {
      const source = readFileSync(join(TEST_DIR, file), "utf8");
      const suites = suitesIn(source);

      for (const test of source.matchAll(TEST_CALL)) {
        const position = test.index ?? 0;
        const containing = suites
          .filter(suite => suite.start < position && position < suite.end)
          .sort((left, right) => right.start - left.start);
        const nearestPurpose = containing.find(suite => suite.purposes.length > 0)?.purposes ?? [];
        if (nearestPurpose.length !== 1) {
          failures.push(`${file}:${source.slice(0, position).split("\n").length} -> ${nearestPurpose}`);
        }
      }
    }

    expect(failures, failures.join("\n")).toEqual([]);
  });
});
