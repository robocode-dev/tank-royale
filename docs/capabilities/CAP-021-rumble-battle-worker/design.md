---
id: CAP-021-design
type: design
status: draft
links: [CAP-021, CAP-001, CAP-016, P-007]
title: Design notes for CAP-021 (rumble-battle-worker)
---

# CAP-021 design

The host and the worker are two processes. The host owns credentials and decides what to run; the worker owns nothing but a job. They meet only through the messages of the unpublished module `lib:rumble-protocol`, which neither the host library nor the worker needs to share any other code to read.

## Messages

Every message is a JSON object on one line with an integer `schemaVersion`, currently 1, the same convention as the other Rumble documents. A reader refuses a version it does not know with the message "uses schema version N, but this program understands version M. Update the program." and never reads part of a message. The messages are Kotlin types in `lib:rumble-protocol`, not JSON schemas in the `schema` folder, because those schemas generate the Java and C# code of the Bot APIs and describe the battle protocol with bots, not this internal channel.

A job names the job, the engine pin as a behavior version, the game type and number of rounds, and the participating bots as name, version, content hash and the folder the host has already placed and verified. The worker trusts the folder; verifying the hash is the host's task before it writes the job. The worker's answers are events: started, one result per battle with each bot's rank and score components, refused with a stable code, failed with a stable code, and finished. The exit code repeats the outcome for a caller that reads no events.

## The pin

The behavior version is an integer owned by the server and reported in its handshake. The Battle Runner can be told to require a version and then refuses to start a battle on a mismatch. The worker uses that existing check with the job's pin. It is not the same thing as the message schema version.

## The boundary

The worker's dependencies are the runner and the message module. A build check and a source scan keep it that way. A separate process only helps if the host starts it without credentials in its environment, with no home folder and no container socket; that is the host's half and arrives with the host.
