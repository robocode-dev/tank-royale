---
type: skill-reference
title: Readable references
---

## Readable references

Write the meaning a reader needs in the sentence itself. Explain what a referenced goal, decision, criterion, change or rule means for this work, so the reader can understand the purpose and required action without opening it. A title or link alone is insufficient when the consequence remains implicit.

Use descriptive link text for optional detail and verification. Show the canonical ID as secondary information only when precise identification helps, for example `readable goal name (<goal-ID>)`. Read the referenced source before describing it; do not invent its meaning. A short reference table may help a long document, but the main text must stand on its own. Summarize the relevant effect without copying whole records into a second source of truth.

Apply this method to documents, changelogs, human CLI messages, status updates, reports and acceptance handoffs. Changelogs state the user-visible change and required upgrade action; omit internal decision, criterion and migration IDs. A useful sentence is “Repositories requiring pull requests must select PR acceptance explicitly before upgrading”, rather than naming a migration code and expecting the reader to look it up.

Write a status report, a summary of what was done and a handoff to a person in everyday words: say what happened, what is needed from the reader and what comes next. Name an identifier, a file path or a command only when the reader asks for it or needs it to act, and then say in a few words what it is. Do not use the method's own vocabulary, such as a route, a digest, a plan item, a criterion class or a review pass, without saying what it means; the plain words alone are usually enough. The same goes for the tool's own step names and files, such as a preflight, a ledger or a brief: say what it does or what is wrong, not what the step is called. When the reader must run a command, give that one command and say what it will do. This tightens the rule above for these texts only: in a document an identifier beside a name stays allowed, in a report it appears when the reader needs it.

Keep IDs unchanged when they are the data: frontmatter, links fields, criterion/test tags, command arguments, ID-allocation results and structured evidence or manifests. Explain the purpose of a literal example in the surrounding prose. Preserve pinned historical records and released notes; apply the writing rule to new and changed human-facing prose.

For human reports, show a known local reference's readable name with its ID in parentheses; a status report or handoff follows the everyday-words paragraph above and names the ID only when the reader needs it. Explain unknown, unnamed, ambiguous or foreign references honestly, without a network lookup or guessed content. Distinguish Cliewen's internal rule references from an adopter's unrelated artifact with the same ID. Names aid reading; identity, validation, proof semantics and machine-facing formats remain unchanged.

Before handing off, read the text without following links and check that its purpose, relevant meaning and required action are understandable. Formatter and distribution tests do not prove human comprehension.
