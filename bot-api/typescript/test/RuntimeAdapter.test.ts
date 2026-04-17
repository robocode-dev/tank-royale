import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { detectRuntime } from "../src/runtime/detectRuntime.js";
import { NodeRuntimeAdapter } from "../src/runtime/NodeRuntimeAdapter.js";
import { BrowserRuntimeAdapter } from "../src/runtime/BrowserRuntimeAdapter.js";

describe("detectRuntime()", () => {
  it("returns NodeRuntimeAdapter when process.env is available", () => {
    // In the vitest/Node.js test environment, process is always defined.
    const adapter = detectRuntime();
    expect(adapter).toBeInstanceOf(NodeRuntimeAdapter);
  });

  it("returns BrowserRuntimeAdapter when process is undefined", () => {
    const originalProcess = globalThis.process;
    // @ts-expect-error — intentionally removing process to simulate browser
    delete globalThis.process;
    try {
      const adapter = detectRuntime();
      expect(adapter).toBeInstanceOf(BrowserRuntimeAdapter);
    } finally {
      globalThis.process = originalProcess;
    }
  });
});

describe("NodeRuntimeAdapter", () => {
  let adapter: NodeRuntimeAdapter;

  beforeEach(() => {
    adapter = new NodeRuntimeAdapter();
  });

  it("getEnvVar returns value from process.env", () => {
    process.env["TEST_VAR_NODE"] = "hello";
    expect(adapter.getEnvVar("TEST_VAR_NODE")).toBe("hello");
    delete process.env["TEST_VAR_NODE"];
  });

  it("getEnvVar returns undefined for missing variable", () => {
    delete process.env["MISSING_VAR_XYZ"];
    expect(adapter.getEnvVar("MISSING_VAR_XYZ")).toBeUndefined();
  });

  it("exit calls process.exit with the given code", () => {
    const exitSpy = vi.spyOn(process, "exit").mockImplementation((() => {}) as () => never);
    adapter.exit(42);
    expect(exitSpy).toHaveBeenCalledWith(42);
    exitSpy.mockRestore();
  });
});

describe("BrowserRuntimeAdapter", () => {
  let adapter: BrowserRuntimeAdapter;

  beforeEach(() => {
    adapter = new BrowserRuntimeAdapter();
  });

  it("getEnvVar always returns undefined", () => {
    expect(adapter.getEnvVar("ANY_VAR")).toBeUndefined();
    expect(adapter.getEnvVar("SERVER_URL")).toBeUndefined();
  });

  it("exit is a no-op and does not throw", () => {
    expect(() => adapter.exit(0)).not.toThrow();
    expect(() => adapter.exit(1)).not.toThrow();
  });
});
