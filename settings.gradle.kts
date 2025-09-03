rootProject.name = "dev.robocode.tankroyale"

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

// Docs
include("buildDocs")

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