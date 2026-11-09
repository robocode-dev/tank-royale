---
id: CH-093
type: change
status: open
links: [P-007, CAP-016, UC-005, G-004]
title: The Download bots screen offers the platforms this computer can run and sets up Python when asked
---

# The Download bots screen offers the platforms this computer can run and sets up Python when asked

The Download bots screen lists every platform in the catalog with a tick, whether or not the computer can run its bots. In the maintainer's trial a downloaded Python bot did not start: the Python on the path had no Python Bot API, while `pip` belonged to another Python that had it. The screen gave no warning, and the error only showed when a battle started. This change makes the screen tick only the platforms the computer can run. It tells the person what is missing for the others, and it sets up Python for the download folder when the person asks.

## Why

Practising against downloaded bots is the reason to download them. A bot that cannot start is the first thing a new person meets, and its error names a Python module rather than what to do. Java, C# and TypeScript launchers fetch their Bot API themselves, so for them only the tool itself can be missing: Java, the .NET SDK, or Node.js with npm. Python is different: its bots need the Python Bot API installed beforehand in the Python their launcher runs. Each Python can hold only one version of it, and on many Linux and macOS systems a plain `pip install` into the system Python is refused.

## What changes

- **Each platform is checked when the screen opens**, away from the screen's thread, by asking the tools for their version. The minimum versions come from the bot repository's own templates: Java 25, the .NET SDK 10, Node.js 24 with npm, and Python 3.11. For Python the check also asks whether the download folder's own Python environment exists and holds the Python Bot API. Nothing is installed or changed by checking.
- **Ticked only when it can run.** A platform's box is ticked only when its check passed, so bots the computer cannot run are not offered by default.
- **Ticking an unticked platform opens a short dialog** that says in plain words what is missing:
  - **Python with the environment missing:** the dialog offers **Set up Python for these bots**. It shows the two commands it will run before running them. It makes a private Python environment in the download folder from the Python found, then installs the program's own Python Bot API into it with that environment's `python -m pip`. That Bot API is the copy that ships with the program, in the same version as the program's server. Its dependencies come from the Python package index, so this needs the network. Progress and the result are shown, and a failure shows its details.
  - **Python missing, too old, or unable to make an environment:** the dialog says so and links to the official download. On Debian and Ubuntu it names the `python3-venv` package.
  - **Java, the .NET SDK, or Node.js missing or too old:** the dialog says which version is needed and links to the official download page.
  - **Every dialog also has Show anyway.** It ticks the box without installing anything, because a person may download bots only to read them.
- **Python bots start with the folder's environment.** When the desktop program starts a bot that lies in a folder holding that private environment, it puts the environment first on the bot's path and sets `RUMBLE_PYTHON` to its Python. Launchers that call `python` and launchers that use `RUMBLE_PYTHON`, like the bot repository's templates, then both use the environment. Bots anywhere else start exactly as today.
- **Texts** are resources in every language file, English until translated, like the rest of the screen.

Nothing installs Python, Java, .NET or Node.js. Those need an installer and may need administrator rights, so the screen only links to them. A tool that can fetch a standalone Python without an installer, such as uv, is a possibility for later.

## Challenge the commitment

The riskiest assumption is that a passing version check means the bots will start. A C# bot can target another framework than .NET 10, a Java bot can need curl to fetch its Bot API on first start, and a Python bot written for an older Bot API could behave differently on the program's newer one. The check is therefore an aid, not a promise: Show anyway always exists, and the first real start is still the proof. The manual check in this change starts a downloaded Python bot through the set-up environment and a downloaded Java bot.

The credible alternative is to read each bot's own needs, such as a `requirements.txt` or a project file, and install exactly the version it names. It was not chosen as the first step because the catalog does not carry these needs, the test environment's older bots name none, and one environment per bot multiplies setup time and disk use. If bots turn out to need different Python Bot API versions, per-bot environments can follow.

Putting an environment first on the path of some bots changes how the desktop program starts bots. It is limited to bots in a folder that holds the environment, which only this screen creates, so the person's own bots are unaffected.

## Out of scope

- Installing Python, Java, .NET or Node.js.
- Choosing a Python Bot API version per bot.
- The New Battle group for the download folder, and adding the folder to the bot directories automatically.
- Translations.
