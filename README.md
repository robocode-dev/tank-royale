# Robocode Tank Royale

<img src="gfx/Logo/Robocode-logo.svg" width="220">

**Build the best - destroy the rest!**

## About

Robocode is a [programming game](https://en.wikipedia.org/wiki/Programming_game) where the goal is to code a bot in the
form of a virtual tank to compete against other bots in a virtual battle arena. The player is the programmer of the bot,
who will have no direct influence on the game. Instead, the player must write a program for the brain of the bot. The
program is telling how the bot must behave and react to events occurring in the battle arena.

The name _Robocode_ originates from an [older version of the game](https://robocode.sourceforge.io/) and is a short
for "Robot code". With this new version, the term "bot" is preferred "robot".

The game is designed to help you learn how to program and improve your programming skills and have fun while doing it.
Robocode is also useful when studying or improving [machine learning](https://en.wikipedia.org/wiki/Machine_learning)
in a fast-running real-time game.

Robocode's battles take place on a battlefield, where small automated tank bots fight it out until only one is left
like a Battle Royale Game. Hence the name _Tank Royale_.

Please notice that Robocode contains no gore, no blood, no people, and no politics. The battles are simply for the
excitement of the competition that we love so much.

## Example of a battle

<img src="buildDocs/docs/images/robocode-battle-anim.gif">

## Documentation

Main page:
[Robocode Tank Royale Docs](https://robocode-dev.github.io/tank-royale/)

### Try it out
Please head over to [My First Bot tutorial](https://robocode-dev.github.io/tank-royale/tutorial/my-first-bot.html) to learn how to set up your first bot for Robocode Tank Royale.

## Supported platforms

The Robocode game itself needs Java 11 or newer for running, and has been tested to run on:

- Windows
- macOS
- Linux

Bots can (in theory) be written for _any_ platform and programming language, as long as they have access to a
[WebSocket](https://en.wikipedia.org/wiki/WebSocket) API.

However, to handle all the trivial communication between a bot and the server, Bot APIs are provided for:

- Java (JVM) platform and
- Microsoft .Net platform

Both implementations are first class citizens within Tank Royale.

Next platform to support will be:

- Web platform (JavaScript or WebAssembly)

## Supported programming languages

Due to the current bot APIs for the JVM and .Net, Robocode (should be) able to support these programming languages with
the current Bot APIs:

- **Java (JVM) platform:** These (and more) programming languages are available:
    - [Java], [Groovy], [Kotlin], [Scala], [Jython], and [Clojure]

- **.Net platform:** These programming languages (and more) are available:
    - [C#], [F#], [Visual Basic], and [IronPython]

Note that sample bots are only provided for Java and C# for now. But sample bots for other programming languages might
arrive in the future.

## Build tool used

[Gradle] is being used as build tool for building all artifacts and documentation of Tank Royale, which is a
multi-module project.

## Maintainer

[@flemming-n-larsen](https://github.com/flemming-n-larsen)

## License

[Apache License 2.0](LICENSE) © [Flemming N. Larsen](https://github.com/flemming-n-larsen)


[Java]: https://docs.oracle.com/javase/tutorial/java/, "The Java Tutorials"

[Groovy]: https://groovy-lang.org/ "Groovy programming language"

[Kotlin]: https://kotlinlang.org/ "Kotlin programming language"

[Scala]: https://www.scala-lang.org/ "Scala programming language"

[Jython]: https://www.jython.org/ "Implementations of Python in Java"

[Clojure]: https://clojure.org/ "Clojure programming language"

[C#]: https://docs.microsoft.com/en-us/dotnet/csharp/ "C# documentation"

[F#]: https://docs.microsoft.com/en-us/dotnet/fsharp/ "F# documentation"

[Visual Basic]: https://docs.microsoft.com/en-us/dotnet/visual-basic/ "Visual Basic documentation"

[IronPython]: https://ironpython.net/ "Python programming language for .NET"

[Gradle]: https://gradle.org/ "Gradle Build Tool"
