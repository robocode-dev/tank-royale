# My First Bot for the JVM (Java Virtual Machine)

## Introduction

This tutorial is meant for the Java/JVM platform and a continuation of [My First Bot](../my-first-bot.md) tutorial.

The programming language used in this tutorial is [Java], which is the most widespread programming language for the JVM.
But other JVM programming languages like [Groovy], [Kotlin], [Scala], and [Clojure] can be used as well.

This tutorial assumes you are already familiar with basic [Java] programming. But this tutorial should suit well for
practising your skills programming for Java by making a bot for Robocode.

## Programming

### Java (JVM) API

The documentation of the Java (JVM) API for Robocode Tank Royale is available on [this page](../../api/apis.md).

### Create a source file

Inside your bot directory (`../bots/MyFirstBot`) you need to create a Java source file named `MyFirstBot.java`. You can
edit that file using a text editor of your choice, or an [IDE] like e.g. [IntelliJ IDEA], [Eclipse], [NetBeans],
or [Visual Studio Code].

### Initial code

The initial skeleton of your bot could look like this:

```java
import dev.robocode.tankroyale.botapi.*;

public class MyFirstBot extends Bot {
}
```

The class in this example (`MyFirstBot`) is inherited from the [Bot] class from the `dev.robocode.tankroyale.botapi`
package provides methods for controlling the bot but also makes it possible to receive events from the game. The API is
taking care of the communication with the server behind the scene.

### Startup / Main entry

The next thing we need to do is to declare a [main] method for our bot. The bot will run like an ordinary application,
and hence the [main] method is the entry point of the bot.

```java
    // The main method starts our bot
    public static void main(String[]args) {
        new MyFirstBot().start();
    }

    // Constructor, which loads the bot config file
    MyFirstBot() {
        super(BotInfo.fromFile("MyFirstBot.json"));
    }
```

The [main] method in this example simply calls the [start] method of the bot, which will let the bot startup reading
configuration and start communicating with the server.

The bot will attempt to _join_ the server and wait for a signal to engage in a new battle, where one or multiple
instances of this bot must participate.

The constructor of `MyFirstBot` is set up to call the base constructor, which needs a [BotInfo] object containing the
bot configuration. The [BotInfo] class contains a convenient method named [fromFile] which can initialize the [BotInfo]
by reading a JSON file. In this case, it will read the `MyFirstBot.json` file we created earlier, which must be
available within the bot directory (or some other file location accessible for the bot).

Note that it is also possible to provide all the necessary configuration fields programmatically without a file.

### The Run method

When the bot is started by the game, the [run] method will be called. Hence, your bot should override this method to
provide the logic for the bot when the game is started. The [run] method should do all required initializing. After
that, it should enter a loop that runs until the game is ended.

```java
    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        // Repeat while the bot is running
        while (isRunning()) {
            forward(100);
            turnGunRight(360);
            back(100);
            turnGunRight(360);
        }
    }
```

With the code above, the bot will run in a loop, starting by moving forward 100 units. Then it will turn the gun 360°,
move back 100 units and turn the gun 360° again. So the bot will continuously move forward and back all the time and
rotate the gun between moving.

When leaving the [run] method, the bot will not be able to send new commands each round besides code that runs in event
handlers. Therefore, a loop is used for preventing the [run] method from exiting. However, we should stop the loop as
soon as the bot is no longer running, and hence need to exit the [run] method when the [isRunning] method
returns `false`.

The [isRunning] method returns a flag maintained by the API. When the bot is told to stop/terminate its execution, the
[isRunning] method will automatically be set to return `false` by the API.

### Event handlers

The [Bot API] provides a lot of event handlers (on<em>"SomeEvent"</em> methods) that are triggered by different types of
events. All event handlers in the Bot API start with the *on*-prefix like e.g. [onScannedBot]. All event handlers are
available with the [IBaseBot] interface, which the [Bot] class implements.

Talking about the common [onScannedBot] event handler, we can implement this handler to fire the cannon whenever our bot
scans an opponent bot:

```java
    import dev.robocode.tankroyale.botapi.events.*;
    ...

    // We saw another bot -> fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        fire(1);
    }
```

We can also implement another event handler [onHitByBullet] to let the bot attempt to avoid new bullet hits by turning
the bot perpendicular to the bullet direction:

```java
    import dev.robocode.tankroyale.botapi.events.*;
    ...

    // We were hit by a bullet -> turn perpendicular to the bullet
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Calculate the bearing to the direction of the bullet
        double bearing = calcBearing(e.getBullet().getDirection());

        // Turn 90 degrees to the bullet direction based on the bearing
        turnLeft(90 - bearing);
    }
```

