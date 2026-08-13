## Upgrade workflow

Use when a repository already uses Cliewen and the human wants to find out whether, or bring it up to, a newer release. This is a route into a reviewed repository change, never a background update or authority to merge.

1. Run `clue latest`. It determines whether a newer release is available and, when one is, prints the installation route for the machine it is running on. Do not reproduce an installation command here: one distributed skill cannot know the user's platform. If the release list cannot be reached, explain that Cliewen cannot tell and stop; do not call the repository current.
2. If a newer release is available, read that release's notes, including its `### Migration` section. Identify the coordinated corpus, generated-skill, and CI-caller changes before proposing any repository write.
3. Ask the human whether to upgrade now or later. Do nothing to the repository until they explicitly choose now. A later answer is complete: report the available release and stop without creating a branch, changing a file, or opening a pull request.
4. On an explicit yes, make the repository green and create a branch through its normal review process. Move the binary and repository together: preview `clue migrate`, resolve every finding and notice — including those no command may repair — and apply only the complete, preflighted plan with the required reversal-cost choice. Keep the managed skills, the thin caller, and any repository corpus obligations on the chosen release together.
5. Verify the upgraded repository, commit the complete candidate, run its required checks, and mark the upgrade's pull request ready under the review boundary. Never merge it: the repository's human merge boundary accepts the upgrade.
