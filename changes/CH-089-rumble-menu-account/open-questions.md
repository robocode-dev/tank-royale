---
id: CH-089-open-questions
type: open-questions
status: open
links: [CH-089]
title: The menu switch, English-only texts and the environment
---

## Should the Rumble menu be hidden until it is whole?

A released desktop program would otherwise show a menu with a single working item. The alternative is to show it at once, which lets people sign in early but promises more than there is.

Recommendation: hide it behind a developer switch (a system property or an environment variable) and remove the switch when the menu is complete.

## Are English texts alone enough for now?

The desktop program has resource files for English, Catalan, Danish and Spanish. A new screen's texts will change while the screens are built, and translating each version costs more than translating a finished one.

Recommendation: write English only and let the other languages fall back to English. Translate when the menu is whole.

## How does the screen pick the environment?

The command line uses the variable `RUMBLE_ENVIRONMENT`, with the public Rumble as the default. The public Rumble has no sign-in App yet, so the screen can only sign in against the test environment for now.

Recommendation: the same variable, the same default, and on the public Rumble a plain "signing in is not available yet" instead of a button. A selector in the screen is not worth building while one environment cannot sign in.
