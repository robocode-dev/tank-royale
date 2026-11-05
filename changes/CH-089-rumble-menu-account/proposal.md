---
id: CH-089
type: change
status: open
links: [P-007, M-029, M-038, ARCH-027, ARCH-028, ADR-053, ADR-054, CAP-016]
title: Add the Rumble menu to the desktop program, starting with the GitHub account screen
---

# Add the Rumble menu to the desktop program, starting with the GitHub account screen

The libraries and the command line can sign in, register, check, send and follow a bot, but nothing in the desktop program shows it. The desktop program uses the Rumble libraries in its own process, which is decided. This change builds the first piece of the Rumble menu: the menu itself and the GitHub account screen from the reviewed mockups, because everything else in the menu (sending a bot, contributing battles) needs a signed-in account.

## Why

Signing in is the entry to every screen that sends something to the Rumble. It also exercises the whole path the later screens use: the desktop program calling the libraries, doing the work away from the screen's own thread, and showing the library's typed answers in plain words. A small first screen proves that path before the larger screens depend on it.

## What changes

- **A Rumble menu** between Config and Help, with one item for now, GitHub account. The rest of the items in the mockup arrive with their own screens.
- **The GitHub account screen**, built from the reviewed mockup and its state diagram: not signed in, waiting for the person in the browser with the code, the countdown and a button that opens the page and copies the code, signed in with the account name and client ID and the signed-out and sign-out controls, and the failure rows of the mockup's table (the code expired, cancelled or denied, GitHub unreachable, access revoked, the saved sign-in cannot be used, this computer cannot keep it private).
- **The screen's behavior is separate from Swing.** The states, the transitions and the words live in a plain class that can be tested without a window; the dialog only shows its state and passes button presses on. All work that touches GitHub or the disk runs away from the screen's event thread, and a result reaches the screen through its normal event queue.
- **Signing in also registers.** After the code is accepted the account is registered with the Rumble and the client ID is shown, as the mockup says.
- **The environment.** The desktop program uses the public Rumble unless the variable `RUMBLE_ENVIRONMENT` names another, the same rule as the command line. The public Rumble has no sign-in App yet, so on it the screen says in plain words that signing in is not available yet and offers nothing to press.
- **The menu is hidden until it is whole.** A released desktop program must not show a menu with one half-built screen, so the Rumble menu is shown only when the system property or environment variable that switches it on is set. It is removed when the menu is complete, in a later change.
- **Words.** All texts go in the desktop program's resource files. They are written in English; the other languages fall back to English until they are translated.
- **Documents.** The participation design and the mockups' status note record what is built, and the plan gets a progress note. New acceptance criteria are written before the code.

## Challenge

**A screen that talks to GitHub must never freeze the program.** Every call to the library runs away from the event thread, and a test with a library that blocks proves that the screen's own thread is not the one that waits. Cancelling and closing the dialog stop the waiting.

**The token lives in the desktop program's memory.** That is the accepted consequence of the decision to use the libraries in the same process. The screen never shows a token, never copies one to the clipboard (only the short code), and never writes one to a log.

**The first proof against the real GitHub needs the maintainer.** The state logic is proved against fakes and the real sign-in code is already proved by the command line, but the real dialog opening in a real browser, with a real code, is a manual check in the test environment that only the maintainer can do. The handoff will say if it has not been done.

**Half a menu is a risk, and the switch is the answer to it.** The switch is a developer's tool and not a feature; it is documented only in the design and removed when the menu is whole.

**Swing can only be partly tested here.** The dialog itself is checked by building and opening it in a test environment by hand. The behavior class carries the tests.

## Not in this change

No sending, checking or following a bot, no downloading bots, no contributing battles, no rankings, and no menu items other than the account screen. No fetching of the command line, which only contributing battles need. No translations other than the English texts.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any criterion, code or document is changed.
