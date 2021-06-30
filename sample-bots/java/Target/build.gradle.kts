plugins {
    java
}

dependencies {
    implementation(project(":bot-api:java"))
}

group = "dev.robocode.tankroyale.sample.bots"
version = "1.0"
description = "Target - A Robocode Tank Royale Sample Bot"
java.sourceCompatibility = JavaVersion.VERSION_11
