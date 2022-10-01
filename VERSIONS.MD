# Versions

## In process

#### ⚡ Changes

- [#43](https://github.com/robocode-dev/tank-royale/issues/43): Possibility to identify bot from Process to Tick:
    - Went back to boot process `pid` instead of using the `boot id`.

#### 🚀 Improvements

- Sample bots: Improved the Fire sample bot by removing unnecessary code.

#### 🪲 Bug Fixes

- Server:
  - The fire assistant was not always activated.
  - [#45](https://github.com/robocode-dev/tank-royale/issues/45): Server is kidnapping CPU cycles
    - Game is now aborted, when the last bot participating in a battle is leaving (disconnecting).
    - Timer used for turn timeout was not stopped, when a battle had ended/aborted.
    - Freeing heap memory used for storing game state when game is ended/aborted.

## 📦 0.17.2 - Added sounds - 20-Sep-2022

#### 🚀 Improvements

- GUI/Sounds: [#4](https://github.com/robocode-dev/tank-royale/issues/4): Fire and Explosion Sounds:
    - Added feature to play sounds to the GUI application.
    - Added Sound Options to configure sounds.
    - Note that sound files are distributed independently besides the gui application from the
      [sounds releases](https://github.com/robocode-dev/sounds/releases)
- GUI: Moved the Dismiss button on the Bot Root Directories configuration down under the Add and Remove buttons, and
  adjusted the size of the dialog.

#### 🪲 Bug Fixes

- GUI: When the Bot Directories on the Select Bots for Battle window is updated, all selections are now being cleared.

## 📦 0.17.1 - Identifying a bot from Boot-up to Ticks - 15-Sep-2022

#### 🚀 Improvements

- [#43](https://github.com/robocode-dev/tank-royale/issues/43): Possibility to identify bot from Process to Tick:
    - ~~Reintroduced `boot id` to replace boot process id (`pid`)~~
    - Added `sessionId` and `bootId` to these schemas: `bot-handshake`, `bot-state-with-id`, and `participants`

#### 🪲 Bug Fixes

- Bot API:
    - The `onWonRound(WonRoundEvent)` was not triggered as soon as the round has ended.
    - The blocking turn methods for turning the body, gun, and radar invoked `go()` twice internally making the turning
      turn in "step" and take the double amount of time.
    - Some events already handled were not removed from the event queue.

## 📦 0.17.0 - Introduction of process id - 06-Sep-2022

The _boot id_ concept introduced in release 0.16.0 has been rolled back and the _pid_ is used with the Booter again.
However, a new concept is introduced to replace the _boot id_, which is the _process id_.

When a client (bot, controller, observer) is connecting to a server, the server will generate a unique session id and
send to the client via the `server-handshake`. The session id is used to uniquely identify the running instance of a
client, and the client _must_ send back the session id when sending its handshake to the server, e.g. a `bot-handshake`.

The _session id_ is replacing the _boot id_ as the boot id is only available with bots being booted, and which might
only be unique locally, but across multiple systems. With the _session id_, all clients have a single and unique id.

#### 🚀 Improvements

- GUI:
    - Improved sorting of the bot directories list to be case-insensitive.

#### 🪲 Bug Fixes

- Server:
    - The `server-handshake` was missing the name and version.

## 📦 0.16.0 - Introduction of boot id - 31-Aug-2022

~~The _boot id_ has been introduced to make it easier to identify which bots have booted locally. Previously, only the
host and port were available on the server side, making it hard to identify the bots started from a controller.
Now the boot id uniquely identifies a booted bot.~~

~~Previously, a process id (pid) was used to uniquely identify a bot being booted, but the pid has been replaced by the
new boot id.~~

- ~~Schema:~~
    - ~~The `bot-handshake` has been extended to include the `bootId` when the bot is booted locally. This field may be
      empty when a bot has not been booted by the Booter.~~
- ~~Booter~~:
    - ~~Now generates a unique boot id for each booted bot, and sends this to the bot via the new `BOOT_ID` environment
      variable.~~
- ~~Bot API~~:
    - ~~Takes the boot id from the `BOOT_ID` environment variable and includes this within the bot `bot-handshake` whens
      it is sent to the server.~~

#### 🪲 Bug Fixes

- Bot API:
    - Fixed `waitFor(Condition)` so it executes before checking the condition the first time.
- Server
    - Fixed major bug where the firepower was not limited at the server side.
    - Adjusted gun to fire at gun direction for a new turn.
- GUI:
    - Issue with reading huge bot list from the booter when reading from stdin.

## 📦 0.15.0 - Added fire assistance - 17-Aug-2022

#### 🚀 Improvements

Fire assistance:

- Added fire assistance known from the original Robocode game. Fire assistance is useful for bots with limited
  aiming capabilities as it will help the bot by firing directly at a scanned bot when the gun is fired, which is a
  very simple aiming strategy.
- When fire assistance is enabled the gun will fire towards the center of the scanned bot when all these conditions
  are met:
    - The gun is fired (`setFire` and `fire`)
    - The radar is scanning a bot _when_ firing the gun (`onScannedBot`, `setRescan`, `rescan`)
    - The gun and radar are pointing in the exact the same direction. You can call `setAdjustRadarForGunTurn(false)` to
      align the gun and radar and make sure not to turn the radar beside the gun.
- When calling `setAdjustRadarForGunTurn(true)` then fire assistance will automatically be disabled, and when calling
  `setAdjustRadarForGunTurn(false)` then fire assistance will automatically be disabled.
- Added new `setFireAssist(enable)` to enable and disable the fire assistance explicitly.

GUI:

- Updated various hover help texts for the dialog used for starting battles.

Bot info:

- The `gamesTypes` field is no longer required with the JSON config file of a bot. When it omitted, the bot will be able
  to participate in _all_ game types. When defined, the bot will only be able to participate in battles with the game
  type(s) defined within this field.
- The `gameTypes` field has been removed from the sample bots, as all sample bots can participate in any game type.
  At least for now.

#### 🪲 Bug Fixes

- GUI & Booter:
    - Fixed major bug as the GUI and booter did not filter out bots based on their selected game types.
    - Fixed issue with parsing JSON bot info file for the optional fields `description`, `countryCodes`, `homepage`,
      causing an issue with loading a bot, when these fields were omitted.
    - Fixed issue with parsing json content within the JSON bot info due to text encoding.
- Bot API:
    - Corrected the description of the `onScannedBot()` event handler.
- Server:
    - TPS: When TPS was set to 0 and the battle was restarted, the battle ran in max TPS.

## 📦 0.14.3 - Fixed setting adjustment turns - 07-Aug-2022

#### 🪲 Bug Fixes

- GUI:
    - NullPointerException when running robocode-tankroyale-gui-0.14.2.jar
      ([#38](https://github.com/robocode-dev/tank-royale/issues/38))
- Server:
    - `AdjustRadarForBodyTurn` had no effect on the server side.
    - `updateBotTurnRatesAndDirections` did not work properly with adjustments for body and gun turn when
      using `setAdjustGunForBodyTurn`, `setAdjustRadarForGunTurn`, and `setAdjustRadarForBodyTurn`.
- Sample bots
    - Updated Corners to start at random corner when a new battle is started.

## 📦 0.14.2 - Added Velocity Bot - 29-Jul-2022

#### 🚀 Improvements

- Bot API:
    - The run() method is now forced to stop/abort (in case the run method enters and infinite loop).
- Server:
    - Bots are not "sliding" along walls anymore when hitting those.
- Sample bots
    - Added _VelocityBot_ that demonstrates use of turn rates.

#### 🪲 Bug Fixes

- Bot API:
    - Setting the turn rates with the `Bot` class did not work properly (bot, gun, and radar did not turn at all).
- Server:
    - Fixed calculation of 1st, 2nd, and 3rd places with the end results.
    - Fixed issue with restarting a game that has ended.
    - Removal of NullPointerExceptions occurring with max TPS.
- GUI:
    - TPS:
        - Loop could occur when typing in the TPS, where TPS would continuously change to different values.
        - Setting the TPS to max could be reset to default TPS when restarting the battle or GUI.
    - GUI client was registered multiple times with the server.

## 📦 0.14.1 - Added BotInfo builder - 14-Jul-2022

#### 🚀 Improvements

- Bot API:
    - BotInfo:
        - A builder has been provided for creating `BotInfo` instances.
        - Size constraints have been added for the individual `BotInfo` fields/methods.
        - Various bugfixes for BotInfo.
    - GameTypes:
        - Added missing game type for "classic" game + updated documentation.

## 📦 0.14.0 - Adjustments for bot events - 03-Jul-2022

#### Changes

- Bot API:
    - Adjusted the bot events to make it easier to make a bridge between legacy Robocode bots and Tank Royale later
      ([#12](https://github.com/robocode-dev/tank-royale/issues/12)).
    - Introduced `BotDeathEvent` and `HitByBulletEvent`.
    - Made priority values for`DefaultEventPriority` public, and changed event priority order to match the original
      Robocode game.
    - Bot event priorities can now be read and changed.
- GUI:
    - The Pause/Resume and Stop button are now disabled when the battle is stopped.
- Schema:
    - Replaced `$type` with `type` to make it possible to use more code generators
      [#31](https://github.com/robocode-dev/tank-royale/issues/31)

## 📦 0.13.4 - GUI improvements + bugfix - 06-Jun-2022

#### 🪲 Bug Fixes

- Bot API for Java:
    - Major bugfix with `getTurnRemaining()` ([#28](https://github.com/robocode-dev/tank-royale/issues/28)), which
      returned the wrong value. This could be seen with the sample bot, Crazy, which did not turn correctly.

#### Changes

- GUI:
    - **Del Key**: It is now possible to use the Del key to remove a selected bot item on the Booted Bots (to unboot),
      Joined Bots, and Selected Bot lists when selecting bots for a new battle.
    - **Unboot All**: An `← Unboot All` button has been added right below the `← Unboot` button.
    - **TPS**:
        - The last used TPS (Turns Per Second) is now restored when starting up the GUI again.
        - Added a "Default TPS" button to reset the TPS to the default setting (30 TPS).
    - **Tool tips**: Added tool tip texts on the Control panel.

## 📦 0.13.3 - Stabilization of Bot APIs - 01-Jun-2022

#### 🪲 Bug Fixes

- Bot API:
    - Fix for running bots under macOS/bash when bot directory contains
      whitespaces ([#20](https://github.com/robocode-dev/tank-royale/discussions/20)).
    - New fix for issue [#17](https://github.com/robocode-dev/tank-royale/discussions/17) (Blocking bot functions do not
      stop bot after finishing)
    - Fix for `setForward(POSITIVE_INFINITE)` which moved the bot slowly forward with no
      acceleration ([#26](https://github.com/robocode-dev/tank-royale/issues/26)).
    - Fixed issue where the event queue was not disabled if `run()` method threw an exception, causing the event queue
      to reach its max limit of unprocessed 256 events.
    - Fixed issue with events being handled 1 turn later than they
      happened ([#8](https://github.com/robocode-dev/tank-royale/issues/8))
- Bot API for Java:
    - Fixed: `IllegalArgumentException: -Infinity is not a valid double value as per JSON specification`
- Server
    - Fixed various issues with invoking SkippedTurns ([#8](https://github.com/robocode-dev/tank-royale/issues/8))
- GUI:
    - When stopping a battle and starting a new one, the Pause/Resume and Stop buttons were disabled.

## 📦 0.13.2 - Fixing issue #23 - 19-May-2022

#### 🪲 Bug Fixes

- Bot API for .Net:
    - Setting Bot.TargetSpeed did not set the distance to travel.
    - Fixed issue with turning the body, gun, and radar as first and only
      thing ([#22](https://github.com/robocode-dev/tank-royale/issues/22)
      , [#23](https://github.com/robocode-dev/tank-royale/issues/23)).
    - Fixed issue with event queue reaching max after bot has terminated execution
      ([#23](https://github.com/robocode-dev/tank-royale/issues/23)).
- Bot APIs:
    - Added missing documentation about using positive and negative infinity values when moving and turning.

## 📦 0.13.1 - Fixing event queue + country codes - 18-May-2022

#### 🪲 Bug Fixes

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

## 📦 0.13.0 - Bot API for .Net 6 - 03-May-2022

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

#### 🪲 Bug Fixes

- Bot API:
    - Blocking bot functions do not stop bot after
      finishing ([#17](https://github.com/robocode-dev/tank-royale/issues/17)).
    - Fixed issue where event queue would overflow with unhandled events when a Bot´s `run()` method has ended.
- GUI:
    - The 'Start game' button is now disabled when the minimum or maximum number of participants is not met.

## 📦 0.12.0 - Single stepping battle - 17-Apr-2022

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

#### 🪲 Bug Fixes

- Bot API:
    - Make sure the bot terminates (system exit) upon a connection error to prevent dangling bot processes, which
      is usually occurring when terminating the UI, which closes the bot connections.
- GUI:
    - When dragging the battle arena, the graphics were not updated while dragging.

## 📦 0.11.2 - Fix restart issue - 12-Apr-2022

#### 🪲 Bug Fixes

- Battle does not restart ([#10](https://github.com/robocode-dev/tank-royale/issues/10)).
- Bot API did not stop thread when GameAbortedEvent occurred (when restarting).
- When restarting, a two or more games could be started on the server.

## 📦 0.11.1 - Patch for the GUI - 07-Apr-2022

#### 🪲 Bug Fixes

- ConcurrentModificationException could occur for `BotSelectionPanel.updateJoinedBots()`. Could not create a new battle.
- Fixed issue with starting a new battle from the menu with Battle → 'Start Battle' a second time.

#### Changes

- "Restart server" has been renamed into "Reboot Server", and the server and battle is fully stopped, and bots are
  disconnected. Confirmation dialog for rebooting now differ between the reboot is a user action or due to a changed
  server setting.
- Control panel (Pause/Resume, Stop, Restart, TPS) is now always shown when first battle has been started. Various
  improvements were done to the control panel.
- A busy cursor is now (automatically) shown when an operation takes some time to perform.

## 📦 0.11.0 - Initial position feature - 02-Apr-2022

#### 🪲 Bug Fixes

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

## 📦 0.10.0 - Bot API improvements - 24-Mar-2022

#### 🪲 Bug Fixes

- Fixed MyFirstBot.java (sample bot) not running.
- Various bug fixes were found in the Bot APIs, and missing get/set methods were added.
- Selected bots were not removed from the New Battle dialog when bots are disconnecting.
- Booter did not transfer environment variables to bot processes from GUI.

#### Changes

- Moved documentation from robocode.dev to [GitHub Pages](https://robocode-dev.github.io/tank-royale/)
- Changed the default server port from 80 to 7654 to avoid the use of `sudo` before `java` command (ports above 1023
  does not need `sudo`)
- Bot APIs: Moved constants to a Constants class.
- Bot APIs: Introduced a Color class for representing colors.
- Bot API for .Net: Some Set/Get methods were refactored into properties for e.g. speed, turn rates, and colors.
- Bot API for .Net: Lots of optimizations were made.

#### 🚀 Improvements

- Bot handshake has been extended to include server secret with the bot-handshake and the BaseBot class has been
  extended for setting this as input parameter in the constructor.
- Server secret has been split into *controller/observer secrets* and *bot secrets*
    - It is now possible to supply multiple server secrets
- Client is disconnected when sending a wrong secret to the server.
    - Bot API now writes out status-code and the reason for disconnecting when it is due to a wrong secret.

## 📦 0.9.12 - 23-Jan-2022

- First alpha version was released! 🚀😀

[BotInfo.FromConfiguration(IConfiguration)]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.BotInfo.html#Robocode_TankRoyale_BotApi_BotInfo_FromConfiguration_Microsoft_Extensions_Configuration_IConfiguration_