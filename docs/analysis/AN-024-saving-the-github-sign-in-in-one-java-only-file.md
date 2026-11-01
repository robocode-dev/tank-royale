---
id: AN-024
type: analysis
status: active
links: [P-007, M-029, ARCH-027, AN-023]
title: Can the GitHub sign-in be saved in one Java-only file that works the same on every operating system
---

# Saving the GitHub sign-in in one Java-only file

## Question

After a person signs in to GitHub, the program should keep the sign-in between runs so they are not asked every time. The participation design said to use the Windows credential store. The maintainer wants one solution that is the same on Windows, macOS and Linux, with no operating-system-specific code. The unknown is whether a single file in the Tank Royale user folder, encrypted with standard Java only, can be written, protected and read back the same way on all three, and what protection it really gives.

## What was set up

A throwaway program, [CredentialFileSpike.java](../../scripts/rumble/credential-file-spike/CredentialFileSpike.java), written for Java 11 and compiled with `--release 11` so it uses no newer library call. It stores a fake token and never touches a real one or the network. It puts everything in a `credential-spike` folder inside the same user folder that `UserDataDirectory` in the common library chooses for each system, and removes the folder again.

The design under test:

- One file in the user folder. It is created so that only its owner may use it, and the program reads the rights back afterwards.
- The content is encrypted with AES-256 in GCM mode. The key is derived with PBKDF2 from the user name, the home folder, the operating system name and a fixed text built into the program. No host name is used, because it changes with the network.
- The file is written to a temporary file and moved into place in one step.
- Only the refresh token would be stored, never the access token.

## Observations

| System | Java | File system | Result |
|---|---|---|---|
| Windows 11, 64-bit | 25 (Temurin) | NTFS | All 16 checks passed in one process, and all passed again when the file was written by one process and opened by a second. The file had a single permission entry for its owner, kept it after the move, and the atomic move worked. |
| Ubuntu 24.04 under WSL2 | 11 (Temurin) | ext4 | Same: all checks passed in one process and across two processes. Rights read back as `rw-------`, also after the move. |
| Ubuntu under WSL2 with the folder on a mounted Windows drive | 11 | 9p | The two permission checks failed as intended: the rights read back as `rwxrwxrwx`, so the file could not be restricted to its owner. Everything else passed. |
| macOS on Intel | not run | not run | See the next section. |

Every system also refused a file with one changed byte, a file opened by a different user name or home folder, and a truncated or empty file, and none of those crashed. The key derivation took about 55 to 95 milliseconds.

## macOS was not tested

The maintainer has an old Intel Mac but chose not to run the test, reasoning that macOS will work if Linux does. That is a reasonable inference and it is recorded here as an inference, not an observation: macOS offers the same Java permission calls as Linux, its standard file systems keep the owner-only mode and move files atomically, and the encryption comes from the same Java library. The remaining risk is a home folder on a network share or another file system that ignores permissions, and the read-back check below catches that case on any system. If it ever fails on a Mac, the effect is that the program does not save the sign-in and asks again, not that anything is exposed.

## What this means

- The design works with the same Java code on Windows and Linux. The system differences (a permission string on Linux and macOS, an access list on Windows) are handled by the Java library behind one call.
- The program must read the rights back after creating the file and refuse to save when they are not owner-only. The mounted-drive case shows this is needed.
- Any problem with the file (damaged, other user, other machine, rights wrong) must end the same way: nothing is saved or used and the person signs in again. No fallback that puts a token in an ordinary file.

## What the protection is, and is not

The key comes from the user and the machine, not from the operating system's own secure store, so it protects against the file being copied: a zipped or backed-up folder, or a copy taken to another computer or account, cannot be opened. It does not protect against a program running as the same user on the same computer, which can derive the same key. This is the limit of any approach without the operating system's store, and it is also what a plain owner-only file gives. The sign-in token is a GitHub App token, so even a stolen one only reaches what the App may do and can be revoked in GitHub's settings. The design text must say this plainly and must not call the file strongly encrypted.

## Rejected

- **The operating system's own stores** (Windows Credential Manager or DPAPI, the macOS Keychain, Linux Secret Service): stronger, but each needs its own code and either a native-call library or an external program per system, which the maintainer ruled out.
- **A key built into the program as the only protection**: the program is open source, so the key is public. The key here is mixed with the user and the machine, which is what makes a copy useless elsewhere.
- **A passphrase the person types**: works everywhere but defeats staying signed in.
- **A plain unprotected file**: no reason to leave the file open to other accounts when the same call can restrict it.

## Consumers

The change that saves the sign-in in the shared Rumble library uses this design, and its design text replaces the earlier wording about the Windows credential store.
