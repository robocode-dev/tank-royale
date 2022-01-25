import dev.robocode.tankroyale.archive.FatJar
import proguard.gradle.ProGuardTask

group = "dev.robocode.tankroyale"
version = "0.8.15"
description = "Server for running Robocode Tank Royale"

val jarManifestTitle = "Robocode Tank Royale Server"
val jarManifestMainClass = "dev.robocode.tankroyale.server.ServerKt"

val archiveFileName = "$buildDir/libs/robocode-tankroyale-server-$version.jar"

buildscript {
    dependencies {
        classpath(libs.proguard.gradle)
    }
}

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-publish`
    idea
}

idea.module.outputDir = file("$buildDir/classes/kotlin/main") // needed?

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
    val fatJar by registering(FatJar::class) {
        dependsOn(classes)

        title.set(jarManifestTitle)
        mainClass.set(jarManifestMainClass)
    }

    val proguard by registering(ProGuardTask::class) {
        dependsOn(fatJar)

        injars("$buildDir/libs/${project.name}-$version.jar")
        outjars(archiveFileName)
        configuration("proguard-rules.pro")
    }

    jar { // Replace jar task
        actions = emptyList()
        finalizedBy(proguard)
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