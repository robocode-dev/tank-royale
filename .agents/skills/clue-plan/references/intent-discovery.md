## Intent discovery

Use when a repository has no usable vision. `clue` reports whether one exists and what its provenance is; it never interviews and never infers. Interpretation is yours, and accepting meaning is the human's.

### Greenfield: interview, then summarize

A user may start from one informal sentence — "I want to build a service that helps local running clubs organize weekly runs" — and that is enough to begin.

Ask only questions whose answers would change the vision, the initial goals, the system boundary, or a candidate use case. Ask a few at a time and let each round follow from the last; a fixed questionnaire asked in one block is not this. Do not make the user learn Cliewen's vocabulary to answer — ask about their product, and do the translation yourself. Do not demand complete requirements before anything useful can start.

Across the rounds, find out who the actors are, what outcome each wants, what is inside the boundary and what is deliberately outside it, what would count as succeeding, what constrains the direction, what is being assumed, and which end-to-end interactions matter enough to be use cases. Stop as soon as another question would not change what you would write.

Then summarize what you understood in plain language and ask the user to correct or confirm it. Only after that does any of it become accepted intent. Write the vision, the initial goals, and the use cases you recommend — with your reason for each, and for any you considered and are not recommending.

If the user explicitly asks you to draft from what little they have said, do it. What changes is how much you write, never how honestly you label it: the vision stays `draft` with `provenance: inferred`, every uncertainty appears as a stated assumption or an open question, and nothing you guessed is presented as something they told you.

### Brownfield: read first, then ask what the repository cannot answer

Inspect the repository before asking anything. The README and other documentation, architecture and design material, any existing Cliewen corpus, source and public APIs, tests and acceptance scenarios, command-line help and user interfaces, configuration, package metadata, deployment definitions, existing ADRs, PDRs, and IDRs, the change history available inside the repository, and examples and integration tests.

Then produce a concise draft that cites the repository sources behind each material claim, keeps what you observed separate from what you concluded, and names the contradictions, stale documents, and gaps you found rather than smoothing them over. Ask only what the repository genuinely cannot answer.

**Code demonstrates what a system does; it cannot establish why the product exists or what anyone wants.** Never derive strategic intent from implementation structure. Where sources disagree and the disagreement affects durable meaning, record the conflict and ask for a decision instead of choosing the convenient reading. A partially documented repository is the normal case, and missing documentation is a reason to mark uncertainty, never a reason to invent certainty.

Present the proposed vision and any candidate use cases for correction or acceptance. Recommend a use case only where the existing capabilities genuinely fail to explain an outcome; a brownfield system's behaviour is mostly ordinary, and most of it needs no journey written down.
