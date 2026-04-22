---
description: Build and deploy all sample-bot zip files into the target folder. Use when the user runs /deploy-sample-bots [TARGET_DIR].
argument-hint: "[TARGET_DIR]  (default: C:/Code/bots)"
allowed-tools: Bash
version: 1.0.0
authors: Flemming N. Larsen (https://github.com/flemming-n-larsen)
---

# Deploy Sample Bots

You are executing the Tank Royale sample-bot deployment workflow. Follow these phases exactly, in order. **If ANY step fails (non-zero exit code), STOP immediately with an ERROR — never continue to the next step.**

Announce each step clearly before executing it.

## Phase 1 — Pre-flight Check

Verify that `deploy-sample-bots.sh` exists in the current working directory.

- If missing: print `"❌ ERROR: deploy-sample-bots.sh not found. Please run from the Tank Royale repository root."` and **STOP**.
- If found: print `"✅ deploy-sample-bots.sh found"`.

## Phase 2 — Run

Determine the target directory:
- If the user supplied an argument (`$ARGUMENTS`), use it as the target directory.
- Otherwise omit it (the script defaults to `C:/Code/bots`).

Run:
```
bash ./deploy-sample-bots.sh $ARGUMENTS
```

Stream the output to the user. If the exit code is non-zero, print `"❌ Deployment failed — see output above."` and **STOP**.

## Phase 3 — Report

Print a one-line summary:
```
✅ Sample bots deployed to <TARGET_DIR>
```

Where `<TARGET_DIR>` is the directory used (from `$ARGUMENTS` or the default `C:/Code/bots`).