Note that the [Bot API] provides helper methods like [calcBearing] to ease calculating angles and bearings in the game.

### Putting it all together

Okay, let us put all the parts together in a single source file:

```java
import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

public class MyFirstBot extends Bot {

    // The main method starts our bot
    public static void main(String[] args) {
        new MyFirstBot().start();
    }

    // Constructor, which loads the bot config file
    MyFirstBot() {
        super(BotInfo.fromFile("MyFirstBot.json"));
    }

    // Called when a new round is started -> initialize and do some movement
    @Override
    public void run() {
        // Repeat while the bot is running
        while (isRunning()) {
            forward(100);
            turnGunRight(360);
            back(100);
            turnGunRight(360);
        }
    }

    // We saw another bot -> fire!
    @Override
    public void onScannedBot(ScannedBotEvent e) {
        fire(1);
    }

    // We were hit by a bullet -> turn perpendicular to the bullet
    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        // Calculate the bearing to the direction of the bullet
        double bearing = calcBearing(e.getBullet().getDirection());

        // Turn 90 degrees to the bullet direction based on the bearing
        turnLeft(90 - bearing);
    }
}
```

## Running the bot

Now we got a JSON configuration file and the program for our bot. The next step is to provide the files for running the
bot application. We need to set up scripts for starting the bot, and also supply the bot API library (jar file) that the
bot on build on and hence depends on.

### Supply bot API library

You need to download the `robocode-tankroyale-bot-api-x.y.z.jar` library, e.g. from the Java sample bots or Maven
repository and put this into a folder accessible for your bot. I recommend that you put this into a `../bots/lib`
folder (you create) in the root directory containing your MyFirstBot directory (`../bots/MyFirstBot`).

In the following, we assume that you created this `lib` directory beside your bot directory, and copied the bot API jar
file into the `lib` folder.

### Scripts for starting the bot

The remaining part is to supply some script files for starting up the bot. This will ease starting up the bot from the
command line. But those files are also necessary for booting up the bot from Robocode, which will look out for script
files when examining the bot directory and figure out how to run the bot. The script files tell the [booter] of Robocode
how to start the bot, which is different for each programming language, platform and OS.

With Java it is possible to run your bot under Windows, macOS, and Linux. Hence, it is a good idea to provide script
files for all these OSes, which mean that we should provide a [command file][.cmd] for Windows, and
a [shell script][.sh] for macOS and Linux.

We create a command file for Windows named `MyFirstBot.cmd` and put it into our bot directory:

```
java -cp ../lib/* MyFirstBot.java >nul
```

So the `java ... MyFirstBot.java` part is used for starting the bot standing in the bot directory from a command prompt.
The `-cp ../lib/*` part is used for setting the [classpath] containing the bot API. We put this in the library beside
the bot directory, and hence the classpath is `../lib`.

The star (*) tell the classloader in Java to read any file in the `lib` folder, and is just a convenient way to avoid
specifying the full name of the filename of the bot API `robocode-tankroyale-bot-api-x.y.z.jar`, which is quite long.

***IMPORTANT NOTE:*** The `>nul` is a work-around necessary to avoid a Windows-specific quirk where the bot becomes
unresponsive when started up as a process with the Robocode. [^cmd-quirk]

Next, we provide a shell script for macOS and Linux named `MyFirstBot.sh` and put it into our bot directory:

```
#!/bin/sh
java -cp ../lib/* MyFirstBot.java
```

Note that we need to set the file permission to grant *read*, *write*, and especially the *execute* right of the script
for the owner and owner´s group, e.g. set the permissions to `775` (`-rwxrwxr-x`). We can do this with this command:

```
chown 775 MyFirstBot.sh
```

Note that the `5` (*read* and *execute*) is set as everybody else than the owner and owner´s group does should not have
the *write* permission per default unless it is explicitly granted by you. ;)

Now you have everything in place to run your bot with Robocode Tank Royale.

## Packaging your bot

If you need to package your bot for distribution, you can do this by zip-packing the bot directory. The zip archive
should contain:

- Source file (.java, .kt, .groovy, .clj, or .scala)
- Script files (.cmd and .sh)
- JSON config file (.json)

And then you might want to provide a [README] file to provide some information for other people about your bot. :)

You can download the `sample-bots-java-x.y.z.zip` file from any [release], which provides a good example of how to
package one to multiple bots into a zip archive.

## Bot Secrets

When you want to run your bot outside the GUI application from a terminal/shell, you will have to supply `bot secrets`
to the bot. The `bot secrets` is one to several keys that is used by the server to allow the bot to access the server.

A server will automatically create a `server.properties` file when it is running. This file will contain generated keys
that must be used by "external" bots and controller for accessing the server. Inside the properties file, you will find
the field `bot-secrets` like this example:

