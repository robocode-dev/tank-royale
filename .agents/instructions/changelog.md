# Changelog Guidelines

<!-- KEYWORDS: changelog, CHANGELOG.md, release notes, version, user-visible -->

## Audience

The changelog is written **for bot developers** — people who write bots using the Bot API.
It is **not** an internal maintenance log for the maintainer.

## What to include

- New features and capabilities a bot developer can use
- Bug fixes that affected observable bot behaviour
- Breaking changes with a migration path
- GUI or server changes that affect how developers run or debug their bots

## What to exclude

- Implementation details: thread models, internal timers, protocol internals, library choices
- Test additions or refactoring
- Build system, CI, or tooling changes with no user-visible effect
- Internal error fixes that were never visible to bot developers

## Tone and level of detail

Write from the bot developer's point of view:
- **Good:** `Fixed bots disconnecting from the server after ~80 seconds while paused at a debugger breakpoint.`
- **Bad:** `When a bot's JVM is fully suspended by a debugger, all threads — including the WebSocket client's I/O thread — are frozen, so the bot cannot respond to the server's periodic WebSocket ping frames. The server's connection-lost detection (60-second timeout) then closes the connection.`

One sentence per entry is usually enough. Add a second sentence only when the behaviour change requires the developer to act (e.g. a breaking change or a migration step).

## Format

```markdown
## [X.Y.Z] - YYYY-MM-DD - Short release title

Optional one-paragraph summary for significant releases.

### ✨ Features
### 🐞 Bug Fixes
### 🔧 Changes
### ⚠️ Breaking Changes
```

- Group entries under the component where the change is visible to the developer:
  `Server:`, `GUI:`, `Bot API (Java, .NET, Python):`, `Sample bots:`, etc.
- Reference the GitHub issue number when one exists: `#123: …`
- Omit sections that have no entries for the release.
- No `### 🧪 Tests` section — test changes are invisible to bot developers.
