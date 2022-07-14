# Installing and running Robocode

This guide describes how to install and run Robocode Tank Royale.

## Java 11 or newer

Robocode is running on a Java Runtime Environment (JRE) and needs Java 11 as a minimum. If you want to develop bots for
Robocode in the Java programming language, then you need a JDK (Java Development Kit).

> Note that you do not need to run and old version 11 of Robocode to run the GUI, booter and server, or your Java/JVM
> based bot. But you cannot use Java versions older than version 11.

Numerous Java distributions are available:

- [Oracle JDK](https://www.oracle.com/java/technologies/javase/)
- [OpenJDK](https://openjdk.java.net/)
- [Adoption / Eclipse Temurin](https://adoptium.net/)
- [Red Hat](https://developers.redhat.com/products/openjdk)
- [Azul Zulu](https://www.azul.com/downloads/?package=jdk)
- [Amazon Corretto](https://aws.amazon.com/corretto)

If you do not have java installed already (can be checked writing the command below), you should download and install
Java first. Follow the installation instructions carefully, and make sure that you are able to run `java` from the
command line by writing:

```shell
java -version
```

This is a check that Java has been installed correctly and is available. Entering `java -version` should write the
version of the Java that is being used along with the vendor of the java distribution.

## Running the Robocode application (GUI)

Robocode has a GUI application that can be used for running and viewing battles on your local machine. You should use
this application for observing how your bot(s) perform in the battle arena against other bots.

You can download the application from the [Robocode releases].

You need a file named `robocode-tankroyale-gui-x.y.z.jar`, where x.y.z is the specific version number of Robocode, e.g.
version 0.14.1.

You might be able to simply start the application by (double)clicking it, depending on the OS and Java version you have.
If you cannot start the Robocode application by clicking it, you should start it from the command line like this. You
need to stand in the directory containing the .jar file of course.

```
java -jar robocode-tankroyale-gui-x.y.z.jar
```

### GUI issue on Windows with NVidea gfx card

Some people running graphical Java applications on Windows with an NVidia graphics card might experience that the
windows, dialogs, menus etc. might not be painted correctly all of a sudden. If you experience this annoying issue, you
can try to add this option after the `java` command:

```
-Dsun.java2d.opengl=true
```

So the command for running the GUI application for Robocode will be:

```
java -Dsun.java2d.opengl=true -jar robocode-tankroyale-gui-x.y.z.jar
```

## Sample bots

Next, you'll need to provide some bots for the game in order to start up bots that can battle against each other. For
this purpose, the sample bots provided for Robocode come in handy. So you could download a zip archive from the
[Robocode releases], e.g. sample bots for Java that will run when Java has already been installed on your system.

So download and extract the `sample-bots-java-x.y.z.zip` archive to a directory somewhere, and note the file path of the
directory where all the sample bot directories are located.

## Configuration of bot directories

Next, you should start up the Robocode application and select Config -> Bot Root Directories from the menu, and then add
the directory to where you installed the sample bots.

The sample bots should show up under the Bot Directories when selecting Battle -> Start Battle from the menu. If not,
you might have a misconfiguration with the root bot directory.

[Robocode releases]: https://github.com/robocode-dev/tank-royale/releases "Robocode releases"
