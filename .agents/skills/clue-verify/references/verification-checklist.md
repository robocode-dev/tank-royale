## Verification checklist

Run this verification and review workflow before marking a full Cliewen PR ready for review. Pushing inside that loop needs no verification — every changed turn pushes under the [Review boundary](review-boundary.md) — but the readiness claim does: complete the local checks and agentic review loop before marking the PR ready, and complete the hosted-head check immediately after. Simple work uses only checks relevant to its changed surfaces and does not invoke this skill. When the `clue` CLI exists, `clue validate` performs the mechanical half; until then, check by hand. Never fix a failure by weakening the check.

- [ ] The change uses the correct workspace under [Change routing](change-scope-and-tiers.md).
- [ ] Every artifact touched has frontmatter `id`, `type`, `status`, `links`, and `title`, plus decision `author`/`accepted-by`, constraint `source`/`enforcement`, capability `goal`, and any other type-specific fields.
- [ ] Every `links` entry resolves to an existing ID.
- [ ] The command name and the citation scheme are written in a code span wherever prose names them, so an ordinary sentence using the word is never read as a broken citation; a real citation is prose and carries no backticks.
- [ ] Every reference pointing outside this repository names what it points at: a full address for anything that has one, and the `clue:` identity form for an artifact in another repository's corpus. A bare forge number fails. Citations inside this repository keep their bare ID and relative path.
- [ ] The proposal names a real plan item or explicitly declares the change plan-less.
- [ ] Plan bookkeeping reflects the merge, and no completed plan changed. A change completing a plan's last milestone closes that plan `completed` in this same digest.
- [ ] Every active acceptance criterion satisfies its evidence contract: its identity uses the canonical uppercase segmented-prefix, decimal-digit, and lowercase-suffix grammar, a declared machine proof type has supported Go, JVM, or Cucumber evidence classified by that type and positive/negative direction (or the criterion records `(single-direction)`); JVM evidence carries its AC identity, type, and direction on the same Java or Kotlin executable through literal JUnit method tags or the stable named-executable form; a genuine `Human` criterion is named in the acceptance brief as its proof; an individual not-yet-proven criterion carries `@draft`; and an unannotated legacy criterion has its one supported reference.
- [ ] Every `/docs/**` folder has a README; index blocks list every sibling artifact and no deleted file.
- [ ] The change was checked against each constraint listed in `docs/constraints/README.md`'s index (including verifiable quality bars), naming any that do not apply.
- [ ] [Repository-local conventions](repository-local-conventions.md) satisfy their contract.
- [ ] Diagrams use the clearest renderable form: prefer Mermaid, use ASCII art where it is clearer, and retain SVG where neither is adequate.
- [ ] The full-change workspace is absent after digest; `main` never contains `/changes/`.
- [ ] Every decision satisfies [Decision records](decision-records.md), including routing, timeless content, provenance, objections, and pending approval signatures.
- [ ] The current commit received a pass with no blocking findings under the [Agentic review loop](agentic-review-loop.md); every blocking repair after an earlier clean pass triggered a new pass, and any advisory findings left open are named in the verification evidence.
- [ ] The final handoff identifies the review mode (`context-isolated` or `in-context fallback`), reviewed commit, and number of review passes run.
- [ ] Every review of an existing PR names its hosted head; actionable findings are unresolved hosted conversations where supported, or the enforcement gap is disclosed and the PR is reported not merge-ready.
- [ ] Every intended edit, including each review fix, is committed and `git status --porcelain` is empty.
- [ ] Every working turn on this change that changed anything ended by pushing the change branch, and the PR existed as a draft from first publication rather than appearing only at readiness.
- [ ] `git merge-base HEAD origin/main` equals `origin/main` after fetching; no other change workspace is visible on this branch.
- [ ] When the PR is marked ready, the current branch is its head branch, its head SHA equals local `HEAD`, and the reported verification ran against that commit.
- [ ] The branch and hosted PR satisfy the [Review boundary](review-boundary.md).
