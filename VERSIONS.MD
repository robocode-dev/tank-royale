# Versions

## 📦

#### 🚀 Improvements

- Bot API:
    - Added `stop(boolean overwrite)` and `setStop(boolean overwrite)` to overwrite the saved movement from a previous
      call to `setStop()` with current movement values. These methods provide backwards compatibility with the original
      Robocode with the [Robocode API Bridge] (in progress).
    - Due to the removal of Java's `Thread.kill()` and .Net's `Thread.Abort()`, the main thread of the bot is no longer
      being killed by those methods is those threads could not be interrupted. Instead, the bot process it shut down
      with an error message:
      `The thread of the bot could not be interrupted causing the bot to hang. So the bot was stopped by force.`

#### 🪲 Bug Fixes

- Scoring:
    - Fixed various scoring issues when comparing to original Robocode. 
- Bot API:
    - Fixed race condition when adding custom events.
- Bot Console:
    - Events are only printed out while the bot is alive or has just died.
    - The DeathEvent was missing.

## 📦 0.20.1 - Added Events to bot console - 15-Sep-2023

#### 🚀 Improvements

- Bot Console (in GUI):
    - Added an [Events tab] on the Bot Console that continuously dumps the bot events as they occur.
    - Bot property column values on bot consoles are now written in bold and with a monospace font.

## 📦 0.20.0 - Added support for teams and droids - 23-Aug-2023

#### 🚀 Improvements

- Team support:
    - Added support for teams, where a team is a group of bots that work together to battle against other teams (or
      bots). Each team member can communicate with the other teammates, which is crucial to share information and
      coordinate movement and attacks.
- Droid interface:
    - A **Droid** interface was added that turns a bot into a droid bot, which has no radar but has 120 initial energy
      points instead of the normal 100 starting points. The droid is intended for team bots.
- Sample Bots:
    - **MyFirstTeam** has been added to demonstrate how to set up a team.
    - **MyFirstLeader** is a team bot that has been added to demonstrate a (leader) bot that is responsible for scanning
      enemy bots and sending the coordinates of the current target, to fire at, to the teammates.
    - **MyFirstDroid** is a team bot that is a droid, and hence comes with no radar, and relies entirely on the
      MyFirstLeader to send data about which target coordinate to fire at.
- Bot API:
    - Added the `TeamMessageEvent` which is received when another team member has sent a message.
    - Added these new methods to support teams:
        - `getTeammateIds()` to get the ids of all teammates in a team.
        - `isTeammate(botId)` to check if a bot with a specific id is a teammate or opponent.
        - `broadcastTeamMessage(message)` broadcasts a message to all team members.
        - `sendMessage(botId, message)` sends a message to a specific team member.
        - `onMessageReceived(TeamMessageEvent)` is an event handler to take action when a team message is received.
    - Added the `Droid` interface (no scanner, but 120 initial energy points).
- Booter:
    - Updated to support booting teams besides bots.
    - Changed the name of the `run` command into `boot`.
    - Changed command option `-T` into `-g` for filtering game rules (`-t` is now used for the team-only option).
    - Added `--botsOnly` and corresponding `-b` option flag for filtering on bots only (excluding teams).
    - Added `--teamsOnly` and corresponding `-t` option flag for filtering on teams only (excluding bots).
- GUI:
    - Added `Directory Filter` dropdown to filter on bots and teams.
    - The results window now aggregates scores for teams as well as bots and got a wider "Name" column. Some column
      names were changed, and tooltips were added to fields containing values/scores.
- Schema:
    - Renamed `bot-results-for-bot` and `bot-results-for-observer` into `results-for-bot` and `results-for-observer` as
      participants now include teams and not just bots.

#### 🪲 Bug Fixes

- GUI:
    - The GUI could get stuck and become unresponsive when displaying the busy pointer.

## 📦 0.19.3 - Maintenance - 04-Aug-2023

#### 🚀 Improvements

- Server and Booter:
    - The command-line options and commands are now case-insensitive.
    - Changed `--botSecrets` and `--controllerSecrets` into `--bot-secrets` and `--controller-secrets`.
- GUI:
    - The standard output and standard error messages are now being cached within the GUI for the current round for the
      individual bots, so the history of the round is available when opening the bot consoles.
    - Changed the display name of the bot on the bot buttons and bot console title.

#### 🪲 Bug Fixes

