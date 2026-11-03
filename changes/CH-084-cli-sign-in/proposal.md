---
id: CH-084
type: change
status: open
links: [P-007, M-029, ADR-053, CAP-024]
title: Sign in, sign out and register from the Rumble command line, and make it pleasant to use
---

# Sign in, sign out and register from the Rumble command line, and make it pleasant to use

The command line can read and import, but it cannot sign a participant in, and using it shows rough edges. The library already does the sign-in work: it asks GitHub for a sign-in code, checks whether the person has approved it, saves the sign-in in one protected file per environment, renews it, and registers the account through the result repository. This change makes those steps commands and gives the program what a person and an agent need to find their way. It adds no command that uploads a bot. It also records how the program will reach people, fetched by the desktop program at first use, which is built separately.

A review of the first version of this proposal found that it broke the decision it claimed to follow and left many outcomes undefined. This version settles them. The maintainer agreed on 2026-11-03 to refine the decision and to make the sign-in behave the same with and without JSON.

## Why

Sending a bot, running a session and reading a pull request all need a signed-in, registered account, so none of them can be commands before this. Running the current program also showed what makes it awkward: four lines of Java warnings on the help text and on bad input, `rumble nope --json` answering only `rumble: bad input` and no JSON, no `--version`, `Sign-in: missing` with no word on what to do, and help that does not say where to begin. Each is cheap to fix now and expensive once people and agents depend on the answers.

## The refined decision

The accepted decision says that the person starts and approves the sign-in, and that an agent never registers an account. Taking part through an agent is pointless if the agent cannot start the sign-in or register the account it was given. The decision is therefore revised: an agent may start the sign-in, hand the code to its person, and register the account that person approved. It still never approves a sign-in, never creates an account, and never acts for an account nobody approved on this computer. Because the maintainer has approved this wording in conversation, the record is marked as verified by them.

## What changes

**The commands.** All of them work the same with and without `--json`; the option changes only the form of the answer.

- `rumble login` first checks that the state folder can be kept private and that the environment has a sign-in App, so nothing is shown that cannot succeed. It then asks GitHub for a code and answers at once with the code, the address and when the code runs out. The secret device code is saved in a protected file and is never shown; the short code the person types is not secret on its own and can be shown again.
- `rumble login --finish` checks once and answers. With `--wait <seconds>` it keeps checking until that time has passed, never faster than GitHub allows. The next permitted check and GitHub's current pace are saved with the waiting code, so an agent that calls again too soon gets a "wait N seconds" answer without GitHub being contacted. On approval it saves the sign-in, finds the account name and answers with it.
- `rumble login --wait <seconds>` does both in one call for a person at a terminal: it shows the code, then waits. It exists only in text form, because a program would not see the code until the end, and is refused with `--json`.
- A second `login` while a code is still valid returns the same code instead of replacing the one the person may be typing. Two `--finish` calls take turns on a lock, so a code is used once and the later call answers from the saved result. `login` when already signed in says so and does nothing; changing account means `logout` first.
- `rumble logout` removes the saved sign-in, the waiting code, the account name and the registration for that environment. It creates nothing where nothing was. If a file cannot be removed it says so and exits 1, never reporting success while a sign-in is still usable. It also says plainly that this removes the sign-in from this computer only, and that access at GitHub stops when the person revokes the App in their GitHub settings.
- `rumble register` checks the sign-in App first, renews the sign-in and registers the account as the library does, saving the client ID. Running it again gives the same account and client ID with `alreadyRegistered` true. The registration workflow closes every registration issue, refused or not, so running it again after a refusal would open a new issue every time. It therefore reads the account's earlier closed registration issues and reports the earlier refusal without opening another; `--retry` opens one new issue, for a person who has fixed the reason.
- `rumble status` still only reads. It now shows the signed-in account, reports a registration only when it belongs to that account, and gives the next step.

**What goes with the sign-in.** The account name is saved beside the sign-in, since the sign-in file holds no name and a registration for another account could otherwise be shown beside it. Signing in as another account removes that environment's old registration. A sign-in that GitHub approves but that cannot be saved is answered as not saved with exit 1, and the renewal learns to say when the new sign-in could not be kept instead of reporting plain success.

**Exit codes and outcomes.** Every answer carries an `outcome` word, the environment and, where one exists, the next step, so a program never reads prose.

| Exit | Meaning | Outcomes |
|---|---|---|
| 0 | Done | `started`, `signed-in`, `already-signed-in`, `signed-out`, `registered`, `already-registered` |
| 1 | Failed; trying again later may work | `failed`, `not-saved`, `not-removed` |
| 2 | Refused or bad input | `bad-input`, `no-sign-in-app`, `not-private`, `refused` |
| 3 | A sign-in is needed | `no-sign-in`, `sign-in-unusable`, `sign-in-refused`, `no-waiting-code`, `code-expired`, `code-denied` |
| 4 | Not finished; run the same command again | `waiting`, `registration-pending` |

