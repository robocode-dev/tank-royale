# Config Stack — Layer 1: Universal Principles

These principles are **always active** for any configuration file, regardless of format or environment.

Note: `SIMPLE-DESIGN-REVEALS-INTENTION`, `CODE-CS-DRY`, `CODE-CS-KISS`, `CODE-CS-YAGNI`, `CODE-DX-NAMING`, and `ARCH-DECISION-RECORDS` are activated by the universal section of `artifact-types.yaml` and apply to all stacks including config.

| ID | Title | Summary |
|----|-------|---------|
| 12FACTOR-03-CONFIG | Store config in the environment | Separate configuration from code; use environment variables so the same build can run in any environment without modification. |
| CONFIG-NO-HARDCODED-SECRETS | Never hardcode secrets | Secrets, passwords, API keys, and tokens must never appear in configuration files committed to version control. Use secrets management tooling or environment injection. |
| CONFIG-SCHEMA-VALIDATION | Validate configuration against a schema | Configuration should be validated at startup against a schema so that missing or malformed values fail fast with a clear error. |
| CONFIG-SCHEMA-FIRST | Declare a schema for every configuration file | Every config file references a schema so that editors, linters, and CI tools can validate it before the application runs. |
| CONFIG-EXPLICIT-OVER-CONVENTIONAL | Prefer explicit keys over magic conventions | Every key that influences behaviour must be named and present; implicit framework defaults must be documented rather than relied upon silently. |
| CONFIG-ENVIRONMENT-PARITY | Keep config structure identical across environments | Config schema and key set must be the same in dev, staging, and production; only values differ; code must never branch on environment name. |
| CONFIG-EXPLICIT-DEFAULTS | Declare defaults for every optional key | Every optional config key has an explicit, documented default in the schema; production values must be set consciously, never silently defaulted. |
| CODE-CS-DRY | Don't repeat knowledge | Configuration values should be defined once and referenced elsewhere. Duplicate values drift out of sync and create hard-to-diagnose environment differences. |
