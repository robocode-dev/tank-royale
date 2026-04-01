# Audit Scope

**Summary:** Records which principles are fully excluded or partially limited in automated `/dot-audit` scans, and why.

The `/dot-audit` command reviews a codebase against active principles using LLM reasoning and, where available, grep pre-scans. Not every principle can be meaningfully verified by inspecting a codebase snapshot — some require runtime data, git history, org-structure knowledge, or are purely process-based activities. This document records which principles are excluded or limited, and why.

Principles in either table remain fully valid for `/dot-prime` (educational reference) and for human code review. Only `/dot-audit` skips or partially applies them.

---

## Fully excluded

These principles cannot produce a reliable finding from a codebase snapshot alone. The `/dot-audit` command skips them entirely.

| Principle ID | File | Reason |
|---|---|---|
| ARCH-CONWAYS-LAW | `principles/arch/conways-law.md` | Violations require knowing team and org structure. The codebase contains no authoritative record of ownership, reporting lines, or communication patterns. |
| CODE-PF-PROFILE-FIRST | `principles/code/pf/profile-first.md` | The core violation — optimizing without profiling data — is a process event. No artifact in the codebase proves or disproves whether profiling occurred before a change was made. |
| SEC-ARCH-THREAT-MODELLING | `principles/sec-arch/threat-modelling.md` | Threat modelling is a design-time process. Whether it was performed, and what it covered, cannot be determined from code alone. |

---

## Partially limited

These principles have at least one code-detectable violation, but some aspects of the principle require information that is not present in the codebase. The `/dot-audit` command applies them with the restrictions noted.

| Principle ID | File | What is detectable | What is not detectable |
|---|---|---|---|
| CODE-CS-BOY-SCOUT | `principles/code/cs/boy-scout.md` | New/changed lines (via `$GIT_DIFF`) introducing TODO/FIXME markers or magic numbers in files that were touched | Whether the contributor made every possible cleanup improvement (subjective); no-diff cases where git history is unavailable |
| CODE-TS-TEST-FIRST | `principles/code/ts/test-first.md` | Absence of corresponding tests for production code | Whether tests were written *before* production code (requires git history) |
| 12FACTOR-10-DEV-PROD-PARITY | `principles/12factor/10-dev-prod-parity.md` | Env-conditional branches; hard-coded local connection strings | Whether dev and prod use different backing services (requires deployment config) |
| 12FACTOR-09-DISPOSABILITY | `principles/12factor/09-disposability.md` | Missing SIGTERM handlers; ack-before-process patterns; startup migrations | Actual startup time (requires running the app) |
| DDD-UBIQUITOUS-LANGUAGE | `principles/ddd/ubiquitous-language.md` | Generic technical names; intra-codebase naming inconsistencies | Whether naming matches domain-expert vocabulary (requires a domain glossary) |

---

## Contributing new principles

When submitting a new principle, mark its audit scope using the optional `**Audit-scope:**` metadata field. See `principles/TEMPLATE.md` for the field format and `CONTRIBUTING.md` for the auditability requirement.
