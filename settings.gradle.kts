plugins {
  // Apply the foojay-resolver plugin to allow automatic download of JDKs
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "Robocode Tank Royale"

val version: String = providers.gradleProperty("version").get()

// Lib
include("lib:common")
include("lib:client")

// Booter
include("booter")

// Server
include("server")

// Recorder
include("recorder")

// GUI
include("gui")

// Bot API
include("bot-api:java")
include("bot-api:dotnet")
include("bot-api:dotnet:schema")
include("bot-api:python")

// Sample Bots archives
include("sample-bots:java")
include("sample-bots:csharp")
include("sample-bots:python")

// Docs
include("docs-build")

// Check dependencies with this command: gradlew dependencyUpdates
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("tankroyale", version)
        }
        create("testLibs") {
            from(files("gradle/test-libs.versions.toml"))
        }
    }
}