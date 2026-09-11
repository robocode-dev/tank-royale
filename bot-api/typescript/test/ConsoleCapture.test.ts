/**
 * Unit tests for ConsoleCapture — the console.log/info/warn/error capture mechanism
 * implementing IDR-005 (P-005/M-017, TBA-132..TBA-136).
 */
import { describe, it, expect, vi, afterEach } from "vitest";
import { ConsoleCapture } from "../src/internal/ConsoleCapture.js";

describe("Unit: ConsoleCapture", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("TBA-132: console.log and console.info are captured into stdOut", () => {
    const capture = new ConsoleCapture();
    const logSpy = vi.spyOn(console, "log").mockImplementation(() => {});
    const infoSpy = vi.spyOn(console, "info").mockImplementation(() => {});

    capture.install();
    console.log("hello");
    console.info("world");
    const { stdOut, stdErr } = capture.drain();
    capture.restore();

    expect(stdOut).toContain("hello");
    expect(stdOut).toContain("world");
    expect(stdErr).toBeNull();
    // Pass-through: the real console method still ran.
    expect(logSpy).toHaveBeenCalledWith("hello");
    expect(infoSpy).toHaveBeenCalledWith("world");
  });

  it("TBA-133: console.warn and console.error are captured into stdErr, not stdOut", () => {
    const capture = new ConsoleCapture();
    const warnSpy = vi.spyOn(console, "warn").mockImplementation(() => {});
    const errorSpy = vi.spyOn(console, "error").mockImplementation(() => {});

    capture.install();
    console.warn("careful");
    console.error("oops");
    const { stdOut, stdErr } = capture.drain();
    capture.restore();

    expect(stdErr).toContain("careful");
    expect(stdErr).toContain("oops");
    expect(stdOut).toBeNull();
    expect(warnSpy).toHaveBeenCalledWith("careful");
    expect(errorSpy).toHaveBeenCalledWith("oops");
  });

  it("TBA-132: preserves newlines, carriage returns, tabs, backslashes, and quotes", () => {
    const capture = new ConsoleCapture();
    const logSpy = vi.spyOn(console, "log").mockImplementation(() => {});
    const message = `Hello\nWorld\r\tPath: C:\\temp\\file.txt \"quoted\"`;

    capture.install();
    console.log(message);
    const { stdOut } = capture.drain();
    capture.restore();

    expect(stdOut).toBe(`${message}\n`);
    expect(logSpy).toHaveBeenCalledWith(message);
  });

  it("TBA-132/133 (negative): drain returns null for a stream with no output", () => {
    const capture = new ConsoleCapture();
    vi.spyOn(console, "log").mockImplementation(() => {});

    capture.install();
    console.log("only stdout");
    const { stdOut, stdErr } = capture.drain();
    capture.restore();

    expect(stdOut).toContain("only stdout");
    expect(stdErr).toBeNull();
  });

  it("multi-argument and non-string console.log calls are formatted before being appended", () => {
    const capture = new ConsoleCapture();
    vi.spyOn(console, "log").mockImplementation(() => {});

    capture.install();
    console.log("score:", 42, { hit: true });
    const { stdOut } = capture.drain();
    capture.restore();

    expect(typeof stdOut).toBe("string");
    expect(stdOut).toContain("score:");
    expect(stdOut).toContain("42");
    expect(stdOut).toContain("hit");
  });

  it("drain clears the buffers so unchanged output is never returned twice", () => {
    const capture = new ConsoleCapture();
    vi.spyOn(console, "log").mockImplementation(() => {});

    capture.install();
    console.log("once");
    const first = capture.drain();
    const second = capture.drain();
    capture.restore();

    expect(first.stdOut).toContain("once");
    expect(second.stdOut).toBeNull();
  });

  it("TBA-136: restore() reverts console methods to their originals", () => {
    const capture = new ConsoleCapture();
    const originalLog = console.log;

    capture.install();
    expect(console.log).not.toBe(originalLog);
    capture.restore();

    expect(console.log).toBe(originalLog);
  });

  it("TBA-136 (negative): after restore(), console.log is no longer captured", () => {
    const capture = new ConsoleCapture();
    const logSpy = vi.spyOn(console, "log").mockImplementation(() => {});

    capture.install();
    capture.restore();
    console.log("not captured");
    const { stdOut } = capture.drain();

    expect(stdOut).toBeNull();
    expect(logSpy).toHaveBeenCalledWith("not captured");
  });

  it("install() and restore() are idempotent", () => {
    const capture = new ConsoleCapture();
    const logSpy = vi.spyOn(console, "log").mockImplementation(() => {});
    const preInstallLog = console.log; // what capture.restore() must bring back

    capture.install();
    capture.install(); // second install must not double-wrap or lose the original
    console.log("x");
    capture.restore();
    capture.restore(); // second restore must be a no-op

    expect(console.log).toBe(preInstallLog);
    expect(logSpy).toHaveBeenCalledWith("x");
  });
});
