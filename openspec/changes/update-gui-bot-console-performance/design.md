## Context

The Tank Royale GUI is Swing-based. Bot event/log output is shown in panels like `BotEventsPanel` that inherit
`BaseBotConsolePanel` / `ConsolePanel` and ultimately render ANSI-formatted content through `AnsiEditorPane` (a
`JEditorPane` using `AnsiEditorKit` and a `StyledDocument`).

Users report that continuous logging of bot events makes the GUI slower over time and can appear to freeze. The
suspicion is that Swing text components / StyledDocument styling updates scale poorly with huge amounts of formatted
text and frequent incremental updates.

## Goals / Non-Goals

- Goals:
    - Keep the GUI responsive even under heavy bot logging.
    - Avoid unbounded CPU/heap growth from console rendering.
    - Preserve ANSI coloring/styling for common sequences.
- Non-Goals:
    - Full terminal emulation (cursor addressing, reflow, etc.) unless already supported and required.
    - Rewriting the GUI away from Swing.

## Investigation checklist

### Reproduction workloads

- Real log capture: save output from a battle where bots log frequently.
- Synthetic generator:
    - Append N lines/sec with random ANSI colors.
    - Include the worst cases: very long lines, many short style runs, frequent reset codes.

### Measurements

Collect baseline numbers and keep them in this document.

- UI responsiveness:
    - EDT stall durations while appending.
    - Scroll latency when the document is large.
- Throughput:
    - Max sustainable append rate before visible stutter.
- Resource usage:
    - CPU usage during steady-state appending.
    - Heap size over time; retained objects after clearing/closing.
    - GC pause frequency under workload.

### Profiling evidence

Capture and summarize:

- Hot methods during appending.
- Evidence of excessive repaint/layout.
- Time spent in ANSI parsing vs document mutations vs painting.

## Decisions (to be filled after investigation)

- Decision: Implement bounded retention and batched updates in `ConsolePanel`.
    - Bounded retention: Limit the maximum number of characters in the `StyledDocument` to 10,000 (configurable via
      `ConfigSettings`). This prevents memory leaks and keeps document operations fast.
    - Batched updates: Use a background buffer to collect log lines and flush them to the EDT at a fixed interval (e.g.,
      every 100ms). This reduces the number of `insertString` calls and EDT overhead.
- Alternatives considered:
    - Background ANSI parsing: While beneficial, batched updates already address the main bottleneck of EDT flooding.
      Parsing is relatively fast compared to Swing document mutations.
    - Alternative rendering model: Too complex for the current requirements and risks breaking existing functionality.

## Candidate mitigation strategies

(Select based on measured bottlenecks.)

1) Bounded retention (ring buffer) [SELECTED]

- Keep only the last X lines / chars.
- Pros: hard bound on memory + document size.
- Cons: truncates history; needs UX (export/copy, setting).

2) Batched updates + repaint coalescing [SELECTED]

- Coalesce frequent small appends into a single append every N ms.
- Pros: reduces document churn and repaint frequency.
- Cons: slightly delayed display.

3) Background ANSI parsing

- Parse ANSI into tokens off the EDT; apply minimal document updates on EDT.
- Pros: reduces EDT time spent parsing.
- Cons: adds concurrency complexity.

4) Alternative rendering model

- Use a more efficient text component / custom renderer with virtualized lines.
- Pros: can scale much better.
- Cons: larger refactor; riskier.

5) Degraded mode / fallback

- When output exceeds a threshold, switch to plain text mode or limit ANSI styling.
- Pros: predictable performance.
- Cons: reduced fidelity.

## Risks / Trade-offs

- Truncation can surprise users; must be communicated and/or configurable.
- Background processing must preserve ordering and avoid Swing threading violations.

## Migration plan

- Prefer a conservative default (e.g. bounded retention with high limit + export option) if the current behavior is
  unbounded.
- Consider a feature flag or setting for power users.

## Open questions

- Should limits be global settings or per-panel?
- Should the same mitigation apply to all console panels (events, stdout/stderr, etc.)?
- What ANSI sequences are considered in-scope for “correct styling” in the GUI?

