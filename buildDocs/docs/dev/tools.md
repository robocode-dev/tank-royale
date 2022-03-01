# Tools used for building Robocode Tank Royale

The following tools needs to be pre-installed on your system to build all the modules.

Standing in the root of the `tank-royale` folder, you can build all modules running gradle from the command-line:

    gradle build

Note that gradle is also used for creating documentation for the Bot APIs, e.g. javadoc and docfx, and uploading the
documentation to the GitHub pages. And gradle is being used for publishing artifacts for e.g. Nuget and Sonatype Maven.

Note that the current setup and tools have only been run on a Windows 11 PC so far. But it should be possible to set up
all the tools for macOS and Linux as well.

### Version control

- [git](https://git-scm.com/)

### Build tool

- [Gradle](https://gradle.org/) 7.4

### Java / JVM platform

- [Adopt OpenJDK 11](https://adoptopenjdk.net/)

### .Net platform

- [.Net 5.0 SDK](https://dotnet.microsoft.com/en-us/download/dotnet/5.0)
- [DocFX](https://dotnet.github.io/docfx/) 2.59.0
    - [Prerequisites Visual Studio 2019](https://dotnet.github.io/docfx/tutorial/docfx_getting_started.html) for
      running `docfx metadata`

### Documentation

- [Node.js](https://nodejs.org/en/) 16.14
    - [VuePress](https://vuepress.vuejs.org/) is used for generating a static web page with documentation
