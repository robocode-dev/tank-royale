type CapturedMethod = "log" | "info" | "warn" | "error";

const CAPTURED_METHODS: CapturedMethod[] = ["log", "info", "warn", "error"];

function formatArg(arg: unknown): string {
  if (typeof arg === "string") return arg;
  if (arg instanceof Error) return arg.stack ?? `${arg.name}: ${arg.message}`;
  if (typeof arg === "undefined") return "undefined";
  try {
    return JSON.stringify(arg) ?? String(arg);
  } catch {
    return String(arg);
  }
}

function formatArgs(args: unknown[]): string {
  return args.map(formatArg).join(" ");
}

/**
 * Captures `console.log`/`info`/`warn`/`error` output into stdOut/stdErr buffers, the TypeScript
 * analogue of the recording streams the Java, .NET and Python Bot APIs install over the process's
 * real standard-output/error pipes (IDR-005). Capture is additive: every call still forwards to
 * the real `console` method.
 */
export class ConsoleCapture {
  private stdOutBuffer = "";
  private stdErrBuffer = "";
  private installed = false;
  private readonly originals = new Map<CapturedMethod, (...args: unknown[]) => void>();

  install(): void {
    if (this.installed) return;
    this.installed = true;
    for (const method of CAPTURED_METHODS) {
      const original = console[method];
      this.originals.set(method, original);
      const target: "stdOut" | "stdErr" = method === "warn" || method === "error" ? "stdErr" : "stdOut";
      console[method] = (...args: unknown[]) => {
        original.apply(console, args);
        this.append(target, formatArgs(args));
      };
    }
  }

  restore(): void {
    if (!this.installed) return;
    for (const [method, original] of this.originals) {
      console[method] = original;
    }
    this.originals.clear();
    this.installed = false;
  }

  private append(target: "stdOut" | "stdErr", text: string): void {
    if (target === "stdOut") {
      this.stdOutBuffer += text + "\n";
    } else {
      this.stdErrBuffer += text + "\n";
    }
  }

  /** Returns and clears the buffered output since the last drain. */
  drain(): { stdOut: string | null; stdErr: string | null } {
    const stdOut = this.stdOutBuffer.length > 0 ? this.stdOutBuffer : null;
    const stdErr = this.stdErrBuffer.length > 0 ? this.stdErrBuffer : null;
    this.stdOutBuffer = "";
    this.stdErrBuffer = "";
    return { stdOut, stdErr };
  }
}
