# Versions

## 0.11.1

#### Bugfixes

- ConcurrentModificationException could occur for `BotSelectionPanel.updateJoinedBots()`. Could not create a new battle.
- Fixed issue with starting a new battle from the menu with Battle → 'Start Battle' a second time. 

#### Changes

- "Restart server" has been renamed into "Reboot Server", and the server and battle is fully stopped, and bots are
  disconnected. Confirmation dialog for rebooting now differ between the reboot is a user action or due to a changed
  server setting.

## 0.11.0 - Initial position feature

#### Bugfixes

- Fixes for building Java artifacts to deploy to Maven artifactory.
- Bot API: Replaced the environment variable BOT_URL (deprecated) with BOT_HOMEPAGE.
- Bot API for .Net: Added missing public Bot(BotInfo, Uri, string serverSecret) constructor.

#### Changes

- Implemented initial position feature:
    - Add an `initialPosition` field in the JSON file for your bot with a value like `50,50,90` to request a starting
      coordinate at (50,50) with the bot heading toward 90°.
    - A `--enable-initial-position` (or the short version `-I`) must be set to enable the initial positions feature on
      the server. If initial position is not enabled, the bot will start at a random position.
    - Initial positions can be set from the menu with Config → Debug Options → Enable initial start position.
    - The Target.json file of the Target sample has been updated with an `initialPosition` to demonstrate this feature.
- Updated the SpinBot sample bot for C# to demonstrate the use of `BotInfo.FromConfiguration(IConfiguration)`.
- The config file named `misc.properties` has been renamed into `config.properties` and the `server-url` setting was
  moved to the `server.properties` file.
- When the server settings are changed on the GUI, the user will be asked if the server should be rebooted to let the
  changes take effect.

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

[BotInfo.FromConfiguration(IConfiguration)]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.BotInfo.html#Robocode_TankRoyale_BotApi_BotInfo_FromConfiguration_Microsoft_Extensions_Configuration_IConfiguration_