enableFeaturePreview("VERSION_CATALOGS")

// Schema Generator
include("schema:java")
include("schema:dotnet")

// Booter
include("booter")

// Server
include("server")

// GUI app
include("gui-app")

// Bot API
include("bot-api:java")
include("bot-api:dotnet")

// Sample Bots archives
include("sample-bots:java")
include("sample-bots:dotnet")

// Docs
include("docs")

// Dependency version management
/*
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            alias("proguard-gradle").to("com.guardsquare:proguard-gradle:7.1.1")
            alias("kotlinx-serialization-json").to("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.0-RC")
            alias("picocli").to("info.picocli:picocli:4.6.1")

            alias("itiviti-dotnet").toPluginId("com.itiviti.dotnet").version("1.8.0")
        }
    }
}
 */