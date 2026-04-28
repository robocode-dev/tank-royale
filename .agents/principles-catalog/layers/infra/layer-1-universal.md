# Infra Stack — Layer 1: Universal Principles

These principles are **always active** for any infrastructure-as-code file, regardless of tool or target platform.

Note: `SIMPLE-DESIGN-REVEALS-INTENTION`, `CODE-CS-DRY`, `CODE-CS-KISS`, `CODE-CS-YAGNI`, `CODE-DX-NAMING`, and `ARCH-DECISION-RECORDS` are activated by the universal section of `artifact-types.yaml` and apply to all stacks including infra.

| ID | Title | Summary |
|----|-------|---------|
| CODE-AR-INFRASTRUCTURE-AS-CODE | Infrastructure as Code | All infrastructure must be defined in version-controlled, reviewable code — no manual changes to production environments. |
| CODE-AR-IMMUTABLE-INFRASTRUCTURE | Immutable Infrastructure | Replace infrastructure rather than mutating running instances; immutability eliminates configuration drift and makes deployments reproducible. |
| CODE-AR-COMPOSABLE-MODULES | Composable modules | Infrastructure should be organized into small, focused, reusable modules with clear interfaces rather than monolithic configurations. |
| CODE-RL-IDEMPOTENCY | Idempotency | Every infrastructure operation must be safe to run multiple times with the same result; re-applying the same configuration should produce no unintended changes. |
