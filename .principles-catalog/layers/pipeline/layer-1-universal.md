# Pipeline Stack — Layer 1: Universal Principles

These principles are **always active** for any CI/CD pipeline definition file, regardless of platform.

Note: `SIMPLE-DESIGN-REVEALS-INTENTION`, `CODE-CS-DRY`, `CODE-CS-KISS`, `CODE-CS-YAGNI`, `CODE-DX-NAMING`, and `ARCH-DECISION-RECORDS` are activated by the universal section of `artifact-types.yaml` and apply to all stacks including pipeline.

| ID | Title | Summary |
|----|-------|---------|
| CODE-RL-IDEMPOTENCY | Idempotency | Pipeline runs must be idempotent: re-triggering the same pipeline on the same inputs should produce the same result without unintended side effects. |
| CODE-AR-PIPELINE-CHANGES | Pipeline changes through code | Infrastructure and environment changes must flow through the same version-controlled pipeline as application changes — no manual interventions in CI/CD. |
| PIPELINE-MINIMAL-PERMISSIONS | Minimal permissions | Pipeline jobs should run with the minimum permissions required to complete their task; avoid broad IAM roles, wildcard scopes, or inherited credentials with excess access. |
| PIPELINE-NO-SECRETS-IN-LOGS | No secrets in logs | Secrets, tokens, and credentials must never appear in pipeline logs or job output; use masked variables and secrets management integrations. |
| PIPELINE-REPRODUCIBLE-BUILDS | Reproducible builds | The same source code, dependency set, and build toolchain must produce an identical artifact every time, regardless of when or where the build runs. |
| PIPELINE-ENVIRONMENT-ISOLATION | Environment isolation | Each pipeline stage must execute in an isolated context with its own workspace, credentials, and environment variables; stages must not share mutable state. |
| PIPELINE-FAIL-FAST-PIPELINE | Fail-fast pipeline | When a pipeline stage fails, subsequent dependent stages must not execute; abort early and report the failure clearly. |
