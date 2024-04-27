// Schema Generator
include("schema:jvm")
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
include("buildDocs")


val tankroyaleVersion: String = providers.gradleProperty("version").get()

val kotlinVersion = "1.9.21"
val junitVersion = "5.10.2"
val kotestVersion = "5.8.1"

// Check dependencies with this command:  gradle dependencyUpdates

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("tankroyale", tankroyaleVersion)
            version("node", "15.5.1")

            library("gson", "com.google.code.gson:gson:2.10.1")
            library("gson-extras", "org.danilopianini:gson-extras:1.2.0")
            library("jansi", "org.fusesource.jansi:jansi:2.4.1")
            library("java-websocket", "org.java-websocket:Java-WebSocket:1.5.6")
            library("picocli", "info.picocli:picocli:4.7.5")
            library("proguard-gradle", "com.guardsquare:proguard-gradle:7.4.2")
            library("miglayout-swing", "com.miglayout:miglayout-swing:11.3")
            library("nv-i18n", "com.neovisionaries:nv-i18n:1.29")
            library("serialization-json", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            library("slf4j-simple", "org.slf4j:slf4j-simple:2.0.9")

            // Java testing
            library("assertj", "org.assertj:assertj-core:3.25.3")
            library("junit-api", "org.junit.jupiter:junit-jupiter-api:$junitVersion")
            library("junit-engine", "org.junit.jupiter:junit-jupiter-engine:$junitVersion")
            library("junit-params", "org.junit.jupiter:junit-jupiter-params:$junitVersion")
            library("system-stubs", "uk.org.webcompere:system-stubs-jupiter:2.1.6")

            // Kotlin testing
            library("kotest-junit5", "io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
            library("kotest-datatest", "io.kotest:kotest-framework-datatest:$kotestVersion")
            library("mockk", "io.mockk:mockk:1.13.10")

            // plugins
            plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm").version(kotlinVersion)
            plugin("kotlin-serialization", "org.jetbrains.kotlin.plugin.serialization").version(kotlinVersion)
            plugin("nexus-publish", "io.github.gradle-nexus.publish-plugin").version("2.0.0-rc-2")
            plugin("shadow-jar","com.github.johnrengelman.shadow").version("8.1.1")
            plugin("node-gradle", "com.github.node-gradle.node").version("7.0.2")
            plugin("jsonschema2pojo", "org.jsonschema2pojo").version("1.2.1")

            // Dependencies versions
            plugin("benmanes-versioning", "com.github.ben-manes.versions").version("0.51.0")
        }
    }
}