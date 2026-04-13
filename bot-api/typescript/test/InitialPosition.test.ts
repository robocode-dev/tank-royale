import { describe, it, expect } from "vitest";
import { InitialPosition } from "../src/InitialPosition.js";

describe("InitialPosition", () => {
  it("stores x, y, direction", () => {
    const pos = new InitialPosition(10, 20, 90);
    expect(pos.x).toBe(10);
    expect(pos.y).toBe(20);
    expect(pos.direction).toBe(90);
  });

  it("allows null fields", () => {
    const pos = new InitialPosition(null, null, null);
    expect(pos.x).toBeNull();
    expect(pos.y).toBeNull();
    expect(pos.direction).toBeNull();
  });

  it("toString returns empty string when all null", () => {
    expect(new InitialPosition(null, null, null).toString()).toBe("");
  });

  it("toString formats correctly", () => {
    expect(new InitialPosition(10, 20, 90).toString()).toBe("10,20,90");
    expect(new InitialPosition(null, 20, null).toString()).toBe(",20,");
    expect(new InitialPosition(10, null, 45).toString()).toBe("10,,45");
  });

  it("fromString returns null for null/blank/comma-only input", () => {
    expect(InitialPosition.fromString(null)).toBeNull();
    expect(InitialPosition.fromString("")).toBeNull();
    expect(InitialPosition.fromString("   ")).toBeNull();
    expect(InitialPosition.fromString(",,,")).toBeNull();
    expect(InitialPosition.fromString("  ,  ")).toBeNull();
  });

  it("fromString parses comma-separated values", () => {
    const pos = InitialPosition.fromString("10,20,90");
    expect(pos?.x).toBe(10);
    expect(pos?.y).toBe(20);
    expect(pos?.direction).toBe(90);
  });

  it("fromString parses whitespace-separated values", () => {
    const pos = InitialPosition.fromString("10 20 90");
    expect(pos?.x).toBe(10);
    expect(pos?.y).toBe(20);
    expect(pos?.direction).toBe(90);
  });

  it("fromString handles partial values", () => {
    const pos1 = InitialPosition.fromString("10");
    expect(pos1?.x).toBe(10);
    expect(pos1?.y).toBeNull();
    expect(pos1?.direction).toBeNull();

    const pos2 = InitialPosition.fromString("10,20");
    expect(pos2?.x).toBe(10);
    expect(pos2?.y).toBe(20);
    expect(pos2?.direction).toBeNull();
  });

  it("fromString treats invalid numbers as null", () => {
    const pos = InitialPosition.fromString("abc,20,90");
    expect(pos?.x).toBeNull();
    expect(pos?.y).toBe(20);
  });
});
