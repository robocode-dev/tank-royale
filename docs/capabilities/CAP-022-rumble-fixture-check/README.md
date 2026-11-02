---
id: CAP-022
type: capability
goal: G-001
status: draft
links: [G-001, P-007, CAP-014, CAP-021]
title: Rumble fixture check
---

# CAP-022 — Rumble fixture check

Let a maintainer run one documented command from Tank Royale that exercises the Rumble pieces with fixtures only, and get a result a program can read and a short report a person can read, with secrets removed automatically, so that every later Rumble feature adds its cases to one place instead of inventing its own way to run and report.

The command is the Gradle task `rumbleFixtureCheck`. It runs the offline checks of the Rumble modules and the Python checks of the environment and reset tool, groups them into named cases, and never runs a live check or touches a public resource. A green run does not claim that the sign-in App is installed on a real fork or that a real account can sign in; the report says so every time. Real outside-account experiments stay separate evidence.
