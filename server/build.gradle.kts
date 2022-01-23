import proguard.gradle.ProGuardTask

val archiveTitle = "Robocode Tank Royale Server"
description = "Server for running Robocode Tank Royale"

group = "dev.robocode.tankroyale"
version = "0.8.15"

val archiveFileName = "$buildDir/libs/robocode-tankroyale-server-$version.jar"

buildscript {
    dependencies {
        classpath(libs.proguard.gradle)
    }
}

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    idea
}

idea.module.outputDir = file("$buildDir/classes/kotlin/main")

dependencies {
    implementation("dev.robocode.tankroyale:robocode-tankroyale-schema:0.8.1")
    implementation(libs.java.websocket)
    implementation(libs.slf4j.simple)
    implementation(libs.picocli)
    implementation(libs.jansi)

    testImplementation(libs.kotest.junit5)
    testImplementation(libs.mockk)
}

tasks {

    val fatJar by registering(dev.robocode.tankroyale.archive.FatJar::class) {
        dependsOn(clean, build)

        title.set(archiveTitle)
        mainClass.set("dev.robocode.tankroyale.server.ServerKt")
    }

    val proguard by registering(ProGuardTask::class) {
        dependsOn(fatJar)
        injars("$buildDir/libs/${project.name}-$version.jar")
        outjars(archiveFileName)
        configuration("proguard-rules.pro")
    }

    register("archive") {
        dependsOn(proguard)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(archiveFileName)
            groupId = group as String?
            artifactId
            version
        }
    }
}