Exit code 4 is new. `waiting` gives the seconds to wait, and `registration-pending` the address of the issue. Without it, an agent cannot tell a person who has not typed the code yet from a failure.

**Making it pleasant to use.**

- The next step everywhere: `status`, `login`, `register` and every exit 3 answer end with the command to run next, as a field in JSON and a `Next:` line in text. `rumble status` becomes the place to start when in doubt.
- Bad input answers in the form asked for: with `--json`, one document with outcome `bad-input` and the real message, also for an unknown command or environment; without, a one-line message instead of the whole help text.
- No Java warnings and a smaller program: the warning comes from the terminal library the help text uses, and that library and its native access code take up about 3 MB of a program people will download. The change tries the core of the command line library without the terminal library, which removes the warning at its source. If the help text suffers or the build refuses it, the jar instead declares native access in its manifest, which only removes the warning. Either way standard error stays empty in normal use.
- `rumble --version` and `-v` work as in Tank Royale's other programs; the `version` command stays for the details.
- The environment is chosen the same way everywhere: `-e` or `--environment` on every command that uses one, or the variable `RUMBLE_ENVIRONMENT`, and otherwise the public Rumble, which is what nearly every participant wants. A developer sets the variable once. Every answer names the environment, in text too, so a mix-up is visible. No command accepts two environments.
- Help that gets you going: the main help says where to begin, shows the sign-in steps as an example and explains all the exit codes; texts say `Sign-in: none saved` rather than `missing`.

**Answers keep their rules.** No token, device code, home folder or absolute path. The cleaning of answers learns to keep one value: the participant's public GitHub account name, which today becomes `<user>` when it equals the operating system user name, as `status` already shows. When the two are equal the answer does reveal the user name, which is accepted because it is the public account name the person signed in with.

**Criteria, written before the code.** Three existing criteria become false and cannot change meaning, so they are retired and replaced: the exit codes (3 is now used and 4 is new), the no-secrets rule (the account name is shown) and the documentation criterion (the refined decision). New criteria cover each command in both forms, every outcome in the table, the pace kept between processes, the same code from a second `login`, two `--finish` calls taking turns, a sign-in approved but not saved, a private folder checked first, logout touching one environment and creating nothing, register's earlier refusal and `--retry`, a changed account, bad input in JSON, no warnings on standard error, and the choice of environment. Tests use a fake GitHub and a fake clock and call the commands in process, and real processes check closed input, empty standard error and the manifest.

**Documents.** The refined decision, the participation design's command contract, the command line capability and the plan note.

## Challenge

The main weak point is the code saved between two runs. The alternative, one blocking call that prints the code on standard error, avoids the file but breaks the rule that standard error holds nothing a program needs, and an agent reading only the final answer never shows the code in time. The file is protected and short-lived, and only the device code in it is secret.

The second is the default environment. A required option would rule out a developer reaching public by forgetting it, but costs every participant a flag on every command, and the answer naming the environment is what really prevents a mix-up. While public has no sign-in App, its sign-in commands are refused anyway. When the App exists, a forgotten option can at worst open one registration issue on public under the developer's own account, which is visible and harmless. This reverses what I said after the review, that the option would be required, and the maintainer accepted the public default on 2026-11-03.

The third is the live proof. The commands are proved only against fakes. A real round trip through them in the test environment is a manual step for the maintainer, stated as not done in the handoff. The registration workflow exists only in the test environment today, which limits what register can do on public regardless.

## Not in this change

No staging, pre-check, sending or reading of a pull request, no battle running, no change to the sign-in rules at GitHub, the registration workflow or the moderation, and no agent with an account of its own. Later candidates that would also add comfort, left out to keep this change reviewable: a `doctor` command that checks Java, the state folder and the connection to GitHub, and shell completion, which only makes sense once there is a short command to type. Nothing is published.

**Fetched by the desktop program at first use.** The maintainer decided on 2026-11-03 that the program is not carried inside the desktop program, so that the desktop jar does not grow and the server and the booter do not lie in it several times. Instead the desktop program fetches it from the GitHub release with the same version, the first time it is needed, and keeps it in the user's Tank Royale folder. This change writes that direction into the participation design and the plan, as the open question records; the fetching itself, and attaching the program to a release, are a separate change. `rumble version --json` already gives the version the desktop program will need to check.

## Publication and acceptance

This branch is local and not pushed, and has no pull request, because local acceptance is the accepted policy and the maintainer has not asked for publication. The work serves the simple-participation goal; the repository states no vision. The maintainer reviews this proposal before any criterion, code or document is changed.
