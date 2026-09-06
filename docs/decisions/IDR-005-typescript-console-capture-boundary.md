---
id: IDR-005
type: decision
status: inferred
links: [P-005]
title: TypeScript captures console output, not the standard-stream pipes
author: agent
accepted-by: []
---

# IDR-005 — TypeScript captures console output, not the standard-stream pipes

## Context

The Java, .NET, and Python Bot APIs each install a recording stream over the process's real standard-output/error pipes (`System.out`/`System.err`, `Console.Out`/`Console.Err`, `sys.stdout`/`sys.stderr`) and drain it into `BotIntent.stdOut`/`stdErr` on every intent send, plus once more right after a round ends, to catch output from final-tick handlers such as `onWonRound` that fire after the round's last intent already went out.

TypeScript has no equivalent pipe in a browser, and bot code doesn't idiomatically write to `process.stdout` even in Node — it calls `console.log`/`console.error`. Bot code also may not run on the same thread that owns the WebSocket: in worker mode (ADR-028, the default) `bot.run()` executes inside a Worker and the intent is built there, then handed to the main thread via `postMessage`; in the non-worker fallback (old Node without `worker_threads`, or a browser) bot code and the WebSocket both run on the single available thread.

## Decision

1. **Capture point:** override the four `console` methods — `log`/`info` into `stdOut`, `warn`/`error` into `stdErr` — rather than any stream or pipe. This surface exists identically in Node and the browser, and it is what idiomatic TS/JS bot code already calls.
2. **Installation site:** install the override once, in whichever context actually executes `bot.run()`: inside `startAsWorker()` when running as a Worker, and inside the non-worker `connect()` path otherwise, which covers both the legacy-Node fallback and the browser since both run bot code on the same thread that owns everything else.
3. **Crossing the Worker boundary:** no new channel. The captured buffers are drained into `stdOut`/`stdErr` fields on the same intent object that already crosses via `postMessage`, at the point `debugGraphics` is already attached (`renderGraphicsToIntent`, called from both `sendIntentToMain` and `sendIntentDirect`). The main thread never touches console capture directly; it only forwards whatever the intent already carries to `WebSocketHandler.sendBotIntent`.
4. **Round-end residual:** mirroring the Java reference's second drain call, output produced by final-tick event handlers after the round's last intent has already gone out is drained into the intent object immediately after the round-ended final dispatch, so it rides on the next round's first intent instead of being lost.
5. **Restoration:** the installed console override is reverted when the bot stops or disconnects. Unlike Java's process-lifetime `System.setOut`, a non-worker/browser bot shares its JS realm with the host page, so leaving `console.log` rerouted after the bot stops would leak into other code sharing that page.

## Consequences

M-017 implements a small capture wrapper around the four `console` methods, installed at the two sites above and drained at the three points above. Direct writes to `process.stdout`/`process.stderr` that bypass `console` are not captured — a disclosed divergence from the other three APIs' byte-for-byte pipe capture, justified because idiomatic TS bot code does not write to the raw stream and no such stream exists in a browser to capture symmetrically. Multi-argument and non-string `console.log` calls are formatted the way `console` itself would before being appended, so the captured text matches what a bot author sees in a terminal or devtools and the field stays a plain string as the schema requires.
