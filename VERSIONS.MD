# Versions

## 0.9.12 - 23-Jan-2022

- First alpha version released! :)

## 0.9.13

Bugfixes:

- Fixed MyFirstBot.java (sample bot)
- Booter did not set environment variables on bot processes

Changes:

- Moved documentation from https://robocode.dev to https://robocode-dev.github.io/
- Changed default server port from 80 to 7654 to avoid use of `sudo` before `java` command (ports above 1023 does not
  need `sudo`)

Improvements:

- Server secret has been split into *controller/observer secrets* and *bot secrets*
    - It is now possible to supply multiple server secrets
- Bot handshake has been extended to include server secret with the bot-handshake and the BaseBot class has been
  extended for setting this as input parameter in constructor.
- Client are disconnected with a status code and reason when server secret is wrong.
    - Bot API for Java writes out status code and reason when disconnected.