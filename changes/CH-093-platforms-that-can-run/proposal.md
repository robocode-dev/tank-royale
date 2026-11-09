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

- **Each platform is checked when the screen opens and when the list is fetched again**, away from the screen's thread, by asking the tools for their version. The versions are not written in this program. They are read from the Rumble's one list of supported platform versions: the runtime file in the Rumble client's latest release, from which its container image is built, its README block is refreshed and its own runtime check reads. Today that list says Java 25, .NET 10, Python 3.14 and Node.js 24. A platform passes when the computer has at least that major version. When the Rumble upgrades, the screen follows without a new release of this program. A bot's own `programmingLang` field is free text that nothing checks, so it is not used. For Python the check also asks whether the download folder's own Python environment exists and holds the Python Bot API. Nothing is installed or changed by checking. An environment that no longer starts, because Python was upgraded or the folder was moved, counts as missing, and the screen offers to set it up again.
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

## Human evidence

For the scenario where the maintainer sets up Python for downloaded bots and starts them in a battle.

- **Observer:** the maintainer, with the author of this change.
- **Conditions:** the desktop program built from this branch, with the jar shrunk, was started with the Rumble menu switched on for the test environment. The download folder was an empty temporary folder that was also added as a bot directory. The computer had Java 25, and at first only Python 3.13 first on the path. The maintainer then put the already installed Python 3.14.2 first on the user path, and the program was started again.
- **Observation, first part:** with Python 3.13 the screen ticked JVM and showed Python as too old, with its bots hidden. Ticking Python opened the dialog. It said that the computer has Python 3.13 and that the Rumble supports Python 3.14 or newer. Its download button opened python.org, and after Close the Python box was still unticked.
- **Observation, second part:** with Python 3.14 the screen showed Python as needing setting up. The dialog showed the two commands and offered the setup. The screen showed the setup running. It ended with a green "Success: Python is set up for the bots in this folder", and Python was ticked. The environment held the program's Python Bot API 1.4.0 and its dependencies, about 86 MB, mostly the type checker mypy. Downloading all 18 active bots succeeded, each with its license. The Java bot Corners and the Python bot Orbit then played a battle together.
- **Also seen:** the default download folder moves when the temporary folder becomes the first bot directory. Long command lines in the dialog break inside words and are shown without quotes. The dialog has a fixed size with empty space, and its text has a darker background with a visible caret.
- **Not observed:** which Python actually ran Orbit was not looked at while the battle ran. A separate check outside the battle, with the environment first on the path as the program sets it, showed that a launcher calling `python` gets the environment's Python with the Python Bot API 1.4.0. The computer's own Python 3.14 also has an older Python Bot API, 1.1.0, in its user folder. A computer without Java, .NET or Node.js, a Debian system without python3-venv, and a failed setup were only proved by tests.
