---
id: CH-067-open-questions
type: open-questions
status: resolved
links: [CH-067]
title: How much of the bot repository to fetch
---

## Is one whole-repository archive acceptable for fetching bots?

The public bot repository's archive is about 1.7 MB today, and one archive serves any number of bots. That is fine for a download of dozens of bots, and the fetch sits behind an interface so it can be replaced. The recommendation is to accept the whole-repository archive now, and to revisit it when the archive passes about 50 MB or a download of a single bot becomes common. The alternative is to build a per-bot fetch now, which needs the GitHub API with its rate limit and a request per file, or a Git client the program does not otherwise require.

Resolved by the maintainer on 2026-10-31: accept one whole-repository archive now, and revisit it when the archive passes about 50 MB or downloading a single bot becomes common.
