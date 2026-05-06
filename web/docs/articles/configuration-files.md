# Configuration files

This guide describes the files stored in the Robocode Tank Royale user data directory.

## gui.properties

The `gui.properties` file stores GUI settings and preferences. The file is created automatically when you first launch
the GUI and is updated whenever you change settings through **GUI Options**.

**Location:**

```
{UserDataDirectory}/gui.properties
```

**Example content:**

```properties
# GUI Language
locale=en
# UI Scaling for high-DPI displays
ui-scale=1.0
# Console settings
console-max-characters=10000
# Last used window dimensions and position
window-width=1200
window-height=800
window-x=100
window-y=100
```

| Property                 | Description                                | Default |
|:-------------------------|:-------------------------------------------|:--------|
| `locale`                 | GUI language                               | `en`    |
| `ui-scale`               | UI scaling factor for high-DPI displays    | `1.0`   |
| `console-max-characters` | Maximum characters retained in bot console | `10000` |
| `window-width`           | Main window width in pixels                | `1200`  |
| `window-height`          | Main window height in pixels               | `800`   |

Edit the file manually if needed, but the GUI Options dialog is usually the safer way to change these values.

## server.properties

The `server.properties` file contains local server configuration, including authentication secrets for bots and
controllers. It is created automatically when you first start the local server or GUI.

**Location:**

```
{UserDataDirectory}/server.properties
```

**Example content:**

```properties
# Port number for the local server
port=7654
# Bot authentication secret
bots-secrets=/zWlsdEfhNX1YPggA9DJlw
# Controller authentication secret (for GUI and recorder)
controller-secrets=Xcrw0ydtiscD7L7xAT/K4g
# Enable server secrets authentication
enable-server-secrets=false
# Allow turn-by-turn debug stepping (set false for tournaments)
# debugModeSupported=true
# Allow breakpoint mode for debugged bots (set false for tournaments)
# breakpointModeSupported=true
```

| Property                  | Description                                                                 | Default        |
|:--------------------------|:----------------------------------------------------------------------------|:---------------|
| `port`                    | WebSocket port for the local server                                         | `7654`         |
| `bots-secrets`            | Secret for bot authentication                                               | Auto-generated |
| `controller-secrets`      | Secret for controller and GUI authentication                                | Auto-generated |
| `enable-server-secrets`   | Enable or disable secret authentication                                     | `false`        |
| `debugModeSupported`      | Allow turn-by-turn stepping; set `false` for tournaments                    | `true`         |
| `breakpointModeSupported` | Allow breakpoint mode for debugged bots; set `false` for tournaments        | `true`         |

Security considerations:

- the secrets are generated automatically on first run
- keep them confidential if you expose the server publicly
- bots can provide the bot secret through the Bot API or the `SERVER_SECRET` environment variable

Edit this file manually if needed, then restart the server for changes to take effect.

## game-setups.properties

The `game-setups.properties` file stores custom game configurations created through the **Setup Rules** dialog.

**Location:**

```
{UserDataDirectory}/game-setups.properties
```

**Example content:**

```properties
# Custom game setup for testing
test-setup.game-type=custom
test-setup.arena-width=800
test-setup.arena-height=600
test-setup.number-of-rounds=5
test-setup.gun-cooling-rate=0.1
test-setup.min-number-of-participants=2
test-setup.max-number-of-participants=4
test-setup.max-inactivity-turns=450
test-setup.turn-timeout=10000
test-setup.ready-timeout=1000000
# Classic melee battle
melee.game-type=melee
melee.number-of-rounds=10
melee.min-number-of-participants=4
melee.max-number-of-participants=10
```

| Property                     | Description                        | Typical Values                      |
|:-----------------------------|:-----------------------------------|:------------------------------------|
| `game-type`                  | Type of game                       | `classic`, `melee`, `1v1`, `custom` |
| `arena-width`                | Arena width in pixels              | `800` to `5000`                     |
| `arena-height`               | Arena height in pixels             | `600` to `5000`                     |
| `number-of-rounds`           | Number of rounds                   | `1` to `100+`                       |
| `gun-cooling-rate`           | Gun cooling rate per turn          | `0.1`                               |
| `min-number-of-participants` | Minimum bots required              | `2`                                 |
| `max-number-of-participants` | Maximum bots allowed               | `4` to `16+`                        |
| `max-inactivity-turns`       | Turns before inactivity penalty    | `450`                               |
| `turn-timeout`               | Microseconds per turn before skip  | `10000`                             |
| `ready-timeout`              | Microseconds for bot ready message | `1000000`                           |

You can create these entries from the GUI or edit them manually.
