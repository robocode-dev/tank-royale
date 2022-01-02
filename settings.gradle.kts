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

enableFeaturePreview("VERSION_CATALOGS")
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Versions
            version("node-version", "15.5.1")

            // Libraries
            alias("gson").to("com.google.code.gson:gson:2.8.9")
            alias("gson-extras").to("org.danilopianini:gson-extras:0.2.2")
            alias("jansi").to("org.fusesource.jansi:jansi:2.4.0")
            alias("java-websocket").to("org.java-websocket:Java-WebSocket:1.5.2")
            alias("jsonschema2pojo").to("org.jsonschema2pojo:jsonschema2pojo-gradle-plugin:1.1.1")
            alias("kotest-junit5").to("io.kotest:kotest-runner-junit5-jvm:5.0.3")
            alias("picocli").to("info.picocli:picocli:4.6.2")
            alias("proguard-gradle").to("com.guardsquare:proguard-gradle:7.2.0-beta5")
            alias("miglayout-swing").to("com.miglayout:miglayout-swing:11.0")
            alias("mockk").to("io.mockk:mockk:1.12.2")
            alias("nv-i18n").to("com.neovisionaries:nv-i18n:1.29")
            alias("serialization-json").to("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
            alias("slf4j-simple").to("org.slf4j:slf4j-simple:2.0.0-alpha5")

            // Plugins
            alias("kotlin-jvm").toPluginId("org.jetbrains.kotlin.jvm").version("1.6.10")
            alias("kotlin-plugin-serialization").toPluginId("org.jetbrains.kotlin.plugin.serialization").version("1.6.10")
            alias("benmanes-versioning").toPluginId("com.github.ben-manes.versions").version("0.40.0")
            alias("hierynomus-license-base").toPluginId("com.github.hierynomus.license-base").version("0.16.1")
            alias("hidetake-ssh").toPluginId("org.hidetake.ssh").version("2.10.1")
            alias("itiviti-dotnet").toPluginId("com.itiviti.dotnet").version("1.9.2")
            alias("node-gradle").toPluginId("com.github.node-gradle.node").version("3.1.1")
        }
    }
}