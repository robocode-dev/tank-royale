import { RuntimeAdapter } from "./RuntimeAdapter.js";
import { NodeRuntimeAdapter } from "./NodeRuntimeAdapter.js";
import { BrowserRuntimeAdapter } from "./BrowserRuntimeAdapter.js";

/**
 * Auto-detects the current runtime environment and returns the appropriate
 * {@link RuntimeAdapter} implementation.
 *
 * Detection strategy: if `process` is defined and has an `env` property we
 * assume Node.js (or a compatible runtime such as Deno/Bun); otherwise we
 * fall back to the browser adapter.
 */
export function detectRuntime(): RuntimeAdapter {
  if (typeof process !== "undefined" && process.env !== undefined) {
    return new NodeRuntimeAdapter();
  }
  return new BrowserRuntimeAdapter();
}
