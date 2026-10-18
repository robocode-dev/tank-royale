---
type: skill-reference
title: Readable references
---

## Readable references

Write the meaning a reader needs in the sentence itself. Explain what a referenced goal, decision, criterion, change or rule means for this work, so the reader can understand the purpose and required action without opening it. A title or link alone is insufficient when the consequence remains implicit.

Use descriptive link text for optional detail and verification. Show the canonical ID as secondary information only when precise identification helps, for example `readable goal name (<goal-ID>)`. Read the referenced source before describing it; do not invent its meaning. A short reference table may help a long document, but the main text must stand on its own. Summarize the relevant effect without copying whole records into a second source of truth.

Apply this method to documents, changelogs, human CLI messages, status updates, reports and acceptance handoffs. Changelogs state the user-visible change and required upgrade action; omit internal decision, criterion and migration IDs. A useful sentence is “Repositories requiring pull requests must select PR acceptance explicitly before upgrading”, rather than naming a migration code and expecting the reader to look it up.

Keep IDs unchanged when they are the data: frontmatter, links fields, criterion/test tags, command arguments, ID-allocation results and structured evidence or manifests. Explain the purpose of a literal example in the surrounding prose. Preserve pinned historical records and released notes; apply the writing rule to new and changed human-facing prose.

For human reports, show a known local reference's readable name with its ID in parentheses. Explain unknown, unnamed, ambiguous or foreign references honestly, without a network lookup or guessed content. Distinguish Cliewen's internal rule references from an adopter's unrelated artifact with the same ID. Names aid reading; identity, validation, proof semantics and machine-facing formats remain unchanged.

Before handing off, read the text without following links and check that its purpose, relevant meaning and required action are understandable. Formatter and distribution tests do not prove human comprehension.
