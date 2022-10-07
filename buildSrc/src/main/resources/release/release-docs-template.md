## 📄 Documentation

You can read the Robocode Tank Royale [documentation here](https://robocode-dev.github.io/tank-royale/).

### 🔨 Try it out

Please head over to [My First Bot tutorial](https://robocode-dev.github.io/tank-royale/tutorial/my-first-bot.html) to learn how to set up your first bot for Robocode Tank Royale.

## ▶️ Running Robocode

You need Java 11 as a minimum or newer to be preinstalled on your system. I recommend that you use the newest version of Java, if possible.
You can read the [installation guide] to get more details about installing Java and Robocode.

The Robocode [GUI application](https://robocode-dev.github.io/tank-royale/articles/gui.html#gui-application) must be run from the command line in order to start and view battles:

```shell
java -jar robocode-tankroyale-gui-{VERSION}.jar
```

Download it from here:
[robocode-tankroyale-gui-{VERSION}.jar](https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/robocode-tankroyale-gui-{VERSION}.jar)

Note that you need to download sample bots and install those into directories on your system, and add these directories from the menu:

Config → Bot Root Directories

## 🤖 Sample bots

These sample bots are currently available:

|      |   |
|------|---|
| C#   | [sample-bots-csharp-{VERSION}.zip](https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/sample-bots-csharp-{VERSION}.zip) |
| Java | [sample-bots-java-{VERSION}.zip](https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/sample-bots-java-{VERSION}.zip)     |

Note that the C# bots need Microsoft .Net SDK 5.0 or newer and must be preinstalled.

## 📦 Bot API

In order to develop bots for Robocode, you'll need one of the provided APIs for the Java/JVM or .Net platform.

#### 📦 Java:

Available as:

- A jar file: [robocode-tankroyale-bot-api-{VERSION}.jar](https://s01.oss.sonatype.org/service/local/repositories/releases/content/dev/robocode/tankroyale/robocode-tankroyale-bot-api/{VERSION}/robocode-tankroyale-bot-api-{VERSION}.jar)
- An [artifact at Nexus Repository](https://s01.oss.sonatype.org/index.html#view-repositories;releases~browsestorage~/dev/robocode/tankroyale/robocode-tankroyale-bot-api/{VERSION}/robocode-tankroyale-bot-api-{VERSION}.jar).

At some point after this release the artifact will also become available from the [Maven Central Repository](https://search.maven.org/search?q=g:dev.robocode.tankroyale).

#### 📦 .Net:

Available as artifact on the Nuget repository:
https://www.nuget.org/packages/Robocode.TankRoyale.BotApi/{VERSION}

```shell
dotnet add package Robocode.TankRoyale.BotApi --version {VERSION}
```