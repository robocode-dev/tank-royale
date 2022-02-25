val version = "0.9.13"

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
include("sample-bots:csharp")

// Docs
include("docs")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("tankroyale", version)
            version("node", "15.5.1")

            library("tankroyale-schema", "dev.robocode.tankroyale:robocode-tankroyale-schema:$version")
            library("gson", "com.google.code.gson:gson:2.8.9")
            library("gson-extras", "org.danilopianini:gson-extras:0.2.2")
            library("jansi", "org.fusesource.jansi:jansi:2.4.0")
            library("java-websocket", "org.java-websocket:Java-WebSocket:1.5.2")
            library("jsonschema2pojo", "org.jsonschema2pojo:jsonschema2pojo-gradle-plugin:1.1.1")
            library("kotest-junit5", "io.kotest:kotest-runner-junit5-jvm:5.0.3")
            library("picocli", "info.picocli:picocli:4.6.2")
            library("proguard-gradle", "com.guardsquare:proguard-gradle:7.2.0-beta5")
            library("miglayout-swing", "com.miglayout:miglayout-swing:11.0")
            library("mockk", "io.mockk:mockk:1.12.2")
            library("nv-i18n", "com.neovisionaries:nv-i18n:1.29")
            library("serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
            library("slf4j-simple", "org.slf4j:slf4j-simple:2.0.0-alpha5")

            plugin("shadow-jar","com.github.johnrengelman.shadow").version("7.1.2")
            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").version("1.6.10")
            plugin("kotlin-plugin-serialization", "org.jetbrains.kotlin.plugin.serialization").version("1.6.10")
            plugin("benmanes-versioning", "com.github.ben-manes.versions").version("0.40.0")
            plugin("hierynomus-license-base", "com.github.hierynomus.license-base").version("0.16.1")
            plugin("hidetake-ssh", "org.hidetake.ssh").version("2.10.1")
            plugin("itiviti-dotnet", "com.itiviti.dotnet").version("1.9.2")
            plugin("node-gradle", "com.github.node-gradle.node").version("3.1.1")
        }
    }
}