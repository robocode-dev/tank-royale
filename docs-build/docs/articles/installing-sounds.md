# Installing sounds

Installing sound files is optional, but it enables audio effects such as gunshots, explosions, and collisions.

Download `sounds.zip` from the [sounds releases](https://github.com/robocode-dev/sounds/releases).

## Installation

1. Extract the `sounds` directory from the archive
2. Copy the `sounds` directory into the same directory as `robocode-tankroyale-gui-x.y.z.jar`

The directory structure should look like this:

```
[your tank royale directory]
├── robocode-tankroyale-gui-x.y.z.jar
└── sounds/
    ├── bots_collision.wav
    ├── bullet_hit.wav
    ├── bullets_collision.wav
    ├── death.wav
    ├── gunshot.wav
    ├── wall_collision.wav
    └── ...
```

## After installation

- sounds are enabled automatically when the `sounds/` directory is detected
- you can enable or disable all sounds or individual sound effects from **Sound Options**
- the GUI will use the installed files automatically

For the GUI dialog itself, see [Configuring the GUI](gui-configuration.md#sound-options).

## Using your own sound files

You can replace one or more sounds with your own [WAV] files:

- keep the original filenames
- use WAV format only
- place the files in the same `sounds/` directory

[WAV]: https://en.wikipedia.org/wiki/WAV "WAV file"