- Server:
    - Allow bots to connect to non-localhost addresses (PR [#69]).
- Bot API:
    - Fixed broken unit test for .Net.
- GUI:
    - Fixed issue, where the battle graphics was not displayed when starting the battle.
    - Fixed calculation of 1st, 2nd, and 3rd places.

## 📦 0.19.2 - Added bot properties tab - 05-Apr-2023

#### 🚀 Improvements

- GUI:
    - A new Properties tab was added to the bot console, which shows the values of all bot properties for the current
      turn. This is useful for viewing the exact values and for debugging a bot.
    - When displaying an arena bigger than the window showing the battle, the window is now zooming into the battle
      arena automatically so that the visual field fits into the window.

#### 🪲 Bug Fixes

- GUI:
    - When changing the game type for a battle, the arena did not change visually to accommodate to a new size of the
      battle arena.
- Server:
    - [#66]: The server is not accepting the full range of ports.

## 📦 0.19.1 - Maintenance - 26-Feb-2023

#### 🚀 Improvements

- GUI:
    - Console windows for bots and the server now supports 3-bit and 4-bit [ANSI colors][ANSI escape code] (foreground
      and background) and simple text formatting.
    - Added `bot died` info to the bot console.
    - Direct3D and OpenGL acceleration is now disabled under Windows. You can add those yourself when you start up
      the GUI using the `java` command. Read more about it
      [here](https://docs.oracle.com/javase/7/docs/technotes/guides/2d/flags.html).

#### 🪲 Bug Fixes

- Server:
    - [#60]: The round-ended event was not sent, in the round when the game ended.
- GUI:
    - Joined Bots list was not updated (removing bots) when the game is aborted.
    - The properties `sun.java2d.d3d`, `sun.java2d.noddraw`, and `sun.java2d.opengl` is now omitted under macOS.
    - Fixed label colors for fields showing country codes (with flags).
    - Sometimes flags were "empty" when clicking on a joined bot.
- Bot API for .Net:
    - Issue where the `Game Types` field was overwritten by `Country Codes` (could be seen on the UI).

## 📦 0.19.0 - Added bot console windows - 28-Jan-2023

#### 🚀 Improvements

- GUI:
    - A side panel has been added to the right side of the battle view with buttons for each participant bot. When
      clicking one of these buttons, a bot console window is opened that prints out the standard output (stdout) and
      standard error (stderr) from the bot.
    - Added 'OK', 'Clear', and 'Copy to clipboard' buttons to the server log window.
    - Renamed buttons named "Dismiss", used for closing dialogs, to "OK".
- Bot API:
    - When a bot is booted, its standard output (stdout) and standard error (stderr) streams are redirected to the bot
      intent sent to the server, which forwards the data to the observers, making it possible to show stdout and stderr
      in the bot console windows.
    - [#46]: The `RoundEndedEvent` now contains a `results` field containing the accumulated results by the end of a
      round.
- Schema:
    - [#46]: The `round-ended-event` has been split up into `round-ended-event-for-bot` that provides only results for a
      bot,
      and `round-ended-event-for-observer` that provides the results for all bots for observers.

#### 🪲 Bug Fixes

- Bot API:
    - `setAdjustRadarForGunTurn()` was made compatible with orig. Robocode.
    - Fixed issues with moving and turning, which could override the set target speed and turn rates, when these had
      been set explicitly on the API.

## 📦 0.18.0 - Improvements to the Bot APIs - 18-Dec-2022

#### 🪲 Bug Fixes

- Bot API:
    - [#52]: The Bot API used comma-separated strings for `authors`, `countryCodes`, and `gameTypes` when parsing
      the `BotInfo` schema even though the [BotInfo/BotHandshake] uses string arrays. This was fixed to keep Bot APIs
      consistent with the schemas.
    - [#53]: Major bug where each bot received the bullet states of _all_ bullets on the battle arena, including bullets
      fired by other bots, and not just the bullets fired by the bot itself.
    - ~~Fixed issues with moving and turning, which could override the set target speed and turn rates, when these had
      been set explicitly on the API.~~
    - Reading the turn rates and target speed for the current turn did not reflect if the turn rate and/or target speed
      had been set for the current turn.
- Bot API for .Net:
    - Setting the max speed and max turn rates for the body, gun, and radar did not work.
- GUI app:
    - Fixed rendering issues on Windows (and Linux?) when using accelerated hardware, by setting these Java properties
    - as defaults:
        - `sun.java2d.d3d=false` (turn off use of Direct3D)
        - `sun.java2d.noddraw=true` (no use of Direct Draw)
        - `sun.java2d.opengl=true` (turn on OpenGL)

#### 🚀 Improvements

- Bot API:
    - `addCondition()` and `removeCondition()` now returns a boolean value instead of void.
    - Condition can take an optional lambda expression as an input parameter as an alternative to overriding the test()
      method.
    - Added more test units, which did show bugs within the Bot API for both Java and .Net.
    - The event priority order was reversed to match the order used with the original Robocode.

## 📦 0.17.4 - Maintenance - 16-Oct-2022

#### 🚀 Improvements

- Server:
    - Added `--tps` option to the server for setting the initial Turns Per Second (TPS).
    - Fire assistant is now only activated, when both the radar and gun are pointing in the same direction for two turns
      in a row (like original Robocode).
- GUI App:
    - Switched the order of the Game Type dropdown and Setup Rules button.
    - The positioning of the battle view is now controlled by dragging the mouse, instead of clicking.
    - Now sends the current tps to the server when changing game type.
    - Added `Help` menu item under the Help menu, which opens browser to the documentation of the GUI application.
- Release
    - All Java artifacts are now sent to Maven Central, i.e. Bot API, GUI Application, Server, and Booter.
    - New assets are now added to releases: Server and Booter
    - Added build scripts to automated creating (draft) releases using GitHub REST API.
    - Improved layout for release notes.
    - Optimized ProGuard rules for compressing the Booter and Server jar archives even more, making the jar archive for
      the GUI smaller as well.

#### 🪲 Bug Fixes

- Bot API and GUI App: Fixed various concurrency issues.

## 📦 0.17.3 - Maintenance - 02-Oct-2022

#### ⚡ Changes

- [#43]: Possibility to identify bot from Process to Tick:
    - Went back to boot process `pid` instead of using the `boot id`.

#### 🚀 Improvements

- Sample bots: Improved the Fire sample bot by removing unnecessary code.

#### 🪲 Bug Fixes

- Server:
    - The fire assistant was not always activated.
    - [#45]: Server is kidnapping CPU cycles
        - Game is now aborted, when the last bot participating in a battle is leaving (disconnecting).
        - Timer used for turn timeout was not stopped, when a battle had ended/aborted.
        - Freeing heap memory used for storing game state when game is ended/aborted.

## 📦 0.17.2 - Added sounds - 20-Sep-2022

#### 🚀 Improvements

- GUI/Sounds: [#4]: Fire and Explosion Sounds:
    - Added feature to play sounds to the GUI application.
    - Added Sound Options to configure sounds.
    - Note that sound files are distributed independently besides the gui application from the [sounds releases].
- GUI: Moved the ~~Dismiss~~OK button on the Bot Root Directories configuration down under the Add and Remove buttons,
  and adjusted the size of the dialog.

#### 🪲 Bug Fixes

- GUI: When the Bot Directories on the Select Bots for Battle window is updated, all selections are now being cleared.

## 📦 0.17.1 - Identifying a bot from Boot-up to Ticks - 15-Sep-2022

#### 🚀 Improvements

- [#43]: Possibility to identify bot from Process to Tick:
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
    - NullPointerException when running robocode-tankroyale-gui-0.14.2.jar ([#38])
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
    - Adjusted the bot events to make it easier to make a bridge between legacy Robocode bots and Tank Royale
      later ([#12]).
    - Introduced `BotDeathEvent` and `HitByBulletEvent`.
    - Made priority values for`DefaultEventPriority` public, and changed event priority order to match the original
      Robocode game.
    - Bot event priorities can now be read and changed.
- GUI:
    - The Pause/Resume and Stop button are now disabled when the battle is stopped.
- Schema:
    - Replaced `$type` with `type` to make it possible to use more code generators ([#31]).

## 📦 0.13.4 - GUI improvements + bugfix - 06-Jun-2022

#### 🪲 Bug Fixes

- Bot API for Java:
    - Major bugfix with `getTurnRemaining()` ([#28]), which returned the wrong value. This could be seen with the sample
      bot, Crazy, which did not turn correctly.

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
    - Fix for running bots under macOS/bash when bot directory contains whitespaces ([#20]).
    - New fix for issue [#17] (Blocking bot functions do not
      stop bot after finishing)
    - Fix for `setForward(POSITIVE_INFINITE)` which moved the bot slowly forward with no
      acceleration ([#26]).
    - Fixed issue where the event queue was not disabled if `run()` method threw an exception, causing the event queue
      to reach its max limit of unprocessed 256 events.
    - Fixed issue with events being handled 1 turn later than they
      happened ([#8])
- Bot API for Java:
    - Fixed: `IllegalArgumentException: -Infinity is not a valid double value as per JSON specification`
- Server
    - Fixed various issues with invoking SkippedTurns ([#8])
- GUI:
    - When stopping a battle and starting a new one, the Pause/Resume and Stop buttons were disabled.

## 📦 0.13.2 - Fixing issue #23 - 19-May-2022

#### 🪲 Bug Fixes

- Bot API for .Net:
    - Setting Bot.TargetSpeed did not set the distance to travel.
    - Fixed issue with turning the body, gun, and radar as first and only thing ([#22], [#23]).
    - Fixed issue with event queue reaching max after bot has terminated execution ([#23]).
- Bot APIs:
    - Added missing documentation about using positive and negative infinity values when moving and turning.

## 📦 0.13.1 - Fixing event queue + country codes - 18-May-2022

#### 🪲 Bug Fixes

- Bot APIs:
    - Fixed issue with rescanning and interrupting current event handler. Redesigned the event queue.
    - Fixed issue with dangling threads not been stopped (interrupted) correctly.
- Bot API for .Net:
    - Fixed issue with country codes (on macOS) which also caused boot up problems for .Net based bots running under
      macOS ([#20]).
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
    - Blocking bot functions do not stop bot after finishing ([#17]).
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

- Battle does not restart ([#10]).
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

- Moved documentation from robocode.dev to [GitHub Pages].
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

[#4]: https://github.com/robocode-dev/tank-royale/issues/4

[#8]: https://github.com/robocode-dev/tank-royale/discussions/8

[#12]: https://github.com/robocode-dev/tank-royale/issues/12

[#17]: https://github.com/robocode-dev/tank-royale/discussions/17

[#20]: https://github.com/robocode-dev/tank-royale/discussions/20

[#22]: https://github.com/robocode-dev/tank-royale/issues/22

[#23]: https://github.com/robocode-dev/tank-royale/issues/23

[#26]: https://github.com/robocode-dev/tank-royale/discussions/26

[#28]: https://github.com/robocode-dev/tank-royale/issues/28

[#31]: https://github.com/robocode-dev/tank-royale/issues/31

[#38]: https://github.com/robocode-dev/tank-royale/issues/38

[#43]: https://github.com/robocode-dev/tank-royale/issues/43

[#45]: https://github.com/robocode-dev/tank-royale/issues/45

[#46]: https://github.com/robocode-dev/tank-royale/issues/46

[#52]: https://github.com/robocode-dev/tank-royale/discussions/52#discussioncomment-4332072

[#53]: https://github.com/robocode-dev/tank-royale/issues/53

[#60]: https://github.com/robocode-dev/tank-royale/issues/60

[#66]: https://github.com/robocode-dev/tank-royale/issues/66

[#69]: https://github.com/robocode-dev/tank-royale/issues/69

[BotInfo/BotHandshake]: https://github.com/robocode-dev/tank-royale/blob/master/schema/schemas/bot-handshake.yaml

[BotInfo.FromConfiguration(IConfiguration)]: https://robocode-dev.github.io/tank-royale/api/dotnet/api/Robocode.TankRoyale.BotApi.BotInfo.html#Robocode_TankRoyale_BotApi_BotInfo_FromConfiguration_Microsoft_Extensions_Configuration_IConfiguration_

[sounds releases]: https://github.com/robocode-dev/sounds/releases

[GitHub Pages]: https://robocode-dev.github.io/tank-royale/

[ANSI escape code]: https://en.wikipedia.org/w/index.php?title=ANSI_escape_code

[Events tab]: https://robocode-dev.github.io/tank-royale/articles/gui.html#viewing-the-bot-events "Events tab in Bot Console"

[Robocode API Bridge]: https://github.com/robocode-dev/robocode-api-bridge "Robocode API bridge for Tank Royale"