```
bots-secrets=zDuQrkCLQU5VQgytofkNrQ
```

Here the key of `bot-secrets` is `zDuQrkCLQU5VQgytofkNrQ`.

A simple way to set the bot secret for the Java and .Net bot APIs is to set the environment variable `BOT_SECRETS` in
the shell before running the bot:

Mac/Linux bash/shell:

```bash
export VARIABLE_NAME=value
```

Windows command line

```cmd
set BOT_SECRETS=zDuQrkCLQU5VQgytofkNrQ
```

Windows PowerShell:

```powershell
$Env:BOT_SECRETS = zDuQrkCLQU5VQgytofkNrQ
```

Note that it is also possible to provide the server secret and URL programmatically with the Bot APIs with the `Bot`
and `BaseBot` constructors.


[^cmd-quirk]: ? "Note that the unresponsiveness of a Windows process running a bot is not observed when running the
bot directly from the command line with or without the script; only when using a Java process for running the script
inside the Robocode booter."

[Java]: https://docs.oracle.com/javase/tutorial/java/, "The Java Tutorials"

[Groovy]: https://groovy-lang.org/ "Groovy programming language"

[Kotlin]: https://kotlinlang.org/ "Kotlin programming language"

[Scala]: https://www.scala-lang.org/ "Scala programming language"

[Clojure]: https://clojure.org/ "Clojure programming language"

[IntelliJ IDEA]: https://www.jetbrains.com/idea/ "IntelliJ IDEA"

[Eclipse]: https://www.eclipse.org/downloads/packages/release/2021-12/r/eclipse-ide-java-developers "Eclipse IDE for Java Developers"

[NetBeans]: https://netbeans.apache.org/ "Apache NetBeans"

[Visual Studio Code]: https://code.visualstudio.com/ "Visual Studio Code homepage"

[Bot]: https://robocode-dev.github.io/tank-royale/api/java/dev/robocode/tankroyale/botapi/Bot.html "Bot class"

[main]: https://docs.oracle.com/javase/tutorial/getStarted/application/index.html "main() method"

[start]: https://robocode-dev.github.io/tank-royale/api/java/dev/robocode/tankroyale/botapi/BaseBot.html#start() "BaseBot.start()"

[BotInfo]: https://robocode-dev.github.io/tank-royale/api/java/dev/robocode/tankroyale/botapi/BotInfo.html "BotInfo class"

[fromFile]: https://robocode-dev.github.io/tank-royale/api/java/dev/robocode/tankroyale/botapi/BotInfo.html#fromFile(java.lang.String) "BotInfo.fromFile() method"

[run]: https://robocode-dev.github.io/tank-royale/api/java/dev/robocode/tankroyale/botapi/IBot.html#run() "IBot.run() method"

[isRunning]: https://robocode-dev.github.io/tank-royale/api/java/dev/robocode/tankroyale/botapi/IBot.html#isRunning() "IBot.isRunning() method"

[Bot API]: https://robocode-dev.github.io/tank-royale/api/java/dev/robocode/tankroyale/botapi/Bot.html "Bot API"

[IBaseBot]: https://robocode-dev.github.io/tank-royale/api/java/dev/robocode/tankroyale/botapi/IBaseBot.html "IBaseBot interface"

[onScannedBot]: https://robocode-dev.github.io/tank-royale/api/java/dev/robocode/tankroyale/botapi/IBaseBot.html#onScannedBot(dev.robocode.tankroyale.botapi.events.ScannedBotEvent) "IBaseBot.onScannedBot(ScannedBotEvent) event handler"

[onHitByBullet]: https://robocode-dev.github.io/tank-royale/api/java/dev/robocode/tankroyale/botapi/IBaseBot.html#onHitByBullet(dev.robocode.tankroyale.botapi.events.BulletHitBotEvent) "IBaseBot.onHitByBullet(HitByBulletEvent) event handler"

[calcBearing]: https://robocode-dev.github.io/tank-royale/api/java/dev/robocode/tankroyale/botapi/IBaseBot.html#calcBearing(double) "IBaseBot.calcBearing(double) method"

[java]: https://docs.oracle.com/en/java/javase/17/docs/specs/man/java.html "java command"

[booter]: ../../articles/booter.md "Robocode booter"

[.cmd]: https://fileinfo.com/extension/cmd "Windows Command File"

[.sh]: https://fileinfo.com/extension/sh "Bash Shell Script"

[README]: https://fileinfo.com/extension/readme "Readme File"

[classpath]: https://howtodoinjava.com/java/basics/java-classpath/ "Java classpath"

[release]: https://github.com/robocode-dev/tank-royale/releases "Releases"