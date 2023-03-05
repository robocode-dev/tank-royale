# Bot API for Java (JVM)

This directory contains the Bot API for developing bots for Robocode Tank Royale with the Java (JVM) platform.

The Bot API is provided as a JAR archive and built for [Java 11].

## Build commands

#### Clean build directory:

```shell
./gradlew :bot-api:java:clean
```

#### Build/compile classes and javadoc:

```shell
./gradlew :bot-api:java:build
```

Compiled files are output to the `/build` directory:

- `classes`: Contains all the compiled class files.
- `docs`: Contains the compiled [javadoc] files.
- `libs`: Artifacts as JAR (Java Archive) files:
    - `robocode-tankrayale-bot-api-x.y.z.jar` is the bot API artifact.
    - `robocode-tankrayale-bot-api-x.y.z-javadoc.jar` archive contains all javadoc files.
    - `robocode-tankrayale-bot-api-x.y.z-sources.jar` archive contains all source files.

The javadoc can be viewed in a browser by opening this file with a browser:

```
/build/docs/javadoc/index.html
```

#### Publish artifact to the local Maven repository

```shell
./gradlew :bot-api:java:publishToMavenLocal
```


[Java 11]: https://docs.oracle.com/en/java/javase/11/ "Java 11 documentation"

[javadoc]: https://www.oracle.com/technical-resources/articles/java/javadoc-tool.html "Javadoc tool"
