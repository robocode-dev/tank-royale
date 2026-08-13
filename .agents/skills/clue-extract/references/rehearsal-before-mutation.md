## Rehearsal before mutation

After the extraction's full change is proposed, begin with a mandatory report-only pass. Write the rehearsal report under `/changes/<CH-xxx-slug>/`; do not change the target source corpus, Cliewen `/docs` corpus, tests, routing, or hosted state.

The rehearsal report inventories source formats and entry points, proposed artifact mappings, preserved and minted IDs, confidence and reversal cost, test-purpose work, instruction conflicts, planned deletions, and named plan doors. An unresolved conflict becomes an `open-questions.md` entry and stops before mutation.

The rehearsal also writes a pinned source manifest under the change workspace: the exact source revision and location read, and one proof-class, direction, and evidence-location row for every classified reference a criterion has, or one declared exclusion with reason, or one `draft`/`human`/`retired` disposition with a readable justification, its particular `disposition-source-location`, and its own existing target milestone as `plan-door`. This is the same manifest `clue parity` compares against the corpus it derives — write it once here rather than reconstructing it afterward.

The rehearsal also writes a pinned carrier inventory under the change workspace: the same source revision and location, every source-repository path the migration will delete (`deleted-paths`), and one row per operational carrier found — an `instruction`, `workflow`, `freshness-input`, `registry`, `link`, or `diagram-asset` — naming its `id`, `kind`, and `source-path`, plus either its mapped `target-path` and content `fingerprint`, or an explicit `blocked: true` with a `reason` when no target exists yet. This is the same inventory `clue carriers` reconciles against the corpus it derives — write it once here rather than reconstructing it afterward.

Only explicit human direction begins the existing full extraction change's mutate phase. That phase digests the rehearsal into the durable extraction report under `/docs/analysis`, then performs the accepted conversion; the ready PR deletes both the transient change workspace and the parallel source corpus.
