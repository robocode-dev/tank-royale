---
id: AN-018
type: analysis
status: active
links: [CAP-014, M-022, ADR-050]
title: Four-language starter verification before live catalog submissions
---

# Four-language starter verification

## Consumer and evidence boundary

This record supports the milestone for reproducible bot submissions in every official language. [rumble-bots PR 11](https://github.com/robocode-dev/rumble-bots/pull/11) supplies Java, C#, Python and TypeScript starters, a JSON-only TwinDuel example, dependency validation and instructions. [Tank Royale PR 286](https://github.com/robocode-dev/tank-royale/pull/286) supplies the author guide, capability design and the decision permitting the official API's declared runtime dependencies. This is prepared template verification on 2026-10-19, not the milestone's live first-bot and version-update submission evidence. No new bot or owner entry has been added to the public catalog by this change.

The external candidate is `21a347756d3ca8878dc5bf90be5373d5a28e4a7e`, based on bot-catalog commit `c5bea8e9e3100b4241c5e54f44b7033d27c91f53`. The official APIs are pinned to 1.3.1 to match the current client engine. The TypeScript API's [published package manifest](https://registry.npmjs.org/@robocode.dev%2ftank-royale-bot-api/1.3.1) declares the `ws` peer; its published Node runtime adapter loads that peer when connecting. The starter pins `ws` 8.22.0 and commits the npm-generated lockfile. The template runtime restores dependencies outside its submitted source tree.

## Template checks

The repository-owned command is `RUMBLE_TEMPLATE_SMOKE=1 python -m unittest discover -s tests -v`, with Python, Java 25, .NET SDK 10 and Node.js 24 available. Tests copy the actual templates into temporary catalog trees, rename source/config/launcher identifiers, run the real submission validator and generate a catalog. They cover accepted individual entries, a JSON-only true-twin team, version supersession, required lockfiles, an accepted API runtime peer, unrelated direct and locked libraries, missing peers, aliases, unofficial tarball resolutions, malformed peer metadata and generated dependency directories. Smoke tests compare the source-tree hashes before and after execution.

Local Linux verification uses the published `ghcr.io/robocode-dev/rumble-client@sha256:ff82d1d3394fde74f62bb24e8d116f1e8b36a2363e368670fdc90b35780a325a` runtime image. Git archives preserve the templates' committed executable modes. The container is read-only with capabilities dropped and no-new-privileges; an executable temporary fixture filesystem represents a normal checked-out source tree, while C# output remains on the image's separate `noexec` temporary filesystem. C# uses `dotnet run` with the native app host disabled, so generated DLLs can be loaded there without changing mount security.

Smoke means API/source loading, the expected `RUMBLE_SMOKE_READY <BotName>` marker and a successful exit inside the validator's 20-second limit. It does not prove a server connection. Separate one-round battles through the image's Battle Runner 1.3.1 and behavior-version-1 engine completed for all four renamed starters against Fire 1.0.0: JavaScout, CsharpScout, PythonScout and TypescriptScout each joined and appeared in completed results. The final legacy-engine shutdown logged a rejected-executor task after results in one earlier run; it did not prevent completed results, and that engine issue was not investigated as a template defect.

Native Windows smoke using paths containing spaces passed for the Java, Python and TypeScript `.cmd` launchers. Python used an isolated environment with the pinned API. Native C# could not run because the host has SDK 8, while the starter targets .NET 10; its Linux launcher, API load and real battle were verified in the .NET 10 container. No .NET SDK was installed into the user's machine for this check. VitePress built the updated author guide successfully.

The new runtime-peer scenario remains draft in this corpus because external executable evidence is not yet registered in the repository-relative evidence manifest, consistently with the existing catalog criteria. The external tests carry executable-bound criterion/type/direction names, and their workflow runs them; this record does not claim central manifest coverage or change the evidence methodology.

## Live continuation

Keep the milestone unfinished. After the template and policy changes are human-merged and the guide is published, use the documented instructions to submit a new named bot from an eligible owner account, observe its merged active catalog entry, then change its source and bump the version in a subsequent PR. Record both PRs, publication workflow runs, generated source hashes and the superseded/active identities. Keep those live observations separate from the template fixture's local version-update test. A non-member account with unused slots is suitable; do not infer ownership evidence from a maintainer-created PR.

The existing server-connected smoke/public-opening wording in the bot repository has not been removed or claimed satisfied by this work; resolving that gate remains part of the later public-entry-point milestone. Local verification logs are retained under `C:/Code/tank-royale/.git/m022/` for continuation; the tests and public instructions are the durable reproduction path.
