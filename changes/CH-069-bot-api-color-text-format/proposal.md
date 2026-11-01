---
id: CH-069
type: change
status: open
links: [CAP-006, C-003, ADR-038]
title: Make all four Bot APIs read and write colour text the same way
---

# Make all four Bot APIs read and write colour text the same way

A bot can send a colour to a teammate inside a team message, and a bot can read colours as text from the server and from its own settings. The four Bot APIs (Java, C#, Python and TypeScript) do not agree on what colour text they accept or write, so a colour sent by a Java bot can make a C# teammate fail, and the other way around. This change makes them agree, tests the agreement in the shared test definitions that all four APIs already run, and records the rule as acceptance criteria. This is plan-less: it serves the constraint that the four Bot APIs behave the same, with Java as the reference.

## Why

A check on 2026-11-01 found that the C# colour converter is used when a team message carries a colour: with the converter made to fail, the two C# tests that send colours in team messages failed and the other 301 passed. A small probe was then run in each of the four APIs. What each writes into a team message, and which colour texts each accepts:

| | Writes `red` and `half-transparent blue` | `#F00` | `#F00F` | `#FF0000` | `#FF0000FF` | `#ff000080` | Spaces around |
|---|---|---|---|---|---|---|---|
| Java (reference) | `#ff0000` and `#0000ff` (transparency dropped) | read | refused | read | refused | refused | read |
| C# | `#FF0000FF` and `#0000FF80` | refused | read | refused | read | read | refused |
| Python | `#FF0000` and `#0000FF80` | read | refused | read | read | read (main colour class); refused (separate helper) | read (main colour class) |
| TypeScript | `{"value":4278190335}` and `{"value":65408}` (a plain object, not text) | read | refused | read | refused | refused | read |

The C# column is for its separate JSON path in team messages; its general colour text helper reads three and six digits like Java. Python has two readers that disagree: its main colour class reads three, six and eight digits and its helper reads three and six. Capital and small letters were read by every reader that read the digits. No API reads the four-digit form from every reader, and no two APIs agree on what a team message carries. So a colour sent between two bots on different APIs fails to read, or reads wrong, in most pairings.

Teams may now mix languages, so a colour in a team message must mean the same thing whichever language sent it. The shared test definitions cover colour construction and the colour text used for the bot's own settings, but nothing covers colour text in team messages or what each API accepts.

## What changes

- One rule for reading colour text in all four APIs: the text must start with `#`, may have surrounding spaces, and may be written with capital or small letters. Four forms are accepted: `#RGB`, `#RGBA`, `#RRGGBB` and `#RRGGBBAA`. A short form means each digit is doubled, and a form without transparency is fully opaque.
- One rule for writing a colour into a team message in all four APIs: `#RRGGBB` when the colour is fully opaque and `#RRGGBBAA` otherwise, so transparency is kept. This matches what each API's own hex colour function already returns for drawing. See the open questions about changing Java's current writer.
- The colour settings a bot sends to the server stay as they are: three-digit-pair RGB text without transparency, because that is what the server and the shared schema define. The shared schema's colour description is updated to say that the server protocol uses RGB and that the Bot APIs also read the other three forms. Its pattern is written in a form that does not work as a pattern, but it feeds code generation, so it is left alone and reported as a loose end.
- New test cases in the shared test definitions, run by the Java, C#, Python and TypeScript test runners alike, for reading each of the four forms in both letter cases, for refusing text that is not a colour, and for the text written into a team message. The runners gain the small helper methods this needs.
- Acceptance criteria for the colour text rule in the protocol capability, next to its existing team-message criteria, with positive and negative cases.
- A line in the bot developers' changelog for each API that changes behaviour.
- The stash left over from the earlier C# attempt is dropped when this is done, since this change replaces it.

## Challenge

The assumption most likely to undermine this work is that a colour in a team message is read only by the same API that wrote it, so the differences never bite. That is no longer true once teams may mix languages, but if nobody uses colour in team messages across languages, the rule costs four API changes for little gain. That check has been run (see the table above), and it shows the opposite: most pairings fail today, so the work is warranted.

A second failure that every test could miss: older versions of the APIs. A bot built with an older API that reads only three or six digits will fail on a transparent colour written by a newer API, as `#RRGGBBAA`. Opaque colours, which are almost all colours bots send, stay in the six-digit form that old readers accept. The changelog says so, and the change does not try to repair old versions.

## Not in this change

No change to the server, the GUI or the colour text a bot sends in its settings. No new colour names or colour spaces. No change to how colours are drawn. No release is made by this change.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication of this change. The maintainer reviews this proposal before any code or document is changed.
