<!--
Developers are strongly encouraged to use the provided [devcontainer](../../.devcontainer) for a pre-configured development environment.
See [Developing inside a Container](https://code.visualstudio.com/docs/devcontainers/containers) for guidance.
-->

# Tools used for building Robocode Tank Royale

## Required build tool

The following tools needs to be pre-installed on your system to build all the modules.

Standing in the root of the `tank-royale` folder, you can build all modules running gradle from the command-line:

    gradle build

Note that gradle is also used for creating documentation for the Bot APIs, e.g. javadoc and docfx, and uploading the
documentation to the GitHub pages. And gradle is being used for publishing artifacts for e.g. Nuget and Sonatype Maven.

Note that the current setup and tools have only been run on a Windows 11 PC so far. But it should be possible to set up
all the tools for macOS and Linux as well.

### Version control

- [git](https://git-scm.com/) to access [GitHub](https://github.com/robocode-dev/tank-royale)

### Build tool

- [Gradle](https://gradle.org/) (using Kotlin DSL)

### Java / JVM platform

**Important:** Developers building Robocode Tank Royale require **JDK 17-21** (not JDK 11).

- **For building Robocode (developers):** JDK 17-21, e.g.
  - [Eclipse Temurin JDK 17](https://adoptium.net/temurin/releases/?version=17) or
  - [Eclipse Temurin JDK 21](https://adoptium.net/temurin/releases/?version=21)

- **For running Robocode (end users):** Java 11 or newer

**Note:** Java 22 or newer may cause issues with ProGuard. This will be resolved in the future, but for now, stick to
JDK 17-21 for building the project.

### .NET platform

- [.NET 8 SDK](https://dotnet.microsoft.com/en-us/download/dotnet/8.0)
- [DocFX](https://dotnet.github.io/docfx/)

### Documentation

- [Node.js](https://nodejs.org/en/) v22 (LTS)
    - [VuePress](https://vuepress.vuejs.org/) v2 is used for generating a static web page with documentation
