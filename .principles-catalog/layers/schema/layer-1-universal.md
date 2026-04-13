# Schema Stack — Layer 1: Universal Principles

These principles are **always active** for any schema or contract file, regardless of format or target system.

Note: `SIMPLE-DESIGN-REVEALS-INTENTION`, `CODE-CS-DRY`, `CODE-CS-KISS`, `CODE-CS-YAGNI`, `CODE-DX-NAMING`, and `ARCH-DECISION-RECORDS` are activated by the universal section of `artifact-types.yaml` and apply to all stacks including schema.

| ID | Title | Summary |
|----|-------|---------|
| CODE-API-BACKWARD-COMPATIBILITY | Maintain backward compatibility | Changes to a published schema must not break existing consumers; additive changes are safe, removals and renames are breaking changes. |
| SCHEMA-SELF-DESCRIBING | Make schemas self-describing | Every field, type, and enum value should carry enough documentation or naming clarity that a consumer can understand its purpose without consulting external documentation. |
| SCHEMA-FIELD-OPTIONALITY | New fields must be optional | New fields added to a published schema must be optional with sensible defaults; adding a required field is a breaking change that forces lockstep deployment of all consumers. |
| SCHEMA-NO-POLYMORPHIC-BLOBS | No untyped blobs | Schema fields must not use untyped containers (`Any`, `Object`, `bytes`, `additionalProperties: true`) to carry structured data — use explicit typed schemas so data can be validated and evolved. |
| SCHEMA-ENUM-EVOLUTION | Enums must be safe to extend | Every enum must include an unknown/unspecified sentinel value, and every consumer must handle unrecognised variants gracefully — otherwise adding a new variant is a breaking change. |
| CODE-DX-NAMING | Name things by what they represent | Field and type names should reveal their semantic meaning, not their technical implementation. Consistent naming conventions across a schema reduce cognitive load for consumers. |
