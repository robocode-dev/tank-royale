## 📄 Documentation

You find the Robocode Tank Royale [documentation here](https://robocode-dev.github.io/tank-royale/index.html). You should start out by reading [Getting Started] first.

### 🔨 Try it out

Please head over to [My First Bot tutorial] to learn how to set up your first bot for Robocode Tank Royale.

## 🛠 Installing Robocode

You need Java 11 as a minimum or newer, e.g. the newest version of Java available.

You can read the [installation guide] to get more details about installing both Java and Robocode.

## ▶ Running Robocode

The main application is the [GUI Application] which is a Java application. You can read about how to use the GUI Application [here](https://robocode-dev.github.io/tank-royale/articles/gui.html#gui-application).

The Robocode [GUI application] is run from the command line (shell or command prompt) in order to start and view
battles:

```shell
java -jar robocode-tankroyale-gui-{VERSION}.jar
```

## 🤖 Sample bots

If you are new to Robocode, you need to download some bots and extract those to directories on your system.
These bot directories can be added from the menu of the GUI: `Config → Bot Root Directories`

These sample bots are currently available:

| Platform | Archive                            | Requirements                      |
|----------|------------------------------------|-----------------------------------|
| C#       | [sample-bots-csharp-{VERSION}.zip] | Microsoft [.Net SDK] 6.0 or newer |
| Java     | [sample-bots-java-{VERSION}.zip]   | Any [Java SDK] 11 or newer        |

All bots are put in zip archives, which should be installed in independent directories.
Each zip archive contains a ReadMe.md file with more information for the specific platform.

## 📦 Bot API

In order to start developing bots for Robocode, the following APIs are available.

#### 📦 Java:

Available as:

- Jar file: [robocode-tankroyale-bot-api-{VERSION}.jar]
- Artifact at [Nexus Repository](https://s01.oss.sonatype.org/index.html#view-repositories;releases~browsestorage~/dev/robocode/tankroyale/robocode-tankroyale-bot-api/{VERSION}/robocode-tankroyale-bot-api-{VERSION}.jar)
- Artifact at [Maven Central Repository](https://search.maven.org/search?q=g:dev.robocode.tankroyale) (available after some time)

#### 📦 .Net:

Available as:
 - Artifact at [Nuget repository](https://www.nuget.org/packages/Robocode.TankRoyale.BotApi/{VERSION})


[sample-bots-csharp-{VERSION}.zip]: https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/sample-bots-csharp-{VERSION}.zip

[sample-bots-java-{VERSION}.zip]: https://github.com/robocode-dev/tank-royale/releases/download/v{VERSION}/sample-bots-java-{VERSION}.zip

[robocode-tankroyale-bot-api-{VERSION}.jar]: https://s01.oss.sonatype.org/service/local/repositories/releases/content/dev/robocode/tankroyale/robocode-tankroyale-bot-api/{VERSION}/robocode-tankroyale-bot-api-{VERSION}.jar

[.Net SDK]: https://dotnet.microsoft.com/en-us/download/dotnet

[Java SDK]: https://robocode-dev.github.io/tank-royale/articles/installation.html#java-11-or-newer

[My First Bot tutorial]: https://robocode-dev.github.io/tank-royale/tutorial/my-first-bot.html "My First Bot Tutorial"

[Getting Started]: https://robocode-dev.github.io/tank-royale/tutorial/getting-started.html "Getting Started"

[installation guide]: https://robocode-dev.github.io/tank-royale/articles/installation.html "Installing and running Robocode"

[GUI Application]: https://robocode-dev.github.io/tank-royale/articles/gui.html "The GUI application"
