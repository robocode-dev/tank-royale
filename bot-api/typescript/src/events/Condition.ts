/**
 * The Condition class is used for testing if a specific condition is met.
 * Subclass and override test(), or pass a callable to the constructor.
 */
export class Condition {
  readonly name?: string;
  private readonly callable?: () => boolean;

  constructor(nameOrCallable?: string | (() => boolean), callable?: () => boolean) {
    if (typeof nameOrCallable === "function") {
      this.callable = nameOrCallable;
    } else {
      if (nameOrCallable !== undefined) this.name = nameOrCallable;
      if (callable !== undefined) this.callable = callable;
    }
  }

  test(): boolean {
    if (this.callable) {
      return this.callable();
    }
    return false;
  }
}
