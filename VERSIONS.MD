# Versions

## In progress

#### Bugfixes

- Bot API:
    - Fix for running bots under macOS/bash when bot directory contains
      whitespaces ([#20](https://github.com/robocode-dev/tank-royale/discussions/20)).
    - Fix for `setForward(POSITIVE_INFINITE)` which moved the bot slowly forward with no
      acceleration ([#26](https://github.com/robocode-dev/tank-royale/issues/26)).
    - Fixed issue where the event queue was not disabled if run() method threw an exception, causing the event queue to
      reach its max limit of unprocessed 256 events.
- Bot API for Java:
    - Fixed IllegalArgumentException: -Infinity is not a valid double value as per JSON specification
- GUI:
    - When stopping a battle and starting a new one, the Pause/Resume and Stop buttons were disabled.

## 0.13.2 - Fixing issue #23 - 19-05-2022

#### Bugfixes

- Bot API for .Net:
    - Setting Bot.TargetSpeed did not set the distance to travel.
    - Fixed issue with turning the body, gun, and radar as first and only
      thing ([#22](https://github.com/robocode-dev/tank-royale/issues/22)
      , [#23](https://github.com/robocode-dev/tank-royale/issues/23)).
    - Fixed issue with event queue reaching max after bot has terminated execution
      ([#23](https://github.com/robocode-dev/tank-royale/issues/23)).
- Bot APIs:
    - Added missing documentation about using positive and negative infinity values when moving and turning.

## 0.13.1 - Fixing event queue + country codes - 18-05-2022

#### Bugfixes

- Bot APIs:
    - ~~Fixed issue with turning the body, gun, and radar as first and only
      thing ([#23](https://github.com/robocode-dev/tank-royale/issues/23)).~~
    - Fixed issue with rescanning and interrupting current event handler. Redesigned the event queue.
    - Fixed issue with dangling threads not been stopped (interrupted) correctly.
- Bot API for .Net:
    - Fixed issue with country codes (on macOS) which also caused boot up problems for .Net based bots running under
      macOS ([#20](https://github.com/robocode-dev/tank-royale/discussions/20)).
    - Fixed issue with bots stopping doing actions when battle is restarted. E.g. the Corners sample bot was affected by
      this issue.

#### Changes

- Bot API:
    - Order of priorities was changed, so higher priority values gives higher priority.
- Sample bots:
    - Optimized TrackFire after fixing rescanning issue.

## 0.13.0 - Bot API for .Net 6 - 03-May-2022

#### Changes

- Bot API:
    - Upgraded the .Net version of the bot API for .Net 6.0 as .Net 5.0 is soon EOL.
    - The id was removed from `BotResults`, and `GameEndedEvent` will only return a single `BotResult` containing
      the battle results for the bot (instead of a list containing results from all bots).
    - Added setting and getting 'adjusting radar for body turn'.
    - The `scan()` and `setScan()` methods were renamed to `rescan()` and `setRescan()`.
    - Added `setInterruptible(boolean interruptible)` / `Interruptable = [bool]` to allow restarting an event handler
      while it is processing an earlier event.
- GUI:
    - A tooltip text has been added to the 'Start button' that shows the minimum or maximum number of participants
      required for starting the battle.
- Protocol:
    - Removal of the `speed` field of the `bullet-state` as the speed can be calculated as: `20 - 3 x power`.
      Hence, there is no need to transfer this information over the network.
    - Moved id from `bot-results-for-bot` to `bot-results-for-observer`.
    - The `scan` field on `bot-intent` was renamed to `rescan`.

#### Bugfixes

- Bot API:
    - Blocking bot functions do not stop bot after
      finishing ([#17](https://github.com/robocode-dev/tank-royale/issues/17)).
    - Fixed issue where event queue would overflow with unhandled events when a Bot´s `run()` method has ended.
- GUI:
    - The 'Start game' button is now disabled when the minimum or maximum number of participants is not met.

## 0.12.0 - Single stepping battle - 17-Apr-2022

#### Changes

- GUI:
    - Added 'Next turn' button beside the pause button in with the control panel to useful for single-stepping a
      battle, e.g. when debugging your bot.
    - Improvements to the battle dialog for selecting game type and bots for a new battle.
        - Added button for setting up rules.
        - Added tooltip texts.
- Protocol:
    - Removal of rgb-value type from the protocol of Bot APIs.
- Bot API:
    - The `Color.fromRgb()` has been replaced by `Color.fromString()`.

#### Bugfixes

- Bot API:
    - Make sure the bot terminates (system exit) upon a connection error to prevent dangling bot processes, which
      is usually occurring when terminating the UI, which closes the bot connections.
- GUI:
    - When dragging the battle arena, the graphics were not updated while dragging.

## 0.11.2 - Fix restart issue - 12-Apr-2022

#### Bugfixes

- Battle does not restart ([#10](https://github.com/robocode-dev/tank-royale/issues/10)).
- Bot API did not stop thread when GameAbortedEvent occurred (when restarting).
- When restarting, a two or more games could be started on the server.

## 0.11.1 - Patch for the GUI - 07-Apr-2022

#### Bugfixes

- ConcurrentModificationException could occur for `BotSelectionPanel.updateJoinedBots()`. Could not create a new battle.
- Fixed issue with starting a new battle from the menu with Battle → 'Start Battle' a second time.

#### Changes

- "Restart server" has been renamed into "Reboot Server", and the server and battle is fully stopped, and bots are
  disconnected. Confirmation dialog for rebooting now differ between the reboot is a user action or due to a changed
  server setting.
- Control panel (Pause/Resume, Stop, Restart, TPS) is now always shown when first battle has been started. Various
  improvements were done to the control panel.
- A busy cursor is now (automatically) shown when an operation takes some time to perform.

## 0.11.0 - Initial position feature - 02-Apr-2022

#### Bugfixes

- Fixes for building Java artifacts to deploy to Maven artifactory.
- Bot API:
    - Replaced the environment variable BOT_URL (deprecated) with BOT_HOMEPAGE.
- Bot API for .Net:
    - Added missing public Bot(BotInfo, Uri, string serverSecret) constructor.

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