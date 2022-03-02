# Versions

## 0.9.12 - 23-Jan-2022

- First alpha version released! :)

## 0.9.13

Bugfixes:

- Fixed MyFirstBot.java (sample bot)
- Booter did not transfer environment variable to bot processes from GUI

Changes:

- Moved documentation from robocode.dev to [GitHub Pages](https://robocode-dev.github.io/tank-royale/)
- Changed the default server port from 80 to 7654 to avoid use of `sudo` before `java` command (ports above 1023 does
  not need `sudo`)

Improvements:

- Bot handshake has been extended to include server secret with the bot-handshake and the BaseBot class has been
  extended for setting this as input parameter in constructor.
- Server secret has been split into *controller/observer secrets* and *bot secrets*
    - It is now possible to supply multiple server secrets
- Client are disconnected when sending a wrong secret to the server.
    - Bot API now writes out status code and reason when disconnected due to a wrong secret.