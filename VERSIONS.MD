# Versions

## In progress

#### Bugfixes

- Bot API: Replaced the environment variable BOT_URL (deprecated) with BOT_HOMEPAGE.
- Bot API for .Net: Added missing public Bot(BotInfo, Uri, string serverSecret) constructor.

## 0.10.0 - Bot API improvements - 24-Mar-2022

#### Bug fixes:

- Fixed MyFirstBot.java (sample bot) not running.
- Various bug fixes were found in the Bot APIs, and missing get/set methods were added.
- Selected bots were not removed from the New Battle dialog when bots are disconnecting.
- Booter did not transfer environment variables to bot processes from GUI.

#### Changes:

- Moved documentation from robocode.dev to [GitHub Pages](https://robocode-dev.github.io/tank-royale/)
- Changed the default server port from 80 to 7654 to avoid the use of `sudo` before `java` command (ports above 1023
  does not need `sudo`)
- Bot APIs: Moved constants to a Constants class.
- Bot APIs: Introduced a Color class for representing colors.
- Bot API for .Net: Some Set/Get methods were refactored into properties for e.g. speed, turn rates, and colors.
- Bot API for .Net: Lots of optimizations were made.

#### Improvements:

- Bot handshake has been extended to include server secret with the bot-handshake and the BaseBot class has been
  extended for setting this as input parameter in the constructor.
- Server secret has been split into *controller/observer secrets* and *bot secrets*
    - It is now possible to supply multiple server secrets
- Client is disconnected when sending a wrong secret to the server.
    - Bot API now writes out status-code and the reason for disconnecting when it is due to a wrong secret.

## 0.9.12 - 23-Jan-2022

- First alpha version was released! :)